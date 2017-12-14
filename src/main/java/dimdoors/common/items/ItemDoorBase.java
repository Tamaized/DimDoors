package dimdoors.common.items;

import dimdoors.registry.DimBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDoor;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemDoorBase extends ItemDoor {

	private final Block door;

	public ItemDoorBase(Block door) {
		super(door);
		this.setMaxStackSize(16);
		this.door = door;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (facing != EnumFacing.UP) {
			return EnumActionResult.FAIL;
		} else {
			if (player.canPlayerEdit(pos.up(), facing, player.getHeldItem(hand)) && player.canPlayerEdit(pos.up(2), facing, player.getHeldItem(hand))) {
				if (!door.canPlaceBlockAt(world, pos.up())) {
					return EnumActionResult.FAIL;
				} else {
					EnumFacing i1 = EnumFacing.fromAngle(player.rotationYaw);
					placeDoor(world, pos.up(), i1, door, false);
					player.getHeldItem(hand).shrink(1);
					return EnumActionResult.SUCCESS;
				}
			} else {
				return EnumActionResult.FAIL;
			}
		}
	}
}
