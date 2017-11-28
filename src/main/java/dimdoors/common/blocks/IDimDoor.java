package dimdoors.common.blocks;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IDimDoor { // TODO look over this entire interface, I really don't think it's needed

	/**
	 * A function to enter a dim door and traverse its link, called when a player collides with an open door
	 *
	 * @param world
	 * @param pos
	 * @param entity
	 */
	public void enterDimDoor(World world, BlockPos pos, Entity entity);

	/**
	 * called when a door is placed to determine how it will place a link
	 *
	 * @param world
	 * @param pos
	 */
	public void placeLink(World world, BlockPos pos);

	public Item getDoorItem();

	public TileEntity initDoorTE(World world, BlockPos pos);

	/**
	 * checks if any of this doors blocks are overlapping with a rift
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public boolean isDoorOnRift(World world, BlockPos pos);

}
