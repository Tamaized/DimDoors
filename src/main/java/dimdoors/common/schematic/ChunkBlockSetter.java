package dimdoors.common.schematic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class ChunkBlockSetter implements IBlockSetter {

	private boolean ignoreAir;

	public ChunkBlockSetter(boolean ignoreAir) {
		this.ignoreAir = ignoreAir;
	}

	@Override
	public void setBlock(World world, int x, int y, int z, IBlockState state) {
		BlockPos pos = new BlockPos(x, y, z);
		if (world.isAirBlock(pos) && ignoreAir) {
			return;
		}

		int cX = x >> 4;
		int cZ = z >> 4;
		int cY = y >> 4;
		Chunk chunk;

		int localX = (x % 16) < 0 ? (x % 16) + 16 : (x % 16);
		int localZ = (z % 16) < 0 ? (z % 16) + 16 : (z % 16);
		ExtendedBlockStorage extBlockStorage;

		try {
			chunk = world.getChunkFromChunkCoords(cX, cZ);
			extBlockStorage = chunk.getBlockStorageArray()[cY];
			if (extBlockStorage == null) {
				extBlockStorage = new ExtendedBlockStorage(cY << 4, !world.provider.isNether());
				chunk.getBlockStorageArray()[cY] = extBlockStorage;
			}
			extBlockStorage.set(localX, y & 15, localZ, state);
			chunk.markDirty();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
