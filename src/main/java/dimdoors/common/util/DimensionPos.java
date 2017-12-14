package dimdoors.common.util;

import net.minecraft.util.math.BlockPos;

public class DimensionPos extends BlockPos {

	private int dimension;

	public DimensionPos(int x, int y, int z, int d) {
		super(x, y, z);
		dimension = d;
	}

	public DimensionPos(BlockPos pos, int d) {
		super(pos);
		dimension = d;
	}

	public int getDimension() {
		return dimension;
	}

	public BlockPos getBlockPos() {
		return new BlockPos(getX(), getY(), getZ());
	}
}
