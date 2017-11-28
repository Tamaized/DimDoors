package dimdoors.common.saving;


import dimdoors.common.core.LinkType;
import dimdoors.common.util.DimensionPos;

public class PackedLinkTail {
	public final DimensionPos destination;
	public final int linkType;

	public PackedLinkTail(DimensionPos destination, LinkType linkType) {
		this.destination = destination;
		this.linkType = linkType.index;
	}

}
