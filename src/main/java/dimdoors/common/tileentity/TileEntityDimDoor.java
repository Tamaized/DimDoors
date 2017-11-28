package dimdoors.common.tileentity;


import dimdoors.common.core.PocketManager;
import dimdoors.common.watcher.ClientLinkData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityDimDoor extends DDTileEntityBase {

	public boolean openOrClosed;
	public int orientation;
	public boolean hasExit;
	public byte lockStatus;
	public boolean isDungeonChainLink;
	public boolean hasGennedPair = false;

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		if (PocketManager.getLink(pos, world) != null) {
			ClientLinkData linkData = new ClientLinkData(PocketManager.getLink(pos, world));
			NBTTagCompound link = new NBTTagCompound();
			linkData.writeToNBT(link);
			tag.setTag("Link", link);
		}
		return new SPacketUpdateTileEntity(pos, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound tag = pkt.getNbtCompound();
		readFromNBT(tag);

		if (tag.hasKey("Link")) {
			ClientLinkData linkData = ClientLinkData.readFromNBT(tag.getCompoundTag("Link"));
			PocketManager.getLinkWatcher().onCreated(linkData);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.openOrClosed = nbt.getBoolean("openOrClosed");
		this.orientation = nbt.getInteger("orientation");
		this.hasExit = nbt.getBoolean("hasExit");
		this.isDungeonChainLink = nbt.getBoolean("isDungeonChainLink");
		this.hasGennedPair = nbt.getBoolean("hasGennedPair");
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("openOrClosed", this.openOrClosed);
		nbt.setBoolean("hasExit", this.hasExit);
		nbt.setInteger("orientation", this.orientation);
		nbt.setBoolean("isDungeonChainLink", isDungeonChainLink);
		nbt.setBoolean("hasGennedPair", hasGennedPair);
		return nbt;
	}

	@Override
	public float[] getRenderColor(Random rand) {
		float[] rgbaColor = {1, 1, 1, 1};
		if (this.world.provider.getDimension() == -1) {
			rgbaColor[0] = rand.nextFloat() * 0.5F + 0.4F;
			rgbaColor[1] = rand.nextFloat() * 0.05F;
			rgbaColor[2] = rand.nextFloat() * 0.05F;
		} else {
			rgbaColor[0] = rand.nextFloat() * 0.5F + 0.1F;
			rgbaColor[1] = rand.nextFloat() * 0.4F + 0.4F;
			rgbaColor[2] = rand.nextFloat() * 0.6F + 0.5F;
		}
		return rgbaColor;
	}
}
