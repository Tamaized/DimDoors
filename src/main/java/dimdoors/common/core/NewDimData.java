package dimdoors.common.core;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dimdoors.common.dungeon.DungeonData;
import dimdoors.common.dungeon.pack.DungeonPack;
import dimdoors.common.entity.EntityRift;
import dimdoors.common.saving.IPackable;
import dimdoors.common.saving.PackedDimData;
import dimdoors.common.saving.PackedDungeonData;
import dimdoors.common.saving.PackedLinkData;
import dimdoors.common.saving.PackedLinkTail;
import dimdoors.common.util.DimensionPos;
import dimdoors.common.watcher.ClientLinkData;
import dimdoors.common.watcher.IUpdateWatcher;
import dimdoors.registry.DimBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;

public abstract class NewDimData implements IPackable<PackedDimData> {
	protected static Random random = new Random();
	private static int EXPECTED_LINKS_PER_CHUNK = 2;
	public IUpdateWatcher<ClientLinkData> linkWatcher;
	protected int id;
	protected Map<DimensionPos, InnerDimLink> linkMapping;
	protected List<InnerDimLink> linkList;
	protected boolean isFilled;
	protected int depth;
	protected int packDepth;
	protected DimType type;
	protected NewDimData parent;
	protected NewDimData root;
	protected List<NewDimData> children;
	protected DimensionPos origin;
	protected int orientation;
	protected DungeonData dungeon;
	protected boolean modified;
	// Don't write this field to a file - it should be recreated on startup
	private Map<ChunkPos, List<InnerDimLink>> chunkMapping;


	protected NewDimData(int id, NewDimData parent, DimType type, IUpdateWatcher<ClientLinkData> linkWatcher) {
		if (type != DimType.ROOT && (parent == null)) {
			throw new NullPointerException("Dimensions can be pocket dimensions if and only if they have a parent dimension.");
		}

		this.id = id;
		this.linkMapping = new TreeMap<>(); //Should be stored in oct tree -- temporary solution
		this.linkList = Lists.newArrayList(); //Should be stored in oct tree -- temporary solution
		this.children = Lists.newArrayList();
		this.parent = parent;
		this.packDepth = 0;
		this.type = type;
		this.isFilled = false;
		this.orientation = 0;
		this.origin = null;
		this.dungeon = null;
		this.linkWatcher = linkWatcher;
		this.chunkMapping = Maps.newHashMap();
		this.modified = true;

		//Register with parent
		if (parent != null) {
			//We don't need to raise an update event for adding a child because the child's creation will be signaled.
			this.root = parent.root;
			this.depth = parent.depth + 1;
			parent.children.add(this);
			parent.modified = true;
		} else {
			this.root = this;
			this.depth = 0;
		}
	}

	protected NewDimData(int id, NewDimData root, DimType type) {
		// This constructor is meant for client-side code only
		if (root == null) {
			throw new IllegalArgumentException("root cannot be null.");
		}

		this.id = id;
		this.linkMapping = new TreeMap<DimensionPos, InnerDimLink>(); //Should be stored in oct tree -- temporary solution
		this.linkList = new ArrayList<InnerDimLink>(); //Should be stored in oct tree -- temporary solution
		this.children = new ArrayList<NewDimData>();
		this.parent = null;
		this.packDepth = 0;
		this.type = type;
		this.isFilled = false;
		this.orientation = 0;
		this.origin = null;
		this.dungeon = null;
		this.linkWatcher = null;
		this.depth = 0;
		this.root = root;
		this.chunkMapping = null;
	}

	private static int getAbsoluteSum(int i, int j, int k) {
		return Math.abs(i) + Math.abs(j) + Math.abs(k);
	}

	public static int calculatePackDepth(NewDimData parent, DungeonData current) {
		DungeonData predecessor = parent.dungeon();
		if (current == null) {
			throw new IllegalArgumentException("current cannot be null.");
		}
		if (predecessor == null) {
			return 1;
		}

		DungeonPack predOwner = predecessor.dungeonType().Owner;
		DungeonPack currentOwner = current.dungeonType().Owner;
		if (currentOwner == null) {
			return 1;
		}
		if (predOwner == null) {
			return 1;
		}
		if (predOwner == currentOwner) {
			return parent.packDepth + 1;
		}
		return 1;
	}

	public DimLink findNearestRift(World world, int range, BlockPos pos) {
		// Sanity check...
		if (world.provider.getDimension() != id) {
			throw new IllegalArgumentException("Attempted to search for links in a World instance for a different dimension!");
		}

		// Note: Only detect rifts at a distance > 0, so we ignore the rift
		// at the center of the search space.
		DimLink link;
		DimLink nearest = null;

		int i, j, k;
		int distance;
		int minDistance = Integer.MAX_VALUE;

		for (i = -range; i <= range; i++) {
			for (j = -range; j <= range; j++) {
				for (k = -range; k <= range; k++) {
					distance = getAbsoluteSum(i, j, k);
					if (distance > 0 && distance < minDistance && EntityRift.isThereARiftAt(world, pos.add(i, j, k))) {
						link = getLink(pos.add(i, j, k));
						if (link != null) {
							nearest = link;
							minDistance = distance;
						}
					}
				}
			}
		}

		return nearest;
	}

	public List<DimLink> findRiftsInRange(World world, int range, BlockPos pos) {
		// Sanity check...
		if (world.provider.getDimension() != id) {
			throw new IllegalArgumentException("Attempted to search for links in a World instance for a different dimension!");
		}

		// Note: Only detect rifts at a distance > 0, so we ignore the rift
		// at the center of the search space.
		int i, j, k;
		int distance;
		DimLink link;
		List<DimLink> links = Lists.newArrayList();

		for (i = -range; i <= range; i++) {
			for (j = -range; j <= range; j++) {
				for (k = -range; k <= range; k++) {
					distance = getAbsoluteSum(i, j, k);
					if (distance > 0 && EntityRift.isThereARiftAt(world, pos.add(i, j, k))) {
						link = getLink(pos.add(i, j, k));
						if (link != null) {
							links.add(link);
						}
					}
				}
			}
		}

		return links;
	}

	public DimLink createLink(BlockPos pos, LinkType linkType, int orientation) {
		return createLink(new DimensionPos(pos, id), linkType, orientation, null);
	}

	public DimLink createLink(int x, int y, int z, LinkType linkType, int orientation) {
		return createLink(new DimensionPos(x, y, z, id), linkType, orientation, null);
	}

	public DimLink createLink(DimensionPos source, LinkType linkType, int orientation, DDLock locked) {
		// Return an existing link if there is one to avoid creating multiple links starting at the same point.
		InnerDimLink link = linkMapping.get(source);
		if (link == null) {
			link = new InnerDimLink(source, linkType, orientation, locked);
			linkMapping.put(source, link);
			linkList.add(link);

			// If this code is running on the server side, add this link to chunkMapping.
			if (linkType != LinkType.CLIENT) {
				ChunkPos chunk = link.getChunkCoordinates();
				List<InnerDimLink> chunkLinks = chunkMapping.computeIfAbsent(chunk, k -> Lists.newArrayList());
				chunkLinks.add(link);
			}
		} else {
			link.overwrite(linkType, orientation);
		}
		modified = true;

		//Link created!
		if (linkType != LinkType.CLIENT)

		{
			linkWatcher.onCreated(new ClientLinkData(link));
		}
		return link;
	}

	public DimLink createChildLink(BlockPos pos, DimLink parent) {
		return createChildLink(new DimensionPos(pos, id), parent, null);
	}

	public DimLink createChildLink(DimensionPos source, DimLink parent, DDLock locked) {
		// To avoid having multiple links at a single point, if we find an existing link then we overwrite
		// its destination data instead of creating a new instance.

		if (parent == null) {
			throw new IllegalArgumentException("parent cannot be null.");
		}
		InnerDimLink link = linkMapping.get(source);
		if (link == null) {
			link = new InnerDimLink(source, parent, parent.orientation, locked);
			linkMapping.put(source, link);
			linkList.add(link);


			// If this code is running on the server side, add this link to chunkMapping.
			// Granted, the client side code should never create child links anyway...
			if (link.linkType() != LinkType.CLIENT) {
				ChunkPos chunk = link.getChunkCoordinates();
				List<InnerDimLink> chunkLinks = chunkMapping.computeIfAbsent(chunk, k -> Lists.newArrayList());
				chunkLinks.add(link);
			}

			// Link created!
			linkWatcher.onCreated(new ClientLinkData(link));
		} else {
			if (link.overwrite((InnerDimLink) parent, parent.orientation)) {
				//Link created!
				linkWatcher.onCreated(new ClientLinkData(link));
			}
		}
		modified = true;
		return link;
	}

	public boolean deleteLink(DimLink link) {
		if (link.source().getDimension() != id) {
			throw new IllegalArgumentException("Attempted to delete a link from another dimension.");
		}
		InnerDimLink target = linkMapping.remove(link.source());
		if (target != null) {
			linkList.remove(target);

			// If this code is running on the server side, remove this link to chunkMapping.
			if (link.linkType() != LinkType.CLIENT) {
				ChunkPos chunk = target.getChunkCoordinates();
				List<InnerDimLink> chunkLinks = chunkMapping.get(chunk);
				if (chunkLinks != null) {
					chunkLinks.remove(target);
				}
			}

			// Raise deletion event
			if (linkWatcher != null)
				linkWatcher.onDeleted(new ClientLinkData(link));
			target.clear();
			modified = true;
		}
		return (target != null);
	}

	public boolean deleteLink(int x, int y, int z) {
		return this.deleteLink(this.getLink(x, y, z));
	}

	public boolean deleteLink(DimensionPos location) {
		return this.deleteLink(this.getLink(location));
	}

	public DimLink getLink(int x, int y, int z) {
		DimensionPos location = new DimensionPos(x, y, z, id);
		return linkMapping.get(location);
	}

	public DimLink getLink(BlockPos location) {
		return linkMapping.get(new DimensionPos(location, this.id));
	}

	public DimLink getLink(DimensionPos location) {
		if (location.getDimension() != id)
			return null;

		return linkMapping.get(location);
	}

	public List<DimLink> getAllLinks() {
		return Lists.newArrayList(linkMapping.values());
	}

	public boolean isPocketDimension() {
		return (root != this);
	}

	public DimType type() {
		return this.type;
	}

	public boolean isFilled() {
		return isFilled;
	}

	public void setFilled(boolean isFilled) {
		this.isFilled = isFilled;
		this.modified = true;
	}

	public int id() {
		return id;
	}

	public int depth() {
		return depth;
	}

	public int packDepth() {
		return packDepth;
	}

	public DimensionPos origin() {
		return origin;
	}

	public NewDimData parent() {
		return parent;
	}

	public NewDimData root() {
		return root;
	}

	public int orientation() {
		return orientation;
	}

	public DungeonData dungeon() {
		return dungeon;
	}

	public boolean isInitialized() {
		return (origin != null);
	}

	public int linkCount() {
		return linkList.size();
	}

	public Iterable<NewDimData> children() {
		return children;
	}

	public Iterable<? extends DimLink> links() {
		return linkList;
	}

	public void initializeDungeon(int originX, int originY, int originZ, int orientation, DimLink incoming, DungeonData dungeon) {
		if (this.type != DimType.DUNGEON) {
			throw new IllegalStateException("Cannot invoke initializeDungeon() on a non-dungeon dimension.");
		}
		if (isInitialized()) {
			throw new IllegalStateException("The dimension has already been initialized.");
		}
		if (orientation < 0 || orientation > 3) {
			throw new IllegalArgumentException("orientation must be between 0 and 3, inclusive.");
		}
		setLinkDestination(incoming, originX, originY, originZ);
		this.origin = incoming.destination();
		this.orientation = orientation;
		this.dungeon = dungeon;
		this.packDepth = calculatePackDepth(parent, dungeon);
		this.modified = true;
	}

	/**
	 * Effectively moves the dungeon to the 'top' of a chain as far as dungeon generation is concerned.
	 */
	public void setParentToRoot() {
		// Update this dimension's information
		if (parent != null) {
			parent.children.remove(this);
		}
		this.depth = 1;
		this.parent = this.root;
		this.root.children.add(this);
		this.root.modified = true;
		this.modified = true;
		if (this.type == DimType.DUNGEON) {
			this.packDepth = calculatePackDepth(this.parent, this.dungeon);
		}

		// Update the depths for child dimensions using a depth-first traversal
		Stack<NewDimData> ordering = new Stack<>();
		ordering.addAll(this.children);

		while (!ordering.isEmpty()) {
			NewDimData current = ordering.pop();
			current.resetDepth();
			ordering.addAll(current.children);
		}
	}

	private void resetDepth() {
		// We assume that this is only applied to dimensions with parents
		this.depth = this.parent.depth + 1;
		if (this.type == DimType.DUNGEON) {
			this.packDepth = calculatePackDepth(this.parent, this.dungeon);
		}
		this.modified = true;
	}

	public void initializePocket(int originX, int originY, int originZ, int orientation, DimLink incoming) {
		if (!isPocketDimension()) {
			throw new IllegalStateException("Cannot invoke initializePocket() on a non-pocket dimension.");
		}
		if (isInitialized()) {
			throw new IllegalStateException("The dimension has already been initialized.");
		}

		setLinkDestination(incoming, originX, originY, originZ);
		this.origin = incoming.destination();
		this.orientation = orientation;
		this.modified = true;
	}

	public void setLinkDestination(DimLink incoming, BlockPos pos){
		setLinkDestination(incoming, pos.getX(), pos.getY(), pos.getZ());
	}

	public void setLinkDestination(DimLink incoming, int x, int y, int z) {
		InnerDimLink link = (InnerDimLink) incoming;
		link.setDestination(x, y, z, this);
		this.modified = true;
	}

	public void lock(DimLink link, boolean locked) {
		InnerDimLink innerLink = (InnerDimLink) link;
		innerLink.lock.setLockState(locked);
		modified = true;
	}

	public void setLock(DimLink link, DDLock lock) {
		InnerDimLink innerLink = (InnerDimLink) link;
		innerLink.setLock(lock);
		modified = true;
	}

	public void createLock(DimLink link, ItemStack item, int lockKey) {
		InnerDimLink innerLink = (InnerDimLink) link;
		innerLink.createLock(item, lockKey);
		modified = true;
	}

	public void removeLock(DimLink link, ItemStack item) {
		InnerDimLink innerLink = (InnerDimLink) link;
		innerLink.removeLock(item, innerLink);
		modified = true;
	}

	public DimLink getRandomLink() {
		if (linkMapping.isEmpty()) {
			throw new IllegalStateException("There are no links to select from in this dimension.");
		}
		if (linkList.size() > 1) {
			return linkList.get(random.nextInt(linkList.size()));
		}
		return linkList.get(0);
	}

	public Iterable<? extends DimLink> getChunkLinks(int chunkX, int chunkZ) {
		List<InnerDimLink> chunkLinks = chunkMapping.get(new ChunkPos(chunkX, chunkZ));
		if (chunkLinks != null) {
			return chunkLinks;
		}
		return new ArrayList<InnerDimLink>(0);
	}

	public boolean isModified() {
		return modified;
	}

	public void clearModified() {
		this.modified = false;
	}

	public void clear() {
		// If this dimension has a parent, remove it from its parent's list of children
		if (parent != null) {
			parent.children.remove(this);
		}
		// Remove this dimension as the parent of its children
		for (NewDimData child : children) {
			child.parent = null;
		}
		// Clear all fields
		id = Integer.MIN_VALUE;
		linkMapping.clear();
		linkMapping = null;
		linkList.clear();
		linkList = null;
		children.clear();
		children = null;
		type = null;
		isFilled = false;
		depth = Integer.MIN_VALUE;
		packDepth = Integer.MIN_VALUE;
		origin = null;
		orientation = Integer.MIN_VALUE;
		dungeon = null;
		linkWatcher = null;
	}

	public PackedDimData pack() {
		ArrayList<Integer> ChildIDs = new ArrayList<Integer>();
		ArrayList<PackedLinkData> Links = new ArrayList<PackedLinkData>();
		ArrayList<PackedLinkTail> Tails = new ArrayList<PackedLinkTail>();
		PackedDungeonData packedDungeon = null;

		if (this.dungeon != null) {
			packedDungeon = new PackedDungeonData(dungeon.weight(), dungeon.isOpen(), dungeon.isInternal(), dungeon.schematicPath(), dungeon.schematicName(), dungeon.dungeonType().Name, dungeon.dungeonType().Owner.getName());
		}
		//Make a list of children
		for (NewDimData data : this.children) {
			ChildIDs.add(data.id);
		}
		for (DimLink link : this.links()) {
			ArrayList<BlockPos> children = Lists.newArrayList();
			BlockPos parentPoint = new BlockPos(-1, -1, -1);
			if (link.parent != null) {
				parentPoint = link.parent.point.getBlockPos();
			}

			for (DimLink childLink : link.children) {
				children.add(childLink.source().getBlockPos());
			}
			PackedLinkTail tail = new PackedLinkTail(link.tail.getDestination(), link.tail.getLinkType());
			Links.add(new PackedLinkData(link.point, parentPoint, tail, link.orientation, children, link.lock));

			PackedLinkTail tempTail = new PackedLinkTail(link.tail.getDestination(), link.tail.getLinkType());
			if (Tails.contains(tempTail)) {
				Tails.add(tempTail);
			}


		}
		int parentID = this.id;
		BlockPos originPoint = new BlockPos(0, 0, 0);
		if (this.parent != null) {
			parentID = this.parent.id;
		}
		if (this.origin != null) {
			originPoint = this.origin.getBlockPos();
		}
		return new PackedDimData(this.id, depth, this.packDepth, parentID, this.root().id(), orientation, type, isFilled, packedDungeon, originPoint, ChildIDs, Links, Tails);
		// FIXME: IMPLEMENTATION PLZTHX
		//I tried
	}

	@Override
	public String name() {
		return String.valueOf(id);
	}

	@Override
	public String toString() {
		return "DimID= " + this.id;
	}

	private static class InnerDimLink extends DimLink {
		public InnerDimLink(DimensionPos source, DimLink parent, int orientation, DDLock lock) {
			super(source, orientation, lock, parent);
		}

		public InnerDimLink(DimensionPos source, LinkType linkType, int orientation, DDLock lock) {
			super(source, orientation, lock, linkType);
		}

		public void setDestination(int x, int y, int z, NewDimData dimension) {
			tail.setDestination(new DimensionPos(x, y, z, dimension.id()));
		}

		public boolean overwrite(InnerDimLink nextParent, int orientation) {
			if (nextParent == null) {
				throw new IllegalArgumentException("nextParent cannot be null.");
			}
			if (this == nextParent) {
				//Ignore this request silently
				return false;
			}
			if (nextParent.point.getDimension() != point.getDimension()) {
				// Ban having children in other dimensions to avoid serialization issues with cross-dimensional tails
				throw new IllegalArgumentException("source and parent.source must have the same dimension.");
			}

			//Release children
			for (DimLink child : children) {
				((InnerDimLink) child).parent = null;
			}
			children.clear();

			//Release parent
			if (parent != null) {
				parent.children.remove(this);
			}

			//Attach to new parent
			parent = nextParent;
			tail = nextParent.tail;
			nextParent.children.add(this);
			this.orientation = orientation;
			return true;
		}

		public void overwrite(LinkType linkType, int orientation) {
			//Release children
			for (DimLink child : children) {
				((InnerDimLink) child).parent = null;
			}
			children.clear();

			//Release parent
			if (parent != null) {
				parent.children.remove(this);
			}

			//Attach to new parent
			parent = null;
			tail = new LinkTail(linkType, null);
			//Set new orientation
			this.orientation = orientation;
		}

		/**
		 * only use this on the client to update errything
		 *
		 * @param lock
		 */
		public void setLock(DDLock lock) {
			this.lock = lock;
		}

		/**
		 * create a lock from a key. Returns false if this door already has a lock, or if they has already locked a door
		 *
		 * @param itemStack
		 * @return
		 */
		public boolean createLock(ItemStack itemStack, int lockKey) {
			if (this.hasLock() || DDLock.hasCreatedLock(itemStack)) {
				return false;
			}
			this.lock = DDLock.generateLockKeyPair(itemStack, lockKey);
			return true;
		}

		public void removeLock(ItemStack itemStack, InnerDimLink link) {
			if (link.doesKeyUnlock(itemStack)) {
				link.lock = null;
			}
		}

	}
}