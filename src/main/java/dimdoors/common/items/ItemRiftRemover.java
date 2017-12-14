package dimdoors.common.items;


import dimdoors.DimDoors;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.entity.EntityRift;
import dimdoors.common.tileentity.TileEntityRift;
import dimdoors.registry.DimBlocks;
import dimdoors.registry.DimSounds;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemRiftRemover extends Item {

	public ItemRiftRemover() {
		super();
		this.setMaxStackSize(1);
		this.setCreativeTab(DimDoors.CREATIVE_TAB);
		this.setMaxDamage(4);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		// We invoke PlayerControllerMP.onPlayerRightClick() from here so that Minecraft
		// will invoke onItemUseFirst() on the client side. We'll tell it to pass the
		// request to the server, which will make sure that rift-related changes are
		// reflected on the server.

		if (!world.isRemote)
			return ActionResult.newResult(EnumActionResult.PASS, stack);

		RayTraceResult hit = rayTrace(world, player, true);
		if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = hit.getBlockPos();
			NewDimData dimension = PocketManager.createDimensionData(world);
			DimLink link = dimension.getLink(pos);
			if (EntityRift.isThereARiftAt(world, pos) && link != null && player.canPlayerEdit(pos, hit.sideHit, stack)) {
				// Invoke onPlayerRightClick()
				FMLClientHandler.instance().getClient().playerController.processRightClickBlock((EntityPlayerSP) player, (WorldClient) world, pos, hit.sideHit, hit.hitVec, hand);
			}
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos blockpos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {

		// We want to use onItemUseFirst() here so that this code will run on the server side,
		// so we don't need the client to send link-related updates to the server. Still,
		// check whether we have a rift in sight before passing the request over.

		// On integrated servers, the link won't be removed immediately because of the rift
		// removal animation. That means we'll have a chance to check for the link before
		// it's deleted. Otherwise the Rift Remover's durability wouldn't drop.
		RayTraceResult hit = this.rayTrace(world, player, true);
		if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) { // TODO: NEEDS TO BE AN ENTITY CHECK
			BlockPos pos = hit.getBlockPos();
			ItemStack stack = player.getHeldItem(hand);
			NewDimData dimension = PocketManager.createDimensionData(world);
			DimLink link = dimension.getLink(pos);
			if (EntityRift.isThereARiftAt(world, pos) && link != null && player.canPlayerEdit(pos, side, stack)) {
				// Tell the rift's tile entity to do its removal animation
				TileEntity tileEntity = world.getTileEntity(pos);
				if (tileEntity != null && tileEntity instanceof TileEntityRift) {
					((TileEntityRift) tileEntity).shouldClose = true;
					tileEntity.markDirty();
				} else if (!world.isRemote) {
					// Only set the block to air on the server side so that we don't
					// tell the server to remove the rift block before it can use the
					// Rift Remover. Otherwise, it won't know to reduce durability.
					world.setBlockToAir(pos);
				}
				if (world.isRemote) {
					// Tell the server about this
					return EnumActionResult.FAIL;
				} else {
					if (!player.capabilities.isCreativeMode) {
						stack.damageItem(1, player);
					}
					player.playSound(DimSounds.riftclose, 0.8f, 1);
				}
			}
		}
		return EnumActionResult.SUCCESS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("info.riftRemover"));
	}
}
