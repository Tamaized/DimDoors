package dimdoors.common.watcher;


import dimdoors.common.core.DDLock;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.util.DimensionPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;

public class ClientLinkData {

	public final DimensionPos point;
	public final DDLock lock;
	public final LinkType type;

	public ClientLinkData(DimLink link) {
		this.point = link.source();
		this.type = link.linkType();
		if (link.hasLock()) {
			lock = link.getLock();
		} else {
			lock = null;
		}
	}

	public ClientLinkData(DimensionPos point, LinkType type, DDLock lock) {
		this.point = point;
		this.lock = lock;
		this.type = type;

	}

	public static ClientLinkData read(ByteBuf input) throws IOException {
		DimensionPos point = new DimensionPos(input.readInt(), input.readInt(), input.readInt(), input.readInt());
		LinkType type = LinkType.getLinkTypeFromIndex(input.readInt());
		DDLock lock = null;
		if (input.readBoolean()) {
			lock = new DDLock(input.readBoolean(), input.readInt());
		}
		return new ClientLinkData(point, type, lock);
	}

	public static ClientLinkData readFromNBT(NBTTagCompound tag) {
		LinkType type = LinkType.getLinkTypeFromIndex(tag.getInteger("Type"));
		DimensionPos point = null;
		if (tag.hasKey("Point")) {
			NBTTagCompound ct = tag.getCompoundTag("Point");
			point = new DimensionPos(ct.getInteger("x"), ct.getInteger("y"), ct.getInteger("z"), ct.getInteger("dim"));
		}
		DDLock lock = null;
		if (tag.hasKey("Lock")) {
			NBTTagCompound lockTag = tag.getCompoundTag("Lock");
			lock = new DDLock(lockTag.getBoolean("State"), lockTag.getInteger("Key"));
		}
		return new ClientLinkData(point, type, lock);
	}

	public void write(ByteBuf output) throws IOException {
		output.writeInt(point.getX());
		output.writeInt(point.getY());
		output.writeInt(point.getZ());
		output.writeInt(point.getDimension());
		output.writeInt(this.type.index);
		boolean hasLock = this.lock != null;
		output.writeBoolean(hasLock);

		if (hasLock) {
			output.writeBoolean(lock.getLockState());
			output.writeInt(lock.getLockKey());
		}
	}

	public void writeToNBT(NBTTagCompound tag) {
		tag.setInteger("Type", this.type.index);

		if (this.lock != null) {
			NBTTagCompound lock = new NBTTagCompound();
			lock.setBoolean("State", this.lock.getLockState());
			lock.setInteger("Key", this.lock.getLockKey());
			tag.setTag("Lock", lock);
		}

		if (this.point != null) {
			NBTTagCompound point = new NBTTagCompound();
			point.setInteger("dim", this.point.getDimension());
			point.setInteger("x", this.point.getX());
			point.setInteger("y", this.point.getY());
			point.setInteger("z", this.point.getZ());
			tag.setTag("Point", point);
		}
	}
}
