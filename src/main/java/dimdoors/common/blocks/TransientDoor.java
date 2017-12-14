package dimdoors.common.blocks;


import dimdoors.common.core.DDTeleporter;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.util.DimensionPos;
import dimdoors.registry.DimBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TransientDoor extends BaseDimDoor {

	public TransientDoor(Material material) {
		super(material);
	}

	@Override
	public void enterDimDoor(World world, BlockPos pos, Entity entity) {
		// We need to ignore particle entities
		if (world.isRemote) {
			return;
		}
		IBlockState state = world.getBlockState(pos.down());
		// Check that this is the top block of the door
		if (state.getBlock() == this) {
			boolean canUse = true;
			if (canUse && entity instanceof EntityPlayer) {
				// Don't check for non-living entities since it might not work right
				canUse = BaseDimDoor.isEntityFacingDoor(state, (EntityLivingBase) entity);
			}
			if (canUse) {
				// Teleport the entity through the link, if it exists
				DimLink link = PocketManager.getLink(new DimensionPos(pos, world.provider.getDimension()));
				if (link != null) {
					if (link.linkType() != LinkType.PERSONAL || entity instanceof EntityPlayer) {
						DDTeleporter.traverseDimDoor(world, link, entity, this);
						// Turn the door into a rift AFTER teleporting the player.
						// The door's orientation may be necessary for the teleport.
//						world.setBlockState(pos, DimBlocks.blockRift.getDefaultState()); TODO
						world.setBlockToAir(pos.down());
					}
				}
			}
		} else if (world.getBlockState(pos.up()).getBlock() == this) {
			enterDimDoor(world, pos.up(), entity);
		}
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
		return null;
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return null;
	}

}