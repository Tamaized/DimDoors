package dimdoors.common.items;


import com.google.common.collect.Maps;
import dimdoors.common.blocks.BaseDimDoor;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.PocketManager;
import dimdoors.common.entity.EntityRift;
import dimdoors.common.tileentity.TileEntityDimDoor;
import dimdoors.common.util.DimensionPos;
import dimdoors.registry.DimBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;

public abstract class BaseItemDoor extends ItemDoor {

	// Maps non-dimensional door items to their corresponding dimensional door item
	// Also maps dimensional door items to themselves for simplicity
	private static HashMap<Item, BaseItemDoor> doorItemMapping = Maps.newHashMap();

	public BaseItemDoor(Block block, ItemDoor vanillaDoor) {
		super(block);
		this.setMaxStackSize(64);
		//		this.setCreativeTab(mod_pocketDim.dimDoorsCreativeTab); TODO

		doorItemMapping.put(this, this);
		if (vanillaDoor != null) {
			doorItemMapping.put(vanillaDoor, this);
		}
	}

	public static boolean tryToPlaceDoor(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side) {
		if (world.isRemote) {
			return false;
		}
		// Retrieve the actual door type that we want to use here.
		// It's okay if stack isn't an ItemDoor. In that case, the lookup will
		// return null, just as if the item was an unrecognized door type.
		BaseItemDoor mappedItem = doorItemMapping.get(stack.getItem());
		if (mappedItem == null) {
			return false;
		}
		BaseDimDoor doorBlock = mappedItem.getDoorBlock();
		return BaseItemDoor.placeDoorOnBlock(doorBlock, stack, player, world, pos, side) || BaseItemDoor.placeDoorOnRift(doorBlock, world, player, stack);
	}

	public static boolean placeDoorOnBlock(Block doorBlock, ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side) {
		if (world.isRemote) {
			return false;
		}
		// Only place doors on top of blocks - check if we're targeting the top
		// side
		if (side == EnumFacing.UP) {
			IBlockState state = world.getBlockState(pos);
			if (!world.isAirBlock(pos)) {
				if (!state.getBlock().isReplaceable(world, pos)) {
					pos = pos.up();
				}
			}

			if (canPlace(world, pos) && canPlace(world, pos.up()) && player.canPlayerEdit(pos, side, stack) && (player.canPlayerEdit(pos.up(), side, stack) && stack.getCount() > 0) && ((stack.getItem() instanceof BaseItemDoor) || PocketManager.getLink(pos.up(), world) != null)) {
				EnumFacing facing = EnumFacing.fromAngle(player.rotationYaw);
				placeDoor(world, pos, facing, doorBlock, false);

				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * uses a raytrace to try and place a door on a rift
	 */
	public static boolean placeDoorOnRift(Block doorBlock, World world, EntityPlayer player, ItemStack stack) {
		if (world.isRemote) {
			return false;
		}

		RayTraceResult hit = BaseItemDoor.doRayTrace(player.world, player, true);
		if (hit != null) {
			if (EntityRift.isThereARiftAt(world, hit.getBlockPos())) { //TODO: NEEDS TO BE AN ENTITY RAYTRACE
				DimLink link = PocketManager.getLink(new DimensionPos(hit.getBlockPos(), world.provider.getDimension()));
				if (link != null) {

					if (player.canPlayerEdit(hit.getBlockPos(), hit.sideHit, stack) && player.canPlayerEdit(hit.getBlockPos().down(), hit.sideHit, stack)) {
						if (canPlace(world, hit.getBlockPos()) && canPlace(world, hit.getBlockPos().down())) {
							EnumFacing orientation = EnumFacing.fromAngle(player.rotationYaw);
							placeDoor(world, hit.getBlockPos().down(), orientation, doorBlock, false);
							if (!(stack.getItem() instanceof BaseItemDoor)) {
								TileEntity te = world.getTileEntity(hit.getBlockPos());
								if (te instanceof TileEntityDimDoor)
									((TileEntityDimDoor) te).hasGennedPair = true;
							}
							if (!player.capabilities.isCreativeMode) {
								stack.shrink(1);
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean canPlace(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return (EntityRift.isThereARiftAt(world, pos) || world.isAirBlock(pos) || state.getMaterial().isReplaceable());
	}

	/**
	 * Copied from minecraft Item.class
	 * TODO we probably can improve this
	 */
	protected static RayTraceResult doRayTrace(World par1World, EntityPlayer par2EntityPlayer, boolean par3) {
		float f = 1.0F;
		float f1 = par2EntityPlayer.prevRotationPitch + (par2EntityPlayer.rotationPitch - par2EntityPlayer.prevRotationPitch) * f;
		float f2 = par2EntityPlayer.prevRotationYaw + (par2EntityPlayer.rotationYaw - par2EntityPlayer.prevRotationYaw) * f;
		double d0 = par2EntityPlayer.prevPosX + (par2EntityPlayer.posX - par2EntityPlayer.prevPosX) * (double) f;
		double d1 = par2EntityPlayer.prevPosY + (par2EntityPlayer.posY - par2EntityPlayer.prevPosY) * (double) f + (double) (par1World.isRemote ? par2EntityPlayer.getEyeHeight() - par2EntityPlayer.getDefaultEyeHeight() : par2EntityPlayer.getEyeHeight());
		double d2 = par2EntityPlayer.prevPosZ + (par2EntityPlayer.posZ - par2EntityPlayer.prevPosZ) * (double) f;
		Vec3d vec3 = new Vec3d(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 5.0D;
		if (par2EntityPlayer instanceof EntityPlayerMP) {
			d3 = ((EntityPlayerMP) par2EntityPlayer).interactionManager.getBlockReachDistance();
		}
		Vec3d vec31 = vec3.addVector((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
		return par1World.rayTraceBlocks(vec3, vec31, par3);
	}

	/**
	 * Overriden in subclasses to specify which door block that door item will
	 * place
	 */
	protected abstract BaseDimDoor getDoorBlock();

	/**
	 * Overriden here to remove vanilla block placement functionality from
	 * dimensional doors, we handle this in the EventHookContainer
	 */
	@Nonnull
	@Override
	public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
		return EnumActionResult.FAIL;
	}
}