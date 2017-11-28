package dimdoors.common.items;

import dimdoors.common.blocks.BaseDimDoor;
import dimdoors.registry.DimBlocks;
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

	public ItemDimensionalDoor(Block block, ItemDoor door) {
		super(block, door);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("dimdoor.info.dimDoor"));
	}

	@Override
	protected BaseDimDoor getDoorBlock() {
		return (BaseDimDoor) DimBlocks.dimensionalDoor;
	}
}