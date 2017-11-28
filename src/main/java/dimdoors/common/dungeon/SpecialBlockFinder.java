package dimdoors.common.dungeon;


import com.google.common.collect.Lists;
import dimdoors.common.schematic.Schematic;
import dimdoors.common.schematic.SchematicFilter;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SpecialBlockFinder extends SchematicFilter {

	private Block warpDoor;
	private Block dimensionalDoor;
	private Block monolithSpawnMarker;
	private Block exitMarker;
	private int entranceOrientation;
	private Schematic schematic;
	private BlockPos entranceDoorLocation;
	private List<BlockPos> exitDoorLocations;
	private List<BlockPos> dimensionalDoorLocations;
	private List<BlockPos> monolithSpawnLocations;

	public SpecialBlockFinder(Block warpDoor, Block dimensionalDoor, Block monolithSpawn, Block exitDoor) {
		super("SpecialBlockFinder");
		this.warpDoor = warpDoor;
		this.dimensionalDoor = dimensionalDoor;
		this.monolithSpawnMarker = monolithSpawn;
		this.exitMarker = exitDoor;
		this.entranceDoorLocation = null;
		this.entranceOrientation = 0;
		this.exitDoorLocations = Lists.newArrayList();
		this.dimensionalDoorLocations = Lists.newArrayList();
		this.monolithSpawnLocations = Lists.newArrayList();
		this.schematic = null;
	}

	public int getEntranceOrientation() {
		return entranceOrientation;
	}

	public BlockPos getEntranceDoorLocation() {
		return entranceDoorLocation;
	}

	public List<BlockPos> getExitDoorLocations() {
		return exitDoorLocations;
	}

	public List<BlockPos> getDimensionalDoorLocations() {
		return dimensionalDoorLocations;
	}

	public List<BlockPos> getMonolithSpawnLocations() {
		return monolithSpawnLocations;
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

		if (blocks[index] == monolithSpawnMarker) {
			monolithSpawnLocations.add(schematic.calculatePoint(index));
			return true;
		}
		if (blocks[index] == dimensionalDoor) {
			indexBelow = schematic.calculateIndexBelow(index);
			if (indexBelow >= 0 && blocks[indexBelow] == dimensionalDoor) {
				dimensionalDoorLocations.add(schematic.calculatePoint(index));
				return true;
			} else {
				return false;
			}
		}
		if (blocks[index] == warpDoor) {
			indexBelow = schematic.calculateIndexBelow(index);
			if (indexBelow >= 0 && blocks[indexBelow] == warpDoor) {
				indexDoubleBelow = schematic.calculateIndexBelow(indexBelow);
				if (indexDoubleBelow >= 0 && blocks[indexDoubleBelow] == exitMarker) {
					exitDoorLocations.add(schematic.calculatePoint(index));
					return true;
				} else if (entranceDoorLocation == null) {
					entranceDoorLocation = schematic.calculatePoint(index);
					entranceOrientation = (metadata[indexBelow] & 3);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean terminates() {
		return false;
	}
}
