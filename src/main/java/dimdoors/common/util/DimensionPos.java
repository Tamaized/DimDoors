package dimdoors.common.util;

import net.minecraft.util.math.BlockPos;

public class DimensionPos extends BlockPos {

	private int dim;

	public DimensionPos(int x, int y, int z, int d) {
		super(x, y, z);
		dim = d;
	}

	public DimensionPos(BlockPos pos, int d) {
		super(pos);
		dim = d;
	}

	public int getDimension() {
		return dim;
	}

	public BlockPos getBlockPos() {
		return new BlockPos(getX(), getY(), getZ());
	}
}
