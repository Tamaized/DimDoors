package dimdoors.common.world.gateways;


import dimdoors.common.schematic.Schematic;
import dimdoors.common.schematic.SchematicFilter;
import dimdoors.registry.DimBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class GatewayBlockFilter extends SchematicFilter {

	private static final short STANDARD_WARP_DOOR_ID = 1975;
	private static final short STANDARD_DIMENSIONAL_DOOR_ID = 1970;
	private static final short STANDARD_TRANSIENT_DOOR_ID = 1979;

	private int entranceOrientation;
	private Schematic schematic;
	private BlockPos entranceDoorLocation;

	public GatewayBlockFilter() {
		super("GatewayEntranceFinder");
		this.entranceDoorLocation = null;
		this.entranceOrientation = 0;
		this.schematic = null;
	}

	public int getEntranceOrientation() {
		return entranceOrientation;
	}

	public BlockPos getEntranceDoorLocation() {
		return entranceDoorLocation;
	}

	@Override
	protected boolean initialize(Schematic schematic, Block[] blocks, byte[] metadata) {
		this.schematic = schematic;
		return true;
	}

	@Override
	protected boolean applyToBlock(int index, Block[] blocks, byte[] metadata) {
		int indexBelow;
		int indexDoubleBelow;
		if (blocks[index] == DimBlocks.dimensionalDoor) {
			indexBelow = schematic.calculateIndexBelow(index);
			if (indexBelow >= 0 && blocks[indexBelow] == DimBlocks.dimensionalDoor) {
				entranceDoorLocation = schematic.calculatePoint(index);
				entranceOrientation = (metadata[indexBelow] & 3);
				return true;
			}
		}
		if (blocks[index] == DimBlocks.transientDoor) {
			indexBelow = schematic.calculateIndexBelow(index);
			if (indexBelow >= 0 && blocks[indexBelow] == DimBlocks.transientDoor) {
				entranceDoorLocation = schematic.calculatePoint(index);
				entranceOrientation = (metadata[indexBelow] & 3);
				return true;
			}
		}
		if (blocks[index] == DimBlocks.warpDoor) {
			indexBelow = schematic.calculateIndexBelow(index);
			if (indexBelow >= 0 && blocks[indexBelow] == DimBlocks.warpDoor) {
				entranceDoorLocation = schematic.calculatePoint(index);
				entranceOrientation = (metadata[indexBelow] & 3);
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean terminates() {
		return true;
	}
}
