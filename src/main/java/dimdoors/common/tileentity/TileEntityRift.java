package dimdoors.common.tileentity;


import dimdoors.DimDoorsConfig;
import dimdoors.common.blocks.BlockRift;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.util.DimensionPos;
import dimdoors.common.watcher.ClientLinkData;
import dimdoors.registry.DimBlocks;
import dimdoors.registry.DimSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TileEntityRift extends DDTileEntityBase implements ITickable {

	private static final int RIFT_INTERACTION_RANGE = 5;
	private static final int MAX_ANCESTOR_LINKS = 2;
	private static final int MAX_CHILD_LINKS = 1;
	private static final int ENDERMAN_SPAWNING_CHANCE = 1;
	private static final int MAX_ENDERMAN_SPAWNING_CHANCE = 32;
	private static final int RIFT_SPREAD_CHANCE = 1;
	private static final int MAX_RIFT_SPREAD_CHANCE = 256;
	private static final int HOSTILE_ENDERMAN_CHANCE = 1;
	private static final int MAX_HOSTILE_ENDERMAN_CHANCE = 3;
	private static final int UPDATE_PERIOD = 200;
	private static final int CLOSING_PERIOD = 40;

	private static Random random = new Random();
	public int xOffset = 0;
	public int yOffset = 0;
	public int zOffset = 0;
	public boolean shouldClose = false;
	public DimensionPos nearestRiftLocation = null;
	public int spawnedEndermenID = 0;
	public int riftRotation = random.nextInt(360);
	public float growth = 0;
	private int updateTimer;
	private int closeTimer = 0;

	public TileEntityRift() {
		// Vary the update times of rifts to prevent all the rifts in a cluster
		// from updating at the same time.
		updateTimer = random.nextInt(UPDATE_PERIOD);

	}

	@Override
	public void update() {
		if (PocketManager.getLink(new DimensionPos(pos, world.provider.getDimension())) == null) {
			if (world.getBlockState(pos).getBlock() == DimBlocks.blockRift) {
				world.setBlockToAir(pos);
			} else {
				invalidate();
			}
			return;
		}

		if (world.getBlockState(pos).getBlock() != DimBlocks.blockRift) {
			invalidate();
			return;
		}


		// Check if this rift should render white closing particles and
		// spread the closing effect to other rifts nearby.
		if (shouldClose) {
			closeRift();
			return;
		}

		if (updateTimer >= UPDATE_PERIOD) {
			spawnEndermen();
			updateTimer = 0;
		} else if (updateTimer == UPDATE_PERIOD / 2) {
			updateNearestRift();
			spread();
		}
		growth += 1F / (growth + 1);
		updateTimer++;
	}

	private void spawnEndermen() {
		if (world.isRemote || !DimDoorsConfig.riftsSpawnEndermenEnabled) {
			return;
		}

		// Ensure that this rift is only spawning one Enderman at a time, to prevent hordes of Endermen
		Entity entity = world.getEntityByID(this.spawnedEndermenID);
		if (entity != null && entity instanceof EntityEnderman) {
			return;
		}

		if (random.nextInt(MAX_ENDERMAN_SPAWNING_CHANCE) < ENDERMAN_SPAWNING_CHANCE) {
			// Endermen will only spawn from groups of rifts
			if (updateNearestRift()) {
				List<Entity> list = world.getEntitiesWithinAABB(EntityEnderman.class, new AxisAlignedBB(pos.add(-9, -3, -9), pos.add(9, 3, 9)));

				if (list.isEmpty()) {
					EntityEnderman enderman = new EntityEnderman(world);
					enderman.setLocationAndAngles(pos.getX() + 0.5F, pos.getY() - 1F, pos.getZ() + 0.5F, 5, 6);
					world.spawnEntity(enderman);

					if (random.nextInt(MAX_HOSTILE_ENDERMAN_CHANCE) < HOSTILE_ENDERMAN_CHANCE) {
						EntityPlayer player = this.world.getClosestPlayerToEntity(enderman, 50);
						if (player != null) {
							enderman.setAttackTarget(player);
						}
					}
				}
			}
		}
	}

	private void closeRift() {
		NewDimData dimension = PocketManager.createDimensionData(world);
		if (growth < CLOSING_PERIOD / 2) {
			for (DimLink riftLink : dimension.findRiftsInRange(world, 6, pos)) {
				DimensionPos location = riftLink.source();
				TileEntity rift = world.getTileEntity(location);
				if (rift instanceof TileEntityRift && !((TileEntityRift) rift).shouldClose) {
					((TileEntityRift) rift).shouldClose = true;
					rift.markDirty();
				}
			}
		}
		if (growth <= 0 && !world.isRemote) {
			DimLink link = PocketManager.getLink(pos, world);
			if (link != null && !world.isRemote) {
				dimension.deleteLink(link);
			}
			world.setBlockToAir(pos);
			world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, DimSounds.riftclose, SoundCategory.PLAYERS, 0.7f, 1, false);
		}

		growth--;
	}

	public boolean updateNearestRift() {
		DimensionPos previousNearest = nearestRiftLocation;
		DimLink nearestRiftLink = PocketManager.createDimensionData(world).findNearestRift(world, RIFT_INTERACTION_RANGE, pos);

		nearestRiftLocation = (nearestRiftLink == null) ? null : nearestRiftLink.source();

		// If the nearest rift location changed, then update particle offsets
		if (previousNearest != nearestRiftLocation && (previousNearest == null || nearestRiftLocation == null || !previousNearest.equals(nearestRiftLocation))) {
			updateParticleOffsets();
		}
		return (nearestRiftLocation != null);
	}

	private void updateParticleOffsets() {
		if (nearestRiftLocation != null) {
			this.xOffset = pos.getX() - nearestRiftLocation.getX();
			this.yOffset = pos.getY() - nearestRiftLocation.getY();
			this.zOffset = pos.getZ() - nearestRiftLocation.getZ();
		} else {
			this.xOffset = 0;
			this.yOffset = 0;
			this.xOffset = 0;
		}
		this.markDirty();
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}

	public int countAncestorLinks(DimLink link) {
		if (link.parent() != null) {
			return countAncestorLinks(link.parent()) + 1;
		}
		return 0;
	}

	public void spread() {
		if (world.isRemote || !DimDoorsConfig.riftSpreadEnabled || random.nextInt(MAX_RIFT_SPREAD_CHANCE) < RIFT_SPREAD_CHANCE || this.shouldClose) {
			return;
		}

		NewDimData dimension = PocketManager.createDimensionData(world);
		DimLink link = dimension.getLink(pos);

		if (link.childCount() >= MAX_CHILD_LINKS || countAncestorLinks(link) >= MAX_ANCESTOR_LINKS) {
			return;
		}

		// The probability of rifts trying to spread increases if more rifts are nearby.
		// Players should see rifts spread faster within clusters than at the edges of clusters.
		// Also, single rifts CANNOT spread.
		int nearRifts = dimension.findRiftsInRange(world, RIFT_INTERACTION_RANGE, pos).size();
		if (nearRifts == 0 || random.nextInt(nearRifts) == 0) {
			return;
		}
		if (DimBlocks.blockRift instanceof BlockRift)
			((BlockRift) DimBlocks.blockRift).spreadRift(dimension, link, world, random);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.updateTimer = nbt.getInteger("updateTimer");
		this.xOffset = nbt.getInteger("xOffset");
		this.yOffset = nbt.getInteger("yOffset");
		this.zOffset = nbt.getInteger("zOffset");
		this.shouldClose = nbt.getBoolean("shouldClose");
		this.spawnedEndermenID = nbt.getInteger("spawnedEndermenID");
		this.riftRotation = nbt.getInteger("riftRotation");
		this.growth = nbt.getFloat("growth");

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("updateTimer", this.updateTimer);
		nbt.setInteger("xOffset", this.xOffset);
		nbt.setInteger("yOffset", this.yOffset);
		nbt.setInteger("zOffset", this.zOffset);
		nbt.setBoolean("shouldClose", this.shouldClose);
		nbt.setInteger("spawnedEndermenID", this.spawnedEndermenID);
		nbt.setInteger("riftRotation", this.riftRotation);
		nbt.setFloat("growth", this.growth);
		return nbt;
	}

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
		return new SPacketUpdateTileEntity(pos, 0, tag);
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
	public float[] getRenderColor(Random rand) {
		return null;
	}
}
