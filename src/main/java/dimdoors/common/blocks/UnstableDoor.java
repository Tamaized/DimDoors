package dimdoors.common.blocks;


import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.registry.DimItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class UnstableDoor extends DimensionalDoor {

	public UnstableDoor(Material material) {
		super(material, DimItems.itemUnstableDoor);
	}

	@Override
	public void placeLink(World world, BlockPos pos) {
		if (!world.isRemote && world.getBlockState(pos.down()).getBlock() == this) {
			NewDimData dimension = PocketManager.getDimensionData(world);
			dimension.createLink(pos, LinkType.RANDOM, world.getBlockState(pos.down()).getBlock().getMetaFromState(world.getBlockState(pos.down())));
		}
	}

	@Nonnull
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.IRON_DOOR;
	}
}