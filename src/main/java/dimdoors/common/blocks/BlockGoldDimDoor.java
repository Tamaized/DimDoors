package dimdoors.common.blocks;


import dimdoors.common.tileentity.TileEntityDimDoorGold;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockGoldDimDoor extends DimensionalDoor {

	public BlockGoldDimDoor(Material material, Item item) {
		super(material, item);
	}

	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityDimDoorGold();
	}

}
