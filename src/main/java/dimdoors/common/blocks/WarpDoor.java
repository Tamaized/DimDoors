package dimdoors.common.blocks;

import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.registry.DimItems;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WarpDoor extends BaseDimDoor {

	public WarpDoor(Material material) {
		super(material);
	}

	@Override
	public void placeLink(World world, BlockPos pos) {
		if (!world.isRemote && world.getBlockState(pos.down()).getBlock() == this) {
			NewDimData dimension = PocketManager.createDimensionData(world);
			DimLink link = dimension.getLink(pos);
			if (link == null && dimension.isPocketDimension()) {
				dimension.createLink(pos, LinkType.SAFE_EXIT, world.getBlockState(pos.down()).getBlock().getMetaFromState(world.getBlockState(pos.down())));
			}
		}
	}

	@Override
	public Item getDoorItem() {
		return DimItems.itemWarpDoor;
	}
}