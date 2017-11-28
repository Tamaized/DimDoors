package dimdoors.common.saving;


import dimdoors.common.core.DDLock;
import dimdoors.common.util.DimensionPos;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PackedLinkData {

	public final DimensionPos source;
	public final BlockPos parent;
	public final PackedLinkTail tail;
	public final int orientation;
	public final List<BlockPos> children;
	public final DDLock lock;

	public PackedLinkData(DimensionPos source, BlockPos parent, PackedLinkTail tail, int orientation, List<BlockPos> children, DDLock lock) {
		this.source = source;
		this.parent = parent;
		this.tail = tail;
		this.orientation = orientation;
		this.children = children;
		this.lock = lock;
	}
}
