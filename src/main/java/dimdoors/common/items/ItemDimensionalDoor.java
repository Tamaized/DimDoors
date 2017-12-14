package dimdoors.common.items;

import dimdoors.common.blocks.BaseDimDoor;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemDimensionalDoor extends BaseItemDoor {

	private final Block door;

	public ItemDimensionalDoor(Block block, ItemDoor door) {
		super(block, door);
		this.door = block;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("dimdoor.info." + getUnlocalizedName()));
	}

	@Override
	protected BaseDimDoor getDoorBlock() {
		return (BaseDimDoor) door;
	}
}