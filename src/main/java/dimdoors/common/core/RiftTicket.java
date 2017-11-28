package dimdoors.common.core;


import dimdoors.common.util.DimensionPos;

public class RiftTicket implements Comparable<RiftTicket> {

	private long timestamp;
	private DimensionPos location;

	public RiftTicket(DimensionPos location, long timestamp) {
		this.timestamp = timestamp;
		this.location = location;
	}

	@Override
	public int compareTo(RiftTicket other) {
		if (this.timestamp < other.timestamp) {
			return -1;
		} else if (this.timestamp > other.timestamp) {
			return 1;
		}
		return 0;
	}

	public long timestamp() {
		return timestamp;
	}

	public DimensionPos location() {
		return location;
	}

}
