package dimdoors.common.core;

public enum LinkType {

	// WARNING: Don't modify these values carelessly or you'll risk breaking links in existing worlds!

	NORMAL(0),

	POCKET(1),

	DUNGEON(2),

	RANDOM(3),

	DUNGEON_EXIT(4),

	SAFE_EXIT(5),

	UNSAFE_EXIT(6),

	REVERSE(7),

	PERSONAL(8),

	LIMBO(9),

	CLIENT(-1337);

	public static final LinkType[] values = values();

	public final int index;

	LinkType(int index) {
		this.index = index;
	}

	public static LinkType getLinkTypeFromIndex(int index) {
		for (LinkType type : values)
			if (type.index == index)
				return type;
		return null;
	}
}
