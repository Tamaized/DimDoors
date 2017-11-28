package dimdoors.common.dungeon;


import dimdoors.common.schematic.WorldOperation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class FillContainersOperation extends WorldOperation {

	private static final int GRAVE_CHEST_CHANCE = 1;
	private static final int MAX_GRAVE_CHEST_CHANCE = 6;
	private Random random;

	public FillContainersOperation(Random random) {
		super("FillContainersOperation");
		this.random = random;
	}

	private static boolean isInventoryEmpty(IInventory inventory) {
		int size = inventory.getSizeInventory();
		for (int index = 0; index < size; index++) {
			if (!inventory.getStackInSlot(index).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean applyToBlock(World world, int x, int y, int z) {
		Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();

		// Fill empty chests and dispensers
		if (block instanceof BlockContainer) {
			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

			// Fill chests
			if (tileEntity instanceof TileEntityChest) {
				TileEntityChest chest = (TileEntityChest) tileEntity;
				if (isInventoryEmpty(chest)) {
					// Randomly choose whether this will be a regular dungeon chest or a grave chest
					if (random.nextInt(MAX_GRAVE_CHEST_CHANCE) < GRAVE_CHEST_CHANCE) {
						//						DDLoot.fillGraveChest(chest, random, properties); TODO
					} else {
						//						DDLoot.generateChestContents(DDLoot.DungeonChestInfo, chest, random); TODO
					}
				}
			}
		}
		return true;
	}
}
