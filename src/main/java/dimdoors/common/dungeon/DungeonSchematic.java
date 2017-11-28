package dimdoors.common.dungeon;


import com.google.common.collect.Lists;
import dimdoors.common.blocks.IDimDoor;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.entity.EntityMonolith;
import dimdoors.common.schematic.ChunkBlockSetter;
import dimdoors.common.schematic.CompoundFilter;
import dimdoors.common.schematic.IBlockSetter;
import dimdoors.common.schematic.InvalidSchematicException;
import dimdoors.common.schematic.Schematic;
import dimdoors.common.schematic.WorldBlockSetter;
import dimdoors.common.util.DimensionPos;
import dimdoors.common.world.CustomLimboPopulator;
import dimdoors.registry.DimBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class DungeonSchematic extends Schematic {

	private static final int NETHER_DIMENSION_ID = -1;

	private int orientation;
	private BlockPos entranceDoorLocation;
	private List<BlockPos> exitDoorLocations;
	private List<BlockPos> dimensionalDoorLocations;
	private List<BlockPos> monolithSpawnLocations;
	private List<Block> modBlockFilterExceptions;

	private DungeonSchematic(Schematic source) {
		super(source);
		modBlockFilterExceptions = Lists.newArrayList();
		modBlockFilterExceptions.add(DimBlocks.blockDimWall);
		modBlockFilterExceptions.add(DimBlocks.blockDimWallPerm);
		modBlockFilterExceptions.add(DimBlocks.warpDoor);
		modBlockFilterExceptions.add(DimBlocks.dimensionalDoor);
		modBlockFilterExceptions.add(DimBlocks.transientDoor);
	}

	private DungeonSchematic() {
		//Used to create a dummy instance for readFromResource()
		super((short) 0, (short) 0, (short) 0, null, null, null);
	}

	public static DungeonSchematic readFromFile(String schematicPath) throws FileNotFoundException, InvalidSchematicException {
		return readFromFile(new File(schematicPath));
	}

	public static DungeonSchematic readFromFile(File schematicFile) throws FileNotFoundException, InvalidSchematicException {
		return readFromStream(new FileInputStream(schematicFile));
	}

	public static DungeonSchematic readFromResource(String resourcePath) throws InvalidSchematicException {
		//We need an instance of a class in the mod to retrieve a resource
		DungeonSchematic empty = new DungeonSchematic();
		InputStream schematicStream = empty.getClass().getResourceAsStream(resourcePath);
		return readFromStream(schematicStream);
	}

	public static DungeonSchematic readFromStream(InputStream schematicStream) throws InvalidSchematicException {
		return new DungeonSchematic(Schematic.readFromStream(schematicStream));
	}

	public static DungeonSchematic copyFromWorld(World world, int x, int y, int z, short width, short height, short length, boolean doCompactBounds) {
		return new DungeonSchematic(Schematic.copyFromWorld(world, x, y, z, width, height, length, doCompactBounds));
	}

	private static void transformCorners(BlockPos schematicEntrance, BlockPos pocketCenter, int turnAngle, BlockPos minCorner, BlockPos maxCorner) {
		/*int temp; TODO
		BlockRotator.transformPoint(minCorner, schematicEntrance, turnAngle, pocketCenter);
		BlockRotator.transformPoint(maxCorner, schematicEntrance, turnAngle, pocketCenter);
		if (minCorner.getX() > maxCorner.getX()) {
			temp = minCorner.getX();
			minCorner.setX(maxCorner.getX());
			maxCorner.setX(temp);
		}
		if (minCorner.getY() > maxCorner.getY()) {
			temp = minCorner.getY();
			minCorner.setY(maxCorner.getY());
			maxCorner.setY(temp);
		}
		if (minCorner.getZ() > maxCorner.getZ()) {
			temp = minCorner.getZ();
			minCorner.setZ(maxCorner.getZ());
			maxCorner.setZ(temp);
		}*/
	}

	private static void createEntranceReverseLink(World world, NewDimData dimension, BlockPos pocketCenter, DimLink entryLink) {
		IBlockState state = world.getBlockState(pocketCenter.down());
		int orientation = state.getBlock().getMetaFromState(state);
		DimLink reverseLink = dimension.createLink(pocketCenter.getX(), pocketCenter.getY(), pocketCenter.getZ(), LinkType.REVERSE, orientation);
		DimensionPos destination = entryLink.source();
		NewDimData prevDim = PocketManager.getDimensionData(destination.getDimension());
		prevDim.setLinkDestination(reverseLink, destination.getX(), destination.getY(), destination.getZ());
		initDoorTileEntity(world, pocketCenter);
	}

	private static void createExitDoorLink(World world, NewDimData dimension, BlockPos point, BlockPos entrance, int rotation, BlockPos pocketCenter, IBlockSetter blockSetter) {
		//Transform the door's location to the pocket coordinate system
		//		BlockRotator.transformPoint(point, entrance, rotation, pocketCenter); TODO
		IBlockState state = world.getBlockState(point.down());
		int orientation = state.getBlock().getMetaFromState(state);
		dimension.createLink(point.getX(), point.getY(), point.getZ(), LinkType.DUNGEON_EXIT, orientation);
		//Replace the sandstone block under the exit door with the same block as the one underneath it
		int x = point.getX();
		int y = point.getY() - 3;
		int z = point.getZ();
		if (y >= 0) {
			IBlockState blockstate = world.getBlockState(new BlockPos(x, y, z));
			blockSetter.setBlock(world, x, y + 1, z, blockstate);
		}
		initDoorTileEntity(world, point);
	}

	private static void createDimensionalDoorLink(World world, NewDimData dimension, BlockPos point, BlockPos entrance, int rotation, BlockPos pocketCenter) {
		//Transform the door's location to the pocket coordinate system
		//		BlockRotator.transformPoint(point, entrance, rotation, pocketCenter); TODO
		IBlockState state = world.getBlockState(point.down());
		int orientation = state.getBlock().getMetaFromState(state);

		dimension.createLink(point.getX(), point.getY(), point.getZ(), LinkType.DUNGEON, orientation);
		initDoorTileEntity(world, point);
	}

	private static void spawnMonolith(World world, BlockPos point, BlockPos entrance, int rotation, BlockPos pocketCenter, boolean canSpawn, IBlockSetter blockSetter) {
		//Transform the frame block's location to the pocket coordinate system
		//		BlockRotator.transformPoint(point, entrance, rotation, pocketCenter); TODO
		//Remove frame block
		blockSetter.setBlock(world, point.getX(), point.getY(), point.getZ(), Blocks.AIR.getDefaultState());
		//Spawn Monolith
		if (canSpawn) {
			EntityMonolith mob = new EntityMonolith(world);
			mob.setLocationAndAngles(point.getX(), point.getY(), point.getZ(), 1, 1);
			world.spawnEntity(mob);
		}
	}

	private static void initDoorTileEntity(World world, BlockPos point) {
		Block door = world.getBlockState(point).getBlock();
		Block door2 = world.getBlockState(point.down()).getBlock();

		if (door instanceof IDimDoor && door2 instanceof IDimDoor) {
			((IDimDoor) door).initDoorTE(world, point);
			((IDimDoor) door).initDoorTE(world, point);
		} else {
			throw new IllegalArgumentException("Tried to init a dim door TE on a block that isnt a Dim Door!!");
		}
	}

	private static void writeDepthSign(World world, BlockPos pocketCenter, int depth) {
		final int SEARCH_RANGE = 6;

		int x, y, z;
		Block block;
		int dx, dy, dz;

		for (dy = SEARCH_RANGE; dy >= -SEARCH_RANGE; dy--) {
			for (dz = -SEARCH_RANGE; dz <= SEARCH_RANGE; dz++) {
				for (dx = -SEARCH_RANGE; dx <= SEARCH_RANGE; dx++) {
					x = pocketCenter.getX() + dx;
					y = pocketCenter.getY() + dy;
					z = pocketCenter.getZ() + dz;
					block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
					if (block == Blocks.WALL_SIGN || block == Blocks.STANDING_SIGN) {
						TileEntitySign signEntity = new TileEntitySign();
						signEntity.signText[1] = new TextComponentString("Level " + depth); // TODO localize
						world.setTileEntity(new BlockPos(x, y, z), signEntity);
						return;
					}
				}
			}
		}
	}

	public int getOrientation() {
		return orientation;
	}

	public BlockPos getEntranceDoorLocation() {
		return entranceDoorLocation;
	}

	public void applyImportFilters() {
		//Search for special blocks (warp doors, dim doors, and end portal frames that mark Monolith spawn points)
		SpecialBlockFinder finder = new SpecialBlockFinder(DimBlocks.warpDoor, DimBlocks.dimensionalDoor, Blocks.END_PORTAL_FRAME, Blocks.SANDSTONE);
		applyFilter(finder);

		//Flip the entrance's orientation to get the dungeon's orientation
		//		orientation = BlockRotator.transformMetadata(finder.getEntranceOrientation(), 2, Blocks.OAK_DOOR); TODO

		entranceDoorLocation = finder.getEntranceDoorLocation();
		exitDoorLocations = finder.getExitDoorLocations();
		dimensionalDoorLocations = finder.getDimensionalDoorLocations();
		monolithSpawnLocations = finder.getMonolithSpawnLocations();

		//Filter out mod blocks except some of our own
		CompoundFilter standardizer = new CompoundFilter();
		standardizer.addFilter(new ModBlockFilter(modBlockFilterExceptions, DimBlocks.blockDimWall, (byte) 0));

		//Also convert standard DD block IDs to local versions
		applyFilter(standardizer);
	}

	public void applyExportFilters() {
		//Check if some block IDs assigned by Forge differ from our standard IDs
		//If so, change the IDs to standard values
		CompoundFilter standardizer = new CompoundFilter();

		//Filter out mod blocks except some of our own
		//This comes after ID standardization because the mod block filter relies on standardized IDs
		standardizer.addFilter(new ModBlockFilter(modBlockFilterExceptions, DimBlocks.blockDimWall, (byte) 0));

		applyFilter(standardizer);
	}

	public void copyToWorld(World world, BlockPos pocketCenter, int targetOrientation, DimLink entryLink, Random random, boolean notifyClients) {
		if (notifyClients) {
			copyToWorld(world, pocketCenter, targetOrientation, entryLink, random, new WorldBlockSetter(false, true, false));
		} else {
			copyToWorld(world, pocketCenter, targetOrientation, entryLink, random, new ChunkBlockSetter(false));
		}
	}

	public void copyToWorld(World world, BlockPos pocketCenter, int targetOrientation, DimLink entryLink, Random random, IBlockSetter blockSetter) {
		//TODO: This function is an improvised solution so we can get the release moving. In the future,
		//we should generalize block transformations and implement support for them at the level of Schematic,
		//then just use that support from DungeonSchematic instead of making this local fix.
		//It might be easiest to support transformations using a WorldOperation

		final int turnAngle = targetOrientation - this.orientation;

		int index;
		int count;
		Block block;
		int blockMeta = 0;
		int dx, dy, dz;
		BlockPos pocketPoint;

		//Copy blocks and metadata into the world
		index = 0;
		for (dy = 0; dy < height; dy++) {
			for (dz = 0; dz < length; dz++) {
				for (dx = 0; dx < width; dx++) {
					pocketPoint = new BlockPos(dx, dy, dz);
					block = blocks[index];
					//					BlockRotator.transformPoint(pocketPoint, entranceDoorLocation, turnAngle, pocketCenter); TODO
					//					blockMeta = BlockRotator.transformMetadata(metadata[index], turnAngle, block); TODO

					//In the future, we might want to make this more efficient by building whole chunks at a time
					blockSetter.setBlock(world, pocketPoint.getX(), pocketPoint.getY(), pocketPoint.getZ(), block.getStateFromMeta(blockMeta));
					index++;
				}
			}
		}
		//Copy tile entities into the world
		count = tileEntities.tagCount();
		for (index = 0; index < count; index++) {
			NBTTagCompound tileTag = tileEntities.getCompoundTagAt(index);
			//Rewrite its location to be in world coordinates
			pocketPoint = new BlockPos(tileTag.getInteger("x"), tileTag.getInteger("y"), tileTag.getInteger("z"));
			//			BlockRotator.transformPoint(pocketPoint, entranceDoorLocation, turnAngle, pocketCenter); TODO
			tileTag.setInteger("x", pocketPoint.getX());
			tileTag.setInteger("y", pocketPoint.getY());
			tileTag.setInteger("z", pocketPoint.getZ());
			//Load the tile entity and put it in the world
			world.setTileEntity(pocketPoint, TileEntity.create(world, tileTag));
		}

		setUpDungeon(PocketManager.createDimensionData(world), world, pocketCenter, turnAngle, entryLink, random, blockSetter);
	}

	private void setUpDungeon(NewDimData dimension, World world, BlockPos pocketCenter, int turnAngle, DimLink entryLink, Random random, IBlockSetter blockSetter) {
		//Transform dungeon corners
		BlockPos minCorner = new BlockPos(0, 0, 0);
		BlockPos maxCorner = new BlockPos(width - 1, height - 1, length - 1);
		transformCorners(entranceDoorLocation, pocketCenter, turnAngle, minCorner, maxCorner);

		//Fill empty chests and dispensers
		FillContainersOperation filler = new FillContainersOperation(random);
		filler.apply(world, minCorner, maxCorner);

		//Set up entrance door rift
		createEntranceReverseLink(world, dimension, pocketCenter, entryLink);

		//Set up link data for dimensional doors
		for (BlockPos location : dimensionalDoorLocations) {
			createDimensionalDoorLink(world, dimension, location, entranceDoorLocation, turnAngle, pocketCenter);
		}

		//Set up link data for exit door
		for (BlockPos location : exitDoorLocations) {
			createExitDoorLink(world, dimension, location, entranceDoorLocation, turnAngle, pocketCenter, blockSetter);
		}

		//Remove end portal frames and spawn Monoliths, if allowed
		boolean canSpawn = CustomLimboPopulator.isMobSpawningAllowed();
		for (BlockPos location : monolithSpawnLocations) {
			spawnMonolith(world, location, entranceDoorLocation, turnAngle, pocketCenter, canSpawn, blockSetter);
		}

		// If this is a Nether dungeon, search for a sign near the entry door and write the dimension's depth.
		// Checking if this is specifically a Nether pack dungeon is a bit tricky, so I'm going to use this
		// approach to check - if the dungeon is rooted in the Nether, then it SHOULD be a Nether dungeon.
		// This isn't necessarily true if someone uses dd-rift to spawn a dungeon, but it should work under
		// normal use of the mod.
		if (dimension.root().id() == NETHER_DIMENSION_ID) {
			writeDepthSign(world, pocketCenter, dimension.depth());
		}
	}
}
