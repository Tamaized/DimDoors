package dimdoors.common.items;


import dimdoors.DimDoors;
import dimdoors.common.blocks.IDimDoor;
import dimdoors.common.core.DDLock;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.PocketManager;
import dimdoors.common.watcher.ClientLinkData;
import dimdoors.registry.DimSounds;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemDDKey extends Item {
	public static final int TIME_TO_UNLOCK = 30;

	public ItemDDKey() {
		super();
		//		this.setCreativeTab(mod_pocketDim.dimDoorsCreativeTab); TODO
		this.setMaxStackSize(1);

	}

	@Override
	public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (DDLock.hasCreatedLock(stack)) {
			tooltip.add(I18n.format(DimDoors.modid + ".info.riftkey.bound"));
		} else {
			tooltip.add(I18n.format(DimDoors.modid + ".info.riftkey.unbound"));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack) {
		return !DDLock.hasCreatedLock(par1ItemStack);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		player.setActiveHand(hand);
		return EnumActionResult.PASS;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if (world.isRemote) {
			return EnumActionResult.PASS;
		}

		if (!player.inventory.getCurrentItem().isEmpty()) {
			return EnumActionResult.SUCCESS;
		}
		Block block = world.getBlockState(pos).getBlock();
		// make sure we are dealing with a door
		if (!(block instanceof IDimDoor)) {
			return EnumActionResult.PASS;
		}

		DimLink link = PocketManager.getLink(pos, world);
		// dont do anything to doors without links
		if (link == null) {
			return EnumActionResult.PASS;
		}

		ItemStack stack = player.getHeldItem(hand);

		// what to do if the door has a lock already
		if (link.hasLock()) {
			if (link.doesKeyUnlock(stack)) {
				if (link.getLockState()) {
					world.playSound(null, player.getPosition(), DimSounds.keyunlock, SoundCategory.PLAYERS, 1F, 1F);
				} else {
					world.playSound(null, player.getPosition(), DimSounds.keylock, SoundCategory.PLAYERS, 1F, 1F);
				}
				PocketManager.getDimensionData(world).lock(link, !link.getLockState());
				PocketManager.getLinkWatcher().update(new ClientLinkData(link));

			} else {
				world.playSound(null, player.getPosition(), DimSounds.doorlocked, SoundCategory.PLAYERS, 1F, 1F);
			}
		} else {
			if (!DDLock.hasCreatedLock(stack)) {
				world.playSound(null, player.getPosition(), DimSounds.keylock, SoundCategory.PLAYERS, 1F, 1F);
				PocketManager.getDimensionData(world).createLock(link, stack, world.rand.nextInt(Integer.MAX_VALUE));
				PocketManager.getLinkWatcher().update(new ClientLinkData(link));
			}
		}
		return EnumActionResult.PASS;
	}

	/**
	 * Handle removal of locks here
	 */
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft) {
		if (!(entityLiving instanceof EntityPlayer))
			return;
		EntityPlayer player = (EntityPlayer) entityLiving;
		int j = this.getMaxItemUseDuration(stack) - timeLeft;
		if (j >= TIME_TO_UNLOCK) {
			//Raytrace to make sure we are still looking at a door
			RayTraceResult pos = rayTrace(world, player, true);
			if (pos != null && pos.typeOfHit == RayTraceResult.Type.BLOCK) {
				//make sure we have a link and it has a lock
				DimLink link = PocketManager.getLink(pos.getBlockPos(), world);
				if (link != null && link.hasLock()) {
					//make sure the given key is able to access the lock
					if (link.doesKeyUnlock(stack) && !world.isRemote) {
						PocketManager.getDimensionData(world).removeLock(link, stack);
						world.playSound(null, player.getPosition(), DimSounds.doorlockremoved, SoundCategory.PLAYERS, 1F, 1F);

					}
				}
			}
		}
		player.resetActiveHand();

	}

	/**
	 * Raytrace to make sure we are still looking at the right block while preparing to remove the lock
	 */
	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		// no need to check every tick, twice a second instead
		if (count % 10 == 0) {
			RayTraceResult pos = player instanceof EntityPlayer ? rayTrace(player.world, (EntityPlayer) player, true) : null;
			if (pos != null && pos.typeOfHit == RayTraceResult.Type.BLOCK) {
				DimLink link = PocketManager.getLink(pos.getBlockPos(), player.world);
				if (link != null && link.hasLock()) {
					if (link.doesKeyUnlock(stack)) {
						return;
					}
				}
			}
			player.resetActiveHand();
		}
	}

	@Nonnull
	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
		return EnumAction.BOW;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
		return 72000;
	}
}
