package dimdoors.common.world.fortresses;


import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.registry.DimBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemDoor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.List;
import java.util.Random;

public class ComponentNetherGateway extends StructureComponent {
	// Note: In this case, it doesn't really matter which class we extend, since this class will
	// never be passed to Minecraft. We just need an instance to have access to structure-building methods.
	// If Forge supports adding custom fortress structures in the future, then we might have to change
	// our class to extend ComponentNetherBridgeCrossing or something along those lines. ~SenseiKiwi

	public ComponentNetherGateway(int componentType, Random random, StructureBoundingBox bounds, EnumFacing coordBaseMode) {
		super(componentType);

		this.boundingBox = bounds;
		setCoordBaseMode(coordBaseMode);
	}

	/**
	 * Creates and returns a new component piece. Or null if it could not find enough room to place it.
	 */
	public static ComponentNetherGateway createValidComponent(List components, Random random, int minX, int minY, int minZ, EnumFacing coordBaseMode, int componentType) {
		StructureBoundingBox bounds = StructureBoundingBox.getComponentToAddBoundingBox(minX, minY, minZ, -2, 0, 0, 7, 9, 7, coordBaseMode);
		return isAboveGround(bounds) && StructureComponent.findIntersecting(components, bounds) == null ? new ComponentNetherGateway(componentType, random, bounds, coordBaseMode) : null;
	}

	public static ComponentNetherGateway createFromComponent(StructureComponent component, Random random) {
		// Create an instance of our gateway component using the same data as another component,
		// likely a component that we intend to replace during generation
		return new ComponentNetherGateway(component.getComponentType(), random, component.getBoundingBox(), getCoordBaseMode(component));
	}

	private static EnumFacing getCoordBaseMode(StructureComponent component) {
		// This is a hack to get the value of a component's coordBaseMode field.
		// It's essentially the orientation of the component... with a weird name.
		int i = component.createStructureBaseNBT().getInteger("O");
		return i == -1 ? null : EnumFacing.getHorizontal(i);
	}

	/**
	 * Checks if the bounding box's minY is > 10
	 */
	protected static boolean isAboveGround(StructureBoundingBox par0StructureBoundingBox) {
		return par0StructureBoundingBox != null && par0StructureBoundingBox.minY > 10;
	}

	@Override
	protected void writeStructureToNBT(NBTTagCompound tagCompound) {

	}

	@Override
	protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {

	}

	/**
	 * second Part of Structure generating, this for example places Spiderwebs, Mob Spawners, it closes Mineshafts at
	 * the end, it adds Fences...
	 */
	@Override
	public boolean addComponentParts(World world, Random random, StructureBoundingBox bounds) {
		int NETHER_SLAB_METADATA = 6;

		// Set all the blocks in the area of the room to air
		this.fillWithBlocks(world, bounds, 0, 2, 0, 6, 6, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
		// Set up the platform under the gateway
		this.fillWithBlocks(world, bounds, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICK.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);

		// Build the fence at the back of the room
		this.fillWithBlocks(world, bounds, 1, 2, 6, 5, 2, 6, Blocks.NETHER_BRICK.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);
		this.fillWithBlocks(world, bounds, 1, 3, 6, 5, 3, 6, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

		// Build the fences at the sides of the room
		this.fillWithBlocks(world, bounds, 0, 2, 0, 0, 2, 6, Blocks.NETHER_BRICK.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);
		this.fillWithBlocks(world, bounds, 0, 3, 0, 0, 3, 6, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

		this.fillWithBlocks(world, bounds, 6, 2, 0, 6, 2, 6, Blocks.NETHER_BRICK.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);
		this.fillWithBlocks(world, bounds, 6, 3, 0, 6, 3, 6, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

		// Build the fence portions closest to the entrance
		this.setBlockState(world, Blocks.NETHER_BRICK.getDefaultState(), 0, 1, 2, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 1, 3, 0, bounds);

		this.setBlockState(world, Blocks.NETHER_BRICK.getDefaultState(), 5, 2, 0, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 5, 3, 0, bounds);

		// Build the first layer of the gateway
		this.fillWithBlocks(world, bounds, 1, 2, 2, 5, 2, 5, Blocks.NETHER_BRICK.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);
		this.fillWithBlocks(world, bounds, 1, 2, 1, 5, 2, 1, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.NETHERBRICK), Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.NETHERBRICK), false);

		this.setBlockState(world, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.NETHERBRICK), 1, 2, 2, bounds);
		this.setBlockState(world, Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.NETHERBRICK), 5, 2, 2, bounds);

		// Build the second layer of the gateway

		EnumFacing orientation = EnumFacing.VALUES[2];
		this.fillWithBlocks(world, bounds, 2, 3, 3, 2, 3, 4, Blocks.NETHER_BRICK.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);
		this.fillWithBlocks(world, bounds, 4, 3, 3, 4, 3, 4, Blocks.NETHER_BRICK.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);
		this.setBlockState(world, Blocks.NETHER_BRICK.getDefaultState(), 3, 3, 4, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, orientation), 3, 3, 5, bounds);

		// Build the third layer of the gateway
		// We add 4 to get the rotated metadata for upside-down stairs
		// because Minecraft only supports metadata rotations for normal stairs -_-
		this.fillWithBlocks(world, bounds, 2, 4, 4, 4, 4, 4, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, orientation), Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, orientation), false);

		this.setBlockState(world, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.VALUES[0]), 2, 4, 3, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.VALUES[1]), 4, 4, 3, bounds);

		// Build the fourth layer of the gateway
		this.setBlockState(world, Blocks.NETHER_BRICK.getDefaultState(), 3, 5, 3, bounds);

		this.setBlockState(world, Blocks.NETHERRACK.getDefaultState(), 2, 5, 3, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.VALUES[0]), 1, 5, 3, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.VALUES[3]), 2, 5, 2, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.VALUES[2]), 2, 5, 4, bounds);

		this.setBlockState(world, Blocks.NETHERRACK.getDefaultState(), 4, 5, 3, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.VALUES[1]), 5, 5, 3, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.VALUES[3]), 4, 5, 2, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.VALUES[2]), 4, 5, 4, bounds);

		// Build the top layer of the gateway
		this.setBlockState(world, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 3, 6, 3, bounds);

		this.setBlockState(world, Blocks.FIRE.getDefaultState(), 2, 6, 3, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 1, 6, 3, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 2, 6, 2, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 2, 6, 4, bounds);

		this.setBlockState(world, Blocks.FIRE.getDefaultState(), 4, 6, 3, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 5, 6, 3, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 4, 6, 2, bounds);
		this.setBlockState(world, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 4, 6, 4, bounds);

		// Place the transient door
		int y = this.getYWithOffset(3);
		int x = this.getXWithOffset(3, 3);
		int z = this.getZWithOffset(3, 3);
		DimLink link;
		NewDimData dimension;

		// This function might run multiple times for a single component
		// due to the way Minecraft handles structure generation!
		if (bounds.isVecInside(new Vec3i(x, y, z)) && bounds.isVecInside(new Vec3i(x, y + 1, z))) {
			orientation = EnumFacing.VALUES[1];
			dimension = PocketManager.createDimensionData(world);
			link = dimension.getLink(x, y + 1, z);
			if (link == null) {
				link = dimension.createLink(x, y + 1, z, LinkType.DUNGEON, orientation.ordinal());
			}
			ItemDoor.placeDoor(world, new BlockPos(x, y, z), orientation, DimBlocks.transientDoor, false);
		}

		for (x = 0; x <= 6; ++x) {
			for (z = 0; z <= 6; ++z) {
				this.setBlockState(world, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, bounds);
			}
		}

		return true;
	}
}
