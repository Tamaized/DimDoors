package dimdoors.common.util;

import net.minecraft.util.math.ChunkPos;

public class ChunkLocation {

	private final int dim;
	private final ChunkPos chunk;

	public ChunkLocation(int dimensionID, ChunkPos chunkPos) {
		dim = dimensionID;
		chunk = chunkPos;
	}

	public int getDimension() {
		return dim;
	}

	public ChunkPos getChunkPos() {
		return chunk;
	}
}
