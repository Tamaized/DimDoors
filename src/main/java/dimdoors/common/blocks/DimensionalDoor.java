package dimdoors.common.blocks;


import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DimensionalDoor extends BaseDimDoor {

	private final Item dropItem;

	public DimensionalDoor(Material material, Item item) {
		super(material);
		dropItem = item;
	}

	@Override
	public void placeLink(World world, BlockPos pos) {
		if (!world.isRemote && world.getBlockState(pos.down()).getBlock() == this) {
			NewDimData dimension = PocketManager.createDimensionData(world);
			DimLink link = dimension.getLink(pos);
			if (link == null) {
				dimension.createLink(pos, LinkType.POCKET, world.getBlockState(pos.down()).getBlock().getMetaFromState(world.getBlockState(pos.down())));
			}
		}
	}

	@Override
	public Item getDoorItem() {
		return dropItem;
	}
}