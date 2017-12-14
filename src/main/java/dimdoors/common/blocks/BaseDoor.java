package dimdoors.common.blocks;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class BaseDoor extends BlockDoor {

	private final Item dropItem;

	public BaseDoor(Material material, Item item) {
		super(material);
		dropItem = item;
	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
		return new ItemStack(dropItem);
	}

	@Nonnull
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return isUpperDoorBlock(state) ? Items.AIR : dropItem;
	}

	@Nonnull
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(dropItem);
	}

	private boolean isUpperDoorBlock(IBlockState state) {
		return state.getValue(HALF) == EnumDoorHalf.UPPER;
	}

}
