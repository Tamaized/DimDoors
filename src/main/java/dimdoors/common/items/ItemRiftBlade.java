package dimdoors.common.items;


import com.google.common.collect.Sets;
import dimdoors.DimDoors;
import dimdoors.common.core.PocketManager;
import dimdoors.common.entity.EntityRift;
import dimdoors.common.helpers.RayTraceHelper;
import dimdoors.registry.DimBlocks;
import dimdoors.registry.DimItems;
import dimdoors.registry.DimSounds;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemRiftBlade extends ItemSword {

	public ItemRiftBlade() {
		super(ToolMaterial.DIAMOND);
		this.setCreativeTab(DimDoors.CREATIVE_TAB);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack) {
		return true;
	}

	@Override
	protected RayTraceResult rayTrace(World world, EntityPlayer player, boolean useLiquids) {
		float var4 = 1.0F;
		float var5 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * var4;
		float var6 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * var4;
		double var7 = player.prevPosX + (player.posX - player.prevPosX) * var4;
		double var9 = player.prevPosY + (player.posY - player.prevPosY) * var4 + 1.62D - player.getYOffset();
		double var11 = player.prevPosZ + (player.posZ - player.prevPosZ) * var4;
		Vec3d var13 = new Vec3d(var7, var9, var11);
		float var14 = MathHelper.cos(-var6 * 0.017453292F - (float) Math.PI);
		float var15 = MathHelper.sin(-var6 * 0.017453292F - (float) Math.PI);
		float var16 = -MathHelper.cos(-var5 * 0.017453292F);
		float var17 = MathHelper.sin(-var5 * 0.017453292F);
		float var18 = var15 * var16;
		float var20 = var14 * var16;
		double var21 = 5.0D;
		if (player instanceof EntityPlayerMP) {
			var21 = 7;
		}
		Vec3d var23 = var13.addVector(var18 * var21, var17 * var21, var20 * var21);
		return world.rayTraceBlocks(var13, var23, true);
	}

	private boolean teleportToEntity(ItemStack item, Entity par1Entity, EntityPlayer holder) {
		Vec3d var2 = new Vec3d(holder.posX - par1Entity.posX, holder.getEntityBoundingBox().minY + holder.height / 2.0F - par1Entity.posY + par1Entity.getEyeHeight(), holder.posZ - par1Entity.posZ);

		double cooef = (var2.lengthVector() - 2.5) / var2.lengthVector();
		var2 = var2.scale(cooef);
		double var5 = holder.posX - var2.x;
		double var9 = holder.posZ - var2.z;


		double var7 = MathHelper.floor(holder.posY - var2.y);

		int var14 = MathHelper.floor(var5);
		int var15 = MathHelper.floor(var7);
		int var16 = MathHelper.floor(var9);
		while (!holder.world.isAirBlock(new BlockPos(var14, var15, var16))) {
			var15++;
		}
		var7 = var15;

		holder.setPositionAndUpdate(var5, var7, var9);
		holder.world.playSound(null, holder.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);

		return true;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote) {
			RayTraceResult raytrace = RayTraceHelper.tracePath(world, player, 10, 1, Sets.newHashSet(player));
			if (raytrace != null && raytrace.typeOfHit == RayTraceResult.Type.ENTITY) {
				if (raytrace.entityHit instanceof EntityRift) {
					BlockPos pos = new BlockPos(raytrace.entityHit);
					if (PocketManager.getLink(pos, world) != null) {
						Vec3d[] vecs = RayTraceHelper.getPlayerTraceVec(player, 10);
						RayTraceResult facetrace = raytrace.entityHit.getEntityBoundingBox().calculateIntercept(vecs[0], vecs[1]);
						if (facetrace == null)
							return ActionResult.newResult(EnumActionResult.PASS, stack);
						raytrace.sideHit = facetrace.sideHit;
						if (player.canPlayerEdit(pos, raytrace.sideHit, stack) && player.canPlayerEdit(pos.up(), raytrace.sideHit, stack)) {
							int orientation = MathHelper.floor((player.rotationYaw + 180.0F) * 4.0F / 360.0F - 0.5D) & 3;

							if (BaseItemDoor.canPlace(world, pos) && BaseItemDoor.canPlace(world, pos.down())) {
								ItemDimensionalDoor.placeDoor(world, pos.down(), EnumFacing.VALUES[orientation], DimBlocks.transientDoor, false);
								player.playSound(DimSounds.riftdoor, 0.6F, 1.0F);
								stack.damageItem(3, player);
								return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
							}
						}
					}
				} else if (raytrace.entityHit instanceof EntityLivingBase) {
					if (player.canEntityBeSeen(raytrace.entityHit)) {
						teleportToEntity(stack, raytrace.entityHit, player);
						stack.damageItem(3, player);
						return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
					}
				}
			}

			player.setActiveHand(hand);
		}
		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	/**
	 * Return whether this item is repairable in an anvil.
	 */
	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, @Nonnull ItemStack par2ItemStack) {
		//Don't include a call to super.getIsRepairable()!
		//That would cause this sword to accept diamonds as a repair material (since we set material = Diamond).
		return DimItems.itemStableFabric == par2ItemStack.getItem();
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format(DimDoors.modid + ".info.riftblade"));
	}
}
