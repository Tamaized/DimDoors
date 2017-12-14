package dimdoors.common.blocks;


import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.world.PersonalPocketProvider;
import dimdoors.registry.DimItems;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PersonalDimDoor extends BaseDimDoor {

	public PersonalDimDoor(Material material) {
		super(material);
	}

	@Override
	public void placeLink(World world, BlockPos pos) {
		if (!world.isRemote && world.getBlockState(pos.down()).getBlock() == this) {
			NewDimData dimension = PocketManager.getDimensionData(world);
			DimLink link = dimension.getLink(pos);
			if (link == null) {
				if (world.provider instanceof PersonalPocketProvider)
					dimension.createLink(pos, LinkType.LIMBO, world.getBlockState(pos.down()).getBlock().getMetaFromState(world.getBlockState(pos.down())));
				else
					dimension.createLink(pos, LinkType.PERSONAL, world.getBlockState(pos.down()).getBlock().getMetaFromState(world.getBlockState(pos.down())));
			}
		}
	}

	@Override
	public Item getDoorItem() {
		return DimItems.itemPersonalDoor;
	}

}
