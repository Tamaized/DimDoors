package dimdoors.common.schematic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldBlockSetter implements IBlockSetter {
	public final int BLOCK_UPDATES_FLAG = 1;
	public final int NOTIFY_CLIENT_FLAG = 2;

	private int flags;
	private boolean ignoreAir;

	public WorldBlockSetter(boolean doBlockUpdates, boolean notifyClients, boolean ignoreAir) {
		this.flags = 0;
		this.flags += doBlockUpdates ? BLOCK_UPDATES_FLAG : 0;
		this.flags += notifyClients ? NOTIFY_CLIENT_FLAG : 0;
		this.ignoreAir = ignoreAir;
	}

	@Override
	public void setBlock(World world, int x, int y, int z, IBlockState state) {
		BlockPos pos = new BlockPos(x, y, z);
		if (!ignoreAir || !world.isAirBlock(pos)) {
			world.setBlockState(pos, state, flags);
		}
	}
}
