package dimdoors.common.world;


import dimdoors.DimDoorsConfig;
import dimdoors.common.blocks.IDimDoor;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.DimType;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.dungeon.DungeonData;
import dimdoors.common.dungeon.DungeonSchematic;
import dimdoors.common.dungeon.pack.DungeonPackConfig;
import dimdoors.common.helpers.DungeonHelper;
import dimdoors.common.helpers.YCoordHelper;
import dimdoors.common.items.ItemDimensionalDoor;
import dimdoors.common.util.DimensionPos;
import dimdoors.common.util.RandomBetween;
import dimdoors.registry.DimBlocks;
import javafx.geometry.BoundingBox;
import javafx.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;

import java.util.Random;

public class PocketBuilder {

	public static final int MIN_POCKET_SIZE = 5;
	public static final int MAX_POCKET_SIZE = 51;
	public static final int DEFAULT_POCKET_SIZE = 39;

	public static final int MIN_POCKET_WALL_THICKNESS = 1;
	public static final int MAX_POCKET_WALL_THICKNESS = 10;
	public static final int DEFAULT_POCKET_WALL_THICKNESS = 5;

	private static final Random random = new Random();

	private PocketBuilder() {
	}

	private static boolean buildDungeonPocket(DungeonData dungeon, NewDimData dimension, DimLink link, DungeonSchematic schematic, World world) {
		//Calculate the destination point
		DungeonPackConfig packConfig = dungeon.dungeonType().Owner != null ? dungeon.dungeonType().Owner.getConfig() : null;
		DimensionPos source = link.source();
		int orientation = link.orientation();
		BlockPos destination;

		if (packConfig != null && packConfig.doDistortDoorCoordinates()) {
			destination = calculateNoisyDestination(source, dimension, dungeon, orientation);
		} else {
			destination = new BlockPos(source.getX(), source.getY(), source.getZ());
		}

		destination = new BlockPos(destination.getX(), YCoordHelper.adjustDestinationY(destination.getY(), world.getHeight(), schematic.getEntranceDoorLocation().getY(), schematic.getHeight()), destination.getZ());

		//Generate the dungeon
		schematic.copyToWorld(world, destination, orientation, link, random, false);

		//Finish up destination initialization
		dimension.initializeDungeon(destination.getX(), destination.getY(), destination.getZ(), orientation, link, dungeon);
		dimension.setFilled(true);

		return true;
	}

	public static boolean generateSelectedDungeonPocket(DimLink link, DungeonData dungeon) {
		if (link == null) {
			throw new IllegalArgumentException("link cannot be null.");
		}
		if (link.hasDestination()) {
			throw new IllegalArgumentException("link cannot have a destination assigned already.");
		}
		if (dungeon == null) {
			throw new IllegalArgumentException("dungeon cannot be null.");
		}

		// Try to load up the schematic
		DungeonSchematic schematic = null;
		schematic = loadAndValidateDungeon(dungeon);
		if (schematic == null) {
			return false;
		}

		// Register a new dimension
		NewDimData parent = PocketManager.getDimensionData(link.source().getDimension());
		NewDimData dimension = PocketManager.registerPocket(parent, DimType.DUNGEON);

		//Load a world
		World world = PocketManager.loadDimension(dimension.id());

		if (world == null || world.provider == null) {
			System.err.println("Could not initialize dimension for a dungeon!");
			return false;
		}

		return PocketBuilder.buildDungeonPocket(dungeon, dimension, link, schematic, world);
	}


	public static boolean generateNewDungeonPocket(DimLink link) {
		if (link == null) {
			throw new IllegalArgumentException("link cannot be null.");
		}
		if (link.hasDestination()) {
			throw new IllegalArgumentException("link cannot have a destination assigned already.");
		}

		//Choose a dungeon to generate
		NewDimData parent = PocketManager.getDimensionData(link.source().getDimension());
		Pair<DungeonData, DungeonSchematic> pair = selectNextDungeon(parent, random);
		if (pair == null) {
			System.err.println("Could not select a dungeon for generation!");
			return false;
		}
		DungeonData dungeon = pair.getKey();
		DungeonSchematic schematic = pair.getValue();

		//Register a new dimension
		NewDimData dimension = PocketManager.registerPocket(parent, DimType.DUNGEON);

		//Load a world
		World world = PocketManager.loadDimension(dimension.id());

		if (world == null || world.provider == null) {
			System.err.println("Could not initialize dimension for a dungeon!");
			return false;
		}

		return buildDungeonPocket(dungeon, dimension, link, schematic, world);
	}


	private static BlockPos calculateNoisyDestination(DimensionPos source, NewDimData dimension, DungeonData dungeon, int orientation) {
		int depth = NewDimData.calculatePackDepth(dimension.parent(), dungeon);
		int forwardNoise = RandomBetween.getRandomIntBetween(random, 10 * depth, 130 * depth);
		int sidewaysNoise = RandomBetween.getRandomIntBetween(random, -10 * depth, 10 * depth);

		//Rotate the link destination noise to point in the same direction as the door exit
		//and add it to the door's location. Use EAST as the reference orientation since linkDestination
		//is constructed as if pointing East.
		BlockPos linkDestination = new BlockPos(forwardNoise, 0, sidewaysNoise);
		BlockPos sourcePoint = new BlockPos(source.getX(), source.getY(), source.getZ());
		BlockPos zeroPoint = new BlockPos(0, 0, 0);
		//		BlockRotator.transformPoint(linkDestination, zeroPoint, orientation - BlockRotator.EAST_DOOR_METADATA, sourcePoint); TODO
		return linkDestination;
	}

	private static Pair<DungeonData, DungeonSchematic> selectNextDungeon(NewDimData parent, Random random) {
		DungeonData dungeon = null;
		DungeonSchematic schematic = null;

		dungeon = DungeonHelper.instance().selectNextDungeon(parent, random);

		if (dungeon != null) {
			schematic = loadAndValidateDungeon(dungeon);
		} else {
			System.err.println("Could not select a dungeon at all!");
		}

		if (schematic == null) {
			//TODO: In the future, remove this dungeon from the generation lists altogether.
			//That will have to wait until our code is updated to support that more easily.
			try {
				System.err.println("Loading the default error dungeon instead...");
				dungeon = DungeonHelper.instance().getDefaultErrorDungeon();
				schematic = loadAndValidateDungeon(dungeon);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return new Pair<>(dungeon, schematic);
	}

	private static DungeonSchematic loadAndValidateDungeon(DungeonData dungeon) {
		try {
			DungeonSchematic schematic = dungeon.loadSchematic();

			//Validate the dungeon's dimensions
			if (hasValidDimensions(schematic)) {
				schematic.applyImportFilters();

				//Check that the dungeon has an entrance or we'll have a crash
				if (schematic.getEntranceDoorLocation() == null) {
					System.err.println("The following schematic file does not have an entrance: " + dungeon.schematicPath());
					return null;
				}
			} else {
				System.err.println("The following schematic file has dimensions that exceed the maximum permitted dimensions for dungeons: " + dungeon.schematicPath());
				return null;
			}
			return schematic;
		} catch (Exception e) {
			System.err.println("An error occurred while loading the following schematic: " + dungeon.schematicPath());
			System.err.println(e.getMessage());
			return null;
		}
	}

	private static boolean hasValidDimensions(DungeonSchematic schematic) {
		return (schematic.getWidth() <= DungeonHelper.MAX_DUNGEON_WIDTH && schematic.getHeight() <= DungeonHelper.MAX_DUNGEON_HEIGHT && schematic.getLength() <= DungeonHelper.MAX_DUNGEON_LENGTH);
	}

	public static boolean generateNewPocket(DimLink link, Block door, DimType type) {
		return generateNewPocket(link, DEFAULT_POCKET_SIZE, DEFAULT_POCKET_WALL_THICKNESS, door, type);
	}

	private static int getDoorOrientation(DimensionPos source) {
		World world = DimensionManager.getWorld(source.getDimension());
		if (world == null) {
			throw new IllegalStateException("The link's source world should be loaded!");
		}

		//Check if the block below that point is actually a door
		IBlockState state = world.getBlockState(source.down());
		if (!(state.getBlock() instanceof IDimDoor)) {
			throw new IllegalStateException("The link's source is not a door block. It should be impossible to traverse a rift without a door!");
		}

		//Return the orientation portion of its metadata
		return state.getBlock().getMetaFromState(state) & 3;
	}

	public static void validatePocketSetup(DimLink link, int size, int wallThickness, Block door) {
		if (link == null) {
			throw new IllegalArgumentException();
		}
		if (link.linkType() != LinkType.PERSONAL && link.hasDestination()) {
			throw new IllegalArgumentException("link cannot have a destination assigned already.");
		}

		if (door == null) {
			throw new IllegalArgumentException("Must have a doorItem to gen one!!");

		}

		if (size < MIN_POCKET_SIZE || size > MAX_POCKET_SIZE) {
			throw new IllegalArgumentException("size must be between " + MIN_POCKET_SIZE + " and " + MAX_POCKET_SIZE + ", inclusive.");
		}
		if (wallThickness < MIN_POCKET_WALL_THICKNESS || wallThickness > MAX_POCKET_WALL_THICKNESS) {
			throw new IllegalArgumentException("wallThickness must be between " + MIN_POCKET_WALL_THICKNESS + " and " + MAX_POCKET_WALL_THICKNESS + ", inclusive.");
		}
		if (size % 2 == 0) {
			throw new IllegalArgumentException("size must be an odd number.");
		}
		if (size < 2 * wallThickness + 3) {
			throw new IllegalArgumentException("size must be large enough to fit the specified wall thickness and some air space.");
		}
	}

	/**
	 * I know this is almost a copy of generateNewPocket, but we might want to change other things.
	 *
	 * @param link
	 * @param player
	 * @param door
	 * @return
	 */
	public static boolean generateNewPersonalPocket(DimLink link, EntityPlayer player, Block door) {
		//incase a chicken walks in or something
		if (player == null) {
			return false;
		}
		int wallThickness = DEFAULT_POCKET_WALL_THICKNESS;
		int size = DEFAULT_POCKET_SIZE;

		validatePocketSetup(link, size, wallThickness, door);

		try {
			//Register a new dimension
			NewDimData parent = PocketManager.getDimensionData(link.source().getDimension());
			NewDimData dimension = PocketManager.registerPocket(parent, DimType.PERSONAL, player.getGameProfile().getId().toString());


			//Load a world
			World world = PocketManager.loadDimension(dimension.id());

			if (world == null || world.provider == null) {
				System.err.println("Could not initialize dimension for a pocket!");
				return false;
			}

			//Calculate the destination point
			DimensionPos source = link.source();
			int destinationY = YCoordHelper.adjustDestinationY(link.source().getY(), world.getHeight(), wallThickness + 1, size);
			int orientation = getDoorOrientation(source);

			//Place a link leading back out of the pocket
			DimLink reverseLink = dimension.createLink(source.getX(), destinationY, source.getZ(), LinkType.REVERSE, (link.orientation() + 2) % 4);
			parent.setLinkDestination(reverseLink, source.getX(), source.getY(), source.getZ());

			//Build the actual pocket area
			buildPocket(world, source.getX(), destinationY, source.getZ(), orientation, size, wallThickness, door);

			//Finish up destination initialization
			dimension.initializePocket(source.getX(), destinationY, source.getZ(), orientation, link);
			dimension.setFilled(true);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean generateNewPocket(DimLink link, int size, int wallThickness, Block door, DimType type) {
		validatePocketSetup(link, size, wallThickness, door);

		try {
			//Register a new dimension
			NewDimData parent = PocketManager.getDimensionData(link.source().getDimension());
			NewDimData dimension = PocketManager.registerPocket(parent, type);


			//Load a world
			World world = PocketManager.loadDimension(dimension.id());

			if (world == null || world.provider == null) {
				System.err.println("Could not initialize dimension for a pocket!");
				return false;
			}

			//Calculate the destination point
			DimensionPos source = link.source();
			int destinationY = YCoordHelper.adjustDestinationY(source.getY(), world.getHeight(), wallThickness + 1, size);
			int orientation = getDoorOrientation(source);

			//Place a link leading back out of the pocket

			DimLink reverseLink = dimension.createLink(source.getX(), destinationY, source.getZ(), LinkType.REVERSE, (link.orientation() + 2) % 4);
			parent.setLinkDestination(reverseLink, source.getX(), source.getY(), source.getZ());

			//Build the actual pocket area
			buildPocket(world, source.getX(), destinationY, source.getZ(), orientation, size, wallThickness, door);

			//Finish up destination initialization
			dimension.initializePocket(source.getX(), destinationY, source.getZ(), orientation, link);
			dimension.setFilled(true);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void buildPocket(World world, int x, int y, int z, int orientation, int size, int wallThickness, Block doorBlock) {
		if (size < MIN_POCKET_SIZE || size > MAX_POCKET_SIZE) {
			throw new IllegalArgumentException("size must be between " + MIN_POCKET_SIZE + " and " + MAX_POCKET_SIZE + ", inclusive.");
		}
		if (wallThickness < MIN_POCKET_WALL_THICKNESS || wallThickness > MAX_POCKET_WALL_THICKNESS) {
			throw new IllegalArgumentException("wallThickness must be between " + MIN_POCKET_WALL_THICKNESS + " and " + MAX_POCKET_WALL_THICKNESS + ", inclusive.");
		}
		if (size % 2 == 0) {
			throw new IllegalArgumentException("size must be an odd number.");
		}
		if (size < 2 * wallThickness + 3) {
			throw new IllegalArgumentException("size must be large enough to fit the specified wall thickness and some air space.");
		}
		if (!(doorBlock instanceof IDimDoor)) {
			throw new IllegalArgumentException("Door must implement IDimDoor");
		}


		BlockPos center = new BlockPos(x - wallThickness + 1 + (size / 2), y - wallThickness - 1 + (size / 2), z);
		BlockPos door = new BlockPos(x, y, z);
		//		BlockRotator.transformPoint(center, door, orientation - BlockRotator.EAST_DOOR_METADATA, door); TODO

		//Build the outer layer of Eternal Fabric
		buildBox(world, center.getX(), center.getY(), center.getZ(), (size / 2), DimBlocks.blockDimWallPerm.getDefaultState(), false, 0);

		//check if we are building a personal pocket
		IBlockState state = DimBlocks.blockDimWall.getDefaultState();
		if (world.provider instanceof PersonalPocketProvider) {
			state = DimBlocks.blockDimWall.getStateFromMeta(2);
		}

		//Build the (wallThickness - 1) layers of Fabric of Reality
		for (int layer = 1; layer < wallThickness; layer++) {
			buildBox(world, center.getX(), center.getY(), center.getZ(), (size / 2) - layer, state, layer < (wallThickness - 1) && DimDoorsConfig.tntEnabled, DimDoorsConfig.nonTntWeight);
		}

		//MazeBuilder.generate(world, x, y, z, random);

		//Build the door
		EnumFacing doorOrientation = EnumFacing.NORTH;//TODO BlockRotator.transformMetadata(BlockRotator.EAST_DOOR_METADATA, orientation - BlockRotator.EAST_DOOR_METADATA + 2, doorBlock);
		ItemDimensionalDoor.placeDoor(world, door.down(), doorOrientation, doorBlock, false);

	}

	private static void buildBox(World world, int centerX, int centerY, int centerZ, int radius, IBlockState state, boolean placeTnt, int nonTntWeight) {
		int x, y, z;

		final int startX = centerX - radius;
		final int startY = centerY - radius;
		final int startZ = centerZ - radius;

		final int endX = centerX + radius;
		final int endY = centerY + radius;
		final int endZ = centerZ + radius;

		//Build faces of the box
		for (x = startX; x <= endX; x++) {
			for (z = startZ; z <= endZ; z++) {
				setBlockDirectlySpecial(world, x, startY, z, state, placeTnt, nonTntWeight);
				setBlockDirectlySpecial(world, x, endY, z, state, placeTnt, nonTntWeight);
			}

			for (y = startY; y <= endY; y++) {
				setBlockDirectlySpecial(world, x, y, startZ, state, placeTnt, nonTntWeight);
				setBlockDirectlySpecial(world, x, y, endZ, state, placeTnt, nonTntWeight);
			}
		}

		for (y = startY; y <= endY; y++) {
			for (z = startZ; z <= endZ; z++) {
				setBlockDirectlySpecial(world, startX, y, z, state, placeTnt, nonTntWeight);
				setBlockDirectlySpecial(world, endX, y, z, state, placeTnt, nonTntWeight);
			}
		}
	}

	private static void setBlockDirectlySpecial(World world, int x, int y, int z, IBlockState state, boolean placeTnt, int nonTntWeight) {
		if (placeTnt && random.nextInt(nonTntWeight + 1) == 0) {
			setBlockDirectly(world, x, y, z, Blocks.TNT.getDefaultState().withProperty(BlockTNT.EXPLODE, true));
		} else {
			setBlockDirectly(world, x, y, z, state);
		}
	}

	private static void setBlockDirectly(World world, int x, int y, int z, IBlockState state) {
		int cX = x >> 4;
		int cZ = z >> 4;
		int cY = y >> 4;
		Chunk chunk;

		int localX = (x % 16) < 0 ? (x % 16) + 16 : (x % 16);
		int localZ = (z % 16) < 0 ? (z % 16) + 16 : (z % 16);
		ExtendedBlockStorage extBlockStorage;

		chunk = world.getChunkFromChunkCoords(cX, cZ);
		extBlockStorage = chunk.getBlockStorageArray()[cY];
		if (extBlockStorage == null) {
			extBlockStorage = new ExtendedBlockStorage(cY << 4, !world.provider.isNether());
			chunk.getBlockStorageArray()[cY] = extBlockStorage;
		}
		extBlockStorage.set(localX, y & 15, localZ, state);
		chunk.markDirty();
	}

	public static BoundingBox calculateDefaultBounds(NewDimData pocket) {
		// Calculate the XZ bounds of this pocket assuming that it has the default size
		// The Y bounds will be set to encompass the height of a chunk.

		int minX = 0;
		int minZ = 0;
		DimensionPos origin = pocket.origin();
		int orientation = pocket.orientation();
		if (orientation < 0 || orientation > 3) {
			throw new IllegalArgumentException("pocket has an invalid orientation value.");
		}
		switch (orientation) {
			case 0:
				minX = origin.getX() - DEFAULT_POCKET_WALL_THICKNESS + 1;
				minZ = origin.getZ() - DEFAULT_POCKET_SIZE / 2;
				break;
			case 1:
				minX = origin.getX() - DEFAULT_POCKET_SIZE / 2;
				minZ = origin.getZ() - DEFAULT_POCKET_WALL_THICKNESS + 1;
				break;
			case 2:
				minX = origin.getX() + DEFAULT_POCKET_WALL_THICKNESS - DEFAULT_POCKET_SIZE;
				minZ = origin.getZ() - DEFAULT_POCKET_SIZE / 2;
				break;
			case 3:
				minX = origin.getX() - DEFAULT_POCKET_SIZE / 2;
				minZ = origin.getZ() + DEFAULT_POCKET_WALL_THICKNESS - DEFAULT_POCKET_SIZE;
				break;
		}
		return new BoundingBox(minX, 0, minZ, DEFAULT_POCKET_SIZE, 255, DEFAULT_POCKET_SIZE);
	}
}
