package dimdoors.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockUnraveledFabric extends Block {
	//	private final LimboDecay decay; TODO

	public BlockUnraveledFabric(/*, LimboDecay decay*/) // TODO
	{
		super(Material.GROUND);
		//		this.decay = decay;
		this.setTickRandomly(true);
		//		this.setCreativeTab(DimTabs.dimDoorsCreativeTab); TODO
	}

	/**
	 * If the block is in Limbo, attempt to decay surrounding blocks upon receiving a random update tick.
	 */
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		//Make sure this block is in Limbo
		/*if (world.provider.dimensionId == limboDimensionID) TODO
		{
    		decay.applySpreadDecay(world, x, y, z);
    	}*/
	}
}
