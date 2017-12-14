package dimdoors.common.world.gateways;


import dimdoors.common.core.LinkType;
import dimdoors.common.core.PocketManager;
import dimdoors.common.world.LimboProvider;
import dimdoors.registry.DimBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemDoor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GatewayLimbo extends BaseGateway {

	public GatewayLimbo() {
		super();
	}

	@Override
	public boolean generate(World world, int x, int y, int z) {
		IBlockState state = DimBlocks.unraveledFabric.getDefaultState();
		BlockPos pos = new BlockPos(x, y, z);
		// Build the gateway out of Unraveled Fabric. Since nearly all the blocks in Limbo are of
		// that type, there is no point replacing the ground.
		world.setBlockState(pos.add(0, 3, 1), state, 3);
		world.setBlockState(pos.add(0, 3, 1), state, 3);

		// Build the columns around the door
		world.setBlockState(pos.add(0, 2, 1), state, 3);
		world.setBlockState(pos.add(0, 2, 1), state, 3);
		world.setBlockState(pos.add(0, 1, 1), state, 3);
		world.setBlockState(pos.add(0, 1, 1), state, 3);

		PocketManager.getDimensionData(world).createLink(x, y + 2, z, LinkType.DUNGEON, 0);

		ItemDoor.placeDoor(world, pos.add(0, 1, 0), EnumFacing.DOWN, DimBlocks.transientDoor, false);
		return true;
	}

	@Override
	public boolean isLocationValid(World world, int x, int y, int z) {
		return (world.provider instanceof LimboProvider);
	}
}
