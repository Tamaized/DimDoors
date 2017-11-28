package dimdoors.common.core;


import dimdoors.common.util.DimensionPos;

class LinkTail {
	private DimensionPos destination;
	private LinkType linkType;

	public LinkTail(LinkType linkType, DimensionPos destination) {
		this.linkType = linkType;
		this.destination = destination;
	}

	public DimensionPos getDestination() {
		return destination;
	}

	public void setDestination(DimensionPos destination) {
		this.destination = destination;
	}

	public LinkType getLinkType() {
		return linkType;
	}

	public void setLinkType(LinkType linkType) {
		this.linkType = linkType;
	}
}
