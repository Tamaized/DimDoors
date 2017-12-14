package dimdoors.common.blocks;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dimdoors.DimDoorsConfig;
import dimdoors.client.particle.ClosingRiftFX;
import dimdoors.client.particle.GoggleRiftFX;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.core.RiftRegenerator;
import dimdoors.common.tileentity.TileEntityRift;
import dimdoors.common.util.DimensionPos;
import dimdoors.registry.DimBlocks;
import dimdoors.registry.DimItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class BlockRift extends Block implements ITileEntityProvider {
	public static final int MAX_WORLD_THREAD_DROP_CHANCE = 1000;
	private static final float MIN_IMMUNE_RESISTANCE = 5000.0F;
	private static final int BLOCK_DESTRUCTION_RANGE = 4;
	private static final int RIFT_SPREAD_RANGE = 5;
	private static final int MAX_BLOCK_SEARCH_CHANCE = 100;
	private static final int BLOCK_SEARCH_CHANCE = 50;
	private static final int MAX_BLOCK_DESTRUCTION_CHANCE = 100;
	private static final int BLOCK_DESTRUCTION_CHANCE = 50;
	private final List<Block> blocksImmuneToRift;    // List of Vanilla blocks immune to rifts
	private final List<Block> modBlocksImmuneToRift; // List of DD blocks immune to rifts

	public BlockRift(Material par2Material) {
		super(par2Material);
		this.setTickRandomly(true);
		this.modBlocksImmuneToRift = Lists.newArrayList();
		this.modBlocksImmuneToRift.add(DimBlocks.blockDimWall);
		this.modBlocksImmuneToRift.add(DimBlocks.blockDimWallPerm);
		this.modBlocksImmuneToRift.add(DimBlocks.dimensionalDoor);
		this.modBlocksImmuneToRift.add(DimBlocks.warpDoor);
		this.modBlocksImmuneToRift.add(DimBlocks.transTrapdoor);
		this.modBlocksImmuneToRift.add(DimBlocks.unstableDoor);
//		this.modBlocksImmuneToRift.add(DimBlocks.blockRift);
		this.modBlocksImmuneToRift.add(DimBlocks.transientDoor);
		this.modBlocksImmuneToRift.add(DimBlocks.goldenDimensionalDoor);
		this.modBlocksImmuneToRift.add(DimBlocks.goldenDoor);

		this.blocksImmuneToRift = Lists.newArrayList();

		this.blocksImmuneToRift.add(DimBlocks.blockDimWall);
		this.blocksImmuneToRift.add(DimBlocks.blockDimWallPerm);
		this.blocksImmuneToRift.add(DimBlocks.dimensionalDoor);
		this.blocksImmuneToRift.add(DimBlocks.warpDoor);
		this.blocksImmuneToRift.add(DimBlocks.transTrapdoor);
		this.blocksImmuneToRift.add(DimBlocks.unstableDoor);
//		this.blocksImmuneToRift.add(DimBlocks.blockRift);
		this.blocksImmuneToRift.add(DimBlocks.transientDoor);
		this.blocksImmuneToRift.add(DimBlocks.goldenDimensionalDoor);
		this.blocksImmuneToRift.add(DimBlocks.goldenDoor);
		this.blocksImmuneToRift.add(DimBlocks.personalDimDoor);
		this.blocksImmuneToRift.add(Blocks.LAPIS_BLOCK);
		this.blocksImmuneToRift.add(Blocks.IRON_BLOCK);
		this.blocksImmuneToRift.add(Blocks.GOLD_BLOCK);
		this.blocksImmuneToRift.add(Blocks.DIAMOND_BLOCK);
		this.blocksImmuneToRift.add(Blocks.EMERALD_BLOCK);
	}

	private static void addAdjacentBlocks(BlockPos pos, int distance, HashMap<BlockPos, Integer> pointDistances, Queue<BlockPos> points) {
		BlockPos[] neighbors = new BlockPos[]{pos.add(-1, 0, 0), pos.add(1, 0, 0), pos.down(), pos.up(), pos.add(0, 0, -1), pos.add(0, 0, 1)};
		for (BlockPos neighbor : neighbors) {
			if (!pointDistances.containsKey(neighbor)) {
				pointDistances.put(neighbor, distance + 1);
				points.add(neighbor);
			}
		}
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
		return hitIfLiquid;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return true;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return null;
	}

	//function that regulates how many blocks it eats/ how fast it eats them.
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (DimDoorsConfig.riftGriefingEnabled && !world.isRemote && PocketManager.getLink(new DimensionPos(pos, world.provider.getDimension())) != null) {
			//Randomly decide whether to search for blocks to destroy. This reduces the frequency of search operations,
			//moderates performance impact, and controls the apparent speed of block destruction.
			TileEntity te = world.getTileEntity(pos);
			if (world.rand.nextInt(MAX_BLOCK_SEARCH_CHANCE) < BLOCK_SEARCH_CHANCE && te instanceof TileEntityRift && ((TileEntityRift) te).updateNearestRift()) {
				destroyNearbyBlocks(world, pos, world.rand);
			}
		}
	}

	private void destroyNearbyBlocks(World world, BlockPos pos, Random random) {
		// Find reachable blocks that are vulnerable to rift damage (ignoring air, of course)
		ArrayList<BlockPos> targets = findReachableBlocks(world, pos, BLOCK_DESTRUCTION_RANGE, false);

		// For each block, randomly decide whether to destroy it.
		// The randomness makes it so the destroyed area appears "noisy" if the rift is exposed to a large surface.
		for (BlockPos target : targets) {
			if (random.nextInt(MAX_BLOCK_DESTRUCTION_CHANCE) < BLOCK_DESTRUCTION_CHANCE) {
				dropWorldThread(world.getBlockState(target), world, pos, random);
				world.destroyBlock(target, false);
			}
		}
	}

	private ArrayList<BlockPos> findReachableBlocks(World world, BlockPos pos, int range, boolean includeAir) {
		int searchVolume = (int) Math.pow(2 * range + 1, 3);
		HashMap<BlockPos, Integer> pointDistances = Maps.newHashMap();
		Queue<BlockPos> points = Lists.newLinkedList();
		ArrayList<BlockPos> targets = Lists.newArrayList();

		// Perform a breadth-first search outwards from the point at which the rift is located.
		// Record the distances of the points we visit to stop the search at its maximum range.
		pointDistances.put(pos, 0);
		addAdjacentBlocks(pos, 0, pointDistances, points);
		while (!points.isEmpty()) {
			BlockPos current = points.remove();
			int distance = pointDistances.get(current);

			// If the current block is air, continue searching. Otherwise, add the block to our list.
			if (world.isAirBlock(current)) {
				if (includeAir) {
					targets.add(current);
				}
				// Make sure we stay within the search range
				if (distance < BLOCK_DESTRUCTION_RANGE) {
					addAdjacentBlocks(current, distance, pointDistances, points);
				}
			} else {
				// Check if the current block is immune to destruction by rifts. If not, add it to our list.
				if (!isBlockImmune(world, current)) {
					targets.add(current);
				}
			}
		}
		return targets;
	}

	public void dropWorldThread(IBlockState state, World world, BlockPos pos, Random random) {
		if (!world.isAirBlock(pos) && (random.nextInt(MAX_WORLD_THREAD_DROP_CHANCE) < DimDoorsConfig.worldThreadDropChance) && !(state.getBlock() instanceof BlockLiquid || state.getBlock() instanceof IFluidBlock)) {
			ItemStack thread = new ItemStack(DimItems.itemWorldThread, 1);
			world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), thread));
		}
	}

	public boolean spreadRift(NewDimData dimension, DimLink parent, World world, Random random) {
		DimensionPos source = parent.source();

		// Find reachable blocks that are vulnerable to rift damage and include air
		ArrayList<BlockPos> targets = findReachableBlocks(world, source, RIFT_SPREAD_RANGE, true);

		if (!targets.isEmpty()) {
			// Choose randomly from among the possible locations where we can spawn a new rift
			BlockPos target = targets.get(random.nextInt(targets.size()));

			// Create a child, replace the block with a rift, and consider dropping World Thread
			IBlockState state = world.getBlockState(target);
			/*if (world.setBlockState(target, DimBlocks.blockRift.getDefaultState())) {
				dimension.createChildLink(target, parent);
				dropWorldThread(state, world, target, random);
				return true;
			}*/ // TODO
		}
		return false;
	}

	/**
	 * Lets pistons push through rifts, destroying them
	 */
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	/**
	 * regulates the render effect, especially when multiple rifts start to link up. Has 3 main parts- Grows toward and away from nearest rft, bends toward it, and a randomization function
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		List<BlockPos> targets = findReachableBlocks(world, pos, 2, false);
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileEntityRift))
			return;

		//renders an extra little blob on top of the actual rift location so its easier to find. Eventually will only render if the player has the goggles.
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new GoggleRiftFX(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, rand.nextGaussian() * 0.01D, rand.nextGaussian() * 0.01D, rand.nextGaussian() * 0.01D, FMLClientHandler.instance().getClient().effectRenderer));

		if (((TileEntityRift) tile).shouldClose) {
			//renders an opposite color effect if it is being closed by the rift remover
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(new ClosingRiftFX(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, rand.nextGaussian() * 0.01D, rand.nextGaussian() * 0.01D, rand.nextGaussian() * 0.01D, FMLClientHandler.instance().getClient().effectRenderer));

		}

	}

	public boolean tryPlacingRift(World world, BlockPos pos) {
		/*if (world != null && !isBlockImmune(world, pos)) {
			return world.setBlockState(pos, DimBlocks.blockRift.getDefaultState());
		}*/ //TODO
		return false;
	}

	public boolean isBlockImmune(World world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		// SenseiKiwi: I've switched to using the block's blast resistance instead of its
		// hardness since most defensive blocks are meant to defend against explosions and
		// may have low hardness to make them easier to build with. However, block.getExplosionResistance()
		// is designed to receive an entity, the source of the blast. We have no entity so
		// I've set this to access blockResistance directly. Might need changing later.

		return (block.blockResistance >= MIN_IMMUNE_RESISTANCE || modBlocksImmuneToRift.contains(block) || blocksImmuneToRift.contains(block));
	}

	public boolean isModBlockImmune(World world, BlockPos pos) {
		// Check whether the block at the specified location is one of the
		// rift-resistant blocks from DD.
		Block block = world.getBlockState(pos).getBlock();
		return modBlocksImmuneToRift.contains(block);
	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.AIR;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityRift();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		// This function runs on the server side after a block is replaced
		// We MUST call super.breakBlock() since it involves removing tile entities
		super.breakBlock(world, pos, state);

		// Schedule rift regeneration for this block if it was changed
		if (world.getBlockState(pos) != state) {
			RiftRegenerator.scheduleSlowRegeneration(pos, world);
		}
	}
}