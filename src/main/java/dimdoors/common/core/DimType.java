package dimdoors.common.core;

public enum DimType {

	// WARNING: Don't modify these values carelessly or you'll risk breaking existing worlds!
	ROOT(0, false),

	POCKET(1, true),

	DUNGEON(2, true),

	PERSONAL(3, true);

	public final int index;
	public final boolean isPocket;

	public static final DimType[] values = values();

	DimType(int index, boolean isPocket) {
		this.index = index;
		this.isPocket = isPocket;
	}

	/**
	 * Get the DimensionType given an index. I feel like there should be a better way to do this.
	 *
	 * @param index
	 * @return
	 */
	public static DimType getTypeFromIndex(int index) {
		for (DimType type : values) {
			if (type.index == index) {
				return type;
			}
		}
		return null;
	}

	public boolean isPocketDimension() {
		return this.isPocket;
	}
}
