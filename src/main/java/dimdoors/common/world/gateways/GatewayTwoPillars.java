package dimdoors.common.world.gateways;

import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GatewayTwoPillars extends BaseSchematicGateway {

	private static final int GATEWAY_RADIUS = 4;

	public GatewayTwoPillars() {
		super();
	}

	@Override
	protected void generateRandomBits(World world, int x, int y, int z) {
		final IBlockState state = Blocks.BRICK_BLOCK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.DEFAULT);

		//Replace some of the ground around the gateway with bricks
		for (int xc = -GATEWAY_RADIUS; xc <= GATEWAY_RADIUS; xc++) {
			for (int zc = -GATEWAY_RADIUS; zc <= GATEWAY_RADIUS; zc++) {
				//Check that the block is supported by an opaque block.
				//This prevents us from building over a cliff, on the peak of a mountain,
				//or the surface of the ocean or a frozen lake.
				BlockPos pos = new BlockPos(x + xc, y, z + zc);
				if (world.isBlockNormalCube(pos.down(), false)) {
					//Randomly choose whether to place bricks or not. The math is designed so that the
					//chances of placing a block decrease as we get farther from the gateway's center.
					if (Math.abs(xc) + Math.abs(zc) < world.rand.nextInt(2) + 3) {
						//Place Stone Bricks
						world.setBlockState(pos, state, 3);
					} else if (Math.abs(xc) + Math.abs(zc) < world.rand.nextInt(3) + 3) {
						//Place Cracked Stone Bricks
						world.setBlockState(pos, state.withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), 3);
					}
				}
			}
		}
	}

	@Override
	public String getSchematicPath() {
		return "/schematics/gateways/twoPillars.schematic";
	}
}
