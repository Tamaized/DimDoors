package dimdoors.common.items;


import dimdoors.DimDoors;
import dimdoors.common.blocks.BaseDimDoor;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.entity.EntityRift;
import dimdoors.common.util.DimensionPos;
import dimdoors.registry.DimSounds;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemRiftSignature extends Item {

	public ItemRiftSignature() {
		super();
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.hasSubtypes = true;
		this.setCreativeTab(DimDoors.CREATIVE_TAB);
	}

	/**
	 * Makes the rift placement account for replaceable blocks and doors.
	 */
	public static int adjustYForSpecialBlocks(World world, BlockPos pos) {
		pos = pos.down(2); // Get the block the player actually clicked on
		Block block = world.getBlockState(pos).getBlock();
		if (block == Blocks.AIR) {
			return pos.getY() + 2;
		}
		if (block.isReplaceable(world, pos)) {
			return pos.getY() + 1; // Move block placement down (-2+1) one so its directly over things like snow
		}
		if (block instanceof BaseDimDoor) {
			if (((BaseDimDoor) block).isUpperDoorBlock(world.getBlockState(pos))) {
				return pos.getY(); // Move rift placement down two so its in the right place on the door.
			}
			// Move rift placement down one so its in the right place on the door.
			return pos.getY() + 1;
		}
		return pos.getY() + 2;
	}

	public static void setSource(ItemStack itemStack, BlockPos pos, int orientation, NewDimData dimension) {
		NBTTagCompound tag = new NBTTagCompound();

		tag.setInteger("linkX", pos.getX());
		tag.setInteger("linkY", pos.getY());
		tag.setInteger("linkZ", pos.getZ());
		tag.setInteger("orientation", orientation);
		tag.setInteger("linkDimID", dimension.id());

		itemStack.setTagCompound(tag);
		itemStack.setItemDamage(1);
	}

	public static void clearSource(ItemStack itemStack) {
		//Don't just set the tag to null since there may be other data there (e.g. for renamed items)
		NBTTagCompound tag = itemStack.getTagCompound();
		tag.removeTag("linkX");
		tag.removeTag("linkY");
		tag.removeTag("linkZ");
		tag.removeTag("orientation");
		tag.removeTag("linkDimID");
		itemStack.setItemDamage(0);
	}

	public static Point4DOrientation getSource(ItemStack itemStack) {
		if (itemStack.getItemDamage() != 0) {
			if (itemStack.hasTagCompound()) {
				NBTTagCompound tag = itemStack.getTagCompound();

				int x = tag.getInteger("linkX");
				int y = tag.getInteger("linkY");
				int z = tag.getInteger("linkZ");
				int orientation = tag.getInteger("orientation");
				int dimID = tag.getInteger("linkDimID");

				return new Point4DOrientation(x, y, z, orientation, dimID);
			}
			// Mark the item as uninitialized if its source couldn't be read
			itemStack.setItemDamage(0);
		}
		return null;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		//Make the item glow if it has one endpoint stored
		return (stack.getItemDamage() != 0);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		// We must use onItemUseFirst() instead of onItemUse() because Minecraft checks
		// whether the user is in creative mode after calling onItemUse() and undoes any
		// damage we might set to indicate the rift sig has been activated. Otherwise,
		// we would need to rely on checking NBT tags for hasEffect() and that function
		// gets called constantly. Avoiding NBT lookups reduces our performance impact.

		// Return false on the client side to pass this request to the server
		if (world.isRemote) {
			return EnumActionResult.PASS;
		}

		//Increase y by 2 to place the rift at head level
		pos = pos.up(adjustYForSpecialBlocks(world, pos.up(2)));
		ItemStack stack = player.getHeldItem(hand);
		if (!player.canPlayerEdit(pos, side, stack)) {
			return EnumActionResult.SUCCESS;
		}
		Point4DOrientation source = getSource(stack);
		int orientation = MathHelper.floor(((player.rotationYaw + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3;
		if (source != null) {
			// The link was used before and already has an endpoint stored.
			// Create links connecting the two endpoints.
			NewDimData sourceDimension = PocketManager.getDimensionData(source.getDimension());
			NewDimData destinationDimension = PocketManager.getDimensionData(world);

			DimLink link = sourceDimension.createLink(source.getX(), source.getY(), source.getZ(), LinkType.NORMAL, source.getOrientation());
			DimLink reverse = destinationDimension.createLink(pos, LinkType.NORMAL, orientation);

			destinationDimension.setLinkDestination(link, pos);
			sourceDimension.setLinkDestination(reverse, source.getX(), source.getY(), source.getZ());

			// Try placing a rift at the destination point
			tryPlacingRift(world, pos);

			// Try placing a rift at the source point
			// We don't need to check if sourceWorld is null - that's already handled.
			World sourceWorld = DimensionManager.getWorld(sourceDimension.id());
			tryPlacingRift(sourceWorld, new BlockPos(source.getX(), source.getY(), source.getZ()));

			if (!player.capabilities.isCreativeMode) {
				stack.shrink(1);
			}
			clearSource(stack);
			//			mod_pocketDim.sendChat(player, "Rift Created"); TODO
			player.playSound(DimSounds.riftend, 0.6F, 1.0F);
		} else {
			//The link signature has not been used. Store its current target as the first location.
			setSource(stack, pos, orientation, PocketManager.createDimensionData(world));
			//			mod_pocketDim.sendChat(player, ("Location Stored in Rift Signature")); TODO
			player.playSound(DimSounds.riftstart, 0.6F, 1.0F);
		}
		return EnumActionResult.SUCCESS;
	}

	public boolean tryPlacingRift(World world, BlockPos pos) {
		if (world != null/* && !isBlockImmune(world, x, y, z)*/) { // TODO
			if (!EntityRift.isThereARiftAt(world, pos)) {
				world.spawnEntity(new EntityRift(world, pos));
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		Point4DOrientation source = getSource(stack);
		if (source != null)
			tooltip.add(I18n.format("info.riftSignature.bound", source.getX(), source.getY(), source.getZ(), source.getDimension()));
		else
			tooltip.add(I18n.format("info.riftSignature.unbound"));
	}

	static class Point4DOrientation {
		private DimensionPos point;
		private int orientation;

		Point4DOrientation(int x, int y, int z, int orientation, int dimID) {
			this.point = new DimensionPos(x, y, z, dimID);
			this.orientation = orientation;
		}

		int getX() {
			return point.getX();
		}

		int getY() {
			return point.getY();
		}

		int getZ() {
			return point.getZ();
		}

		int getDimension() {
			return point.getDimension();
		}

		int getOrientation() {
			return orientation;
		}

		DimensionPos getPoint() {
			return point;
		}
	}
}

