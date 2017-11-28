package dimdoors.common.entity;


import dimdoors.DimDoorsConfig;
import dimdoors.common.core.DDTeleporter;
import dimdoors.common.util.DimensionPos;
import dimdoors.common.util.RandomBetween;
import dimdoors.common.world.LimboProvider;
import dimdoors.common.world.PocketProvider;
import dimdoors.registry.DimSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class EntityMonolith extends EntityFlying implements IMob {

	private static final AxisAlignedBB ZERO_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

	private static final DataParameter<Integer> AGGRO_WATCHER = EntityDataManager.createKey(EntityMonolith.class, DataSerializers.VARINT);

	private static final int MAX_AGGRO = 250;
	private static final int MAX_AGGRO_CAP = 100;
	private static final int MIN_AGGRO_CAP = 25;
	private static final int MAX_TEXTURE_STATE = 18;
	private static final int MAX_SOUND_COOLDOWN = 200;
	private static final int MAX_AGGRO_RANGE = 35;

	private static final float WIDTH = 3f;
	private static final float HEIGHT = 3f;
	private static final float EYE_HEIGHT = HEIGHT / 2;
	private final int aggroCap;
	public float pitchLevel;
	private int aggro = 0;
	private int soundTime = 0;

	public EntityMonolith(World world) {
		super(world);
		this.setSize(WIDTH, HEIGHT);
		this.noClip = true;
		this.aggroCap = RandomBetween.getRandomIntBetween(rand, MIN_AGGRO_CAP, MAX_AGGRO_CAP);
	}

	public boolean isDangerous() {
		return DimDoorsConfig.monolithTeleportationEnabled && (DimDoorsConfig.category_dimension.limboDimensionID != world.provider.getDimension() || !DimDoorsConfig.dangerousLimboMonolithsDisabled);
	}

	@Override
	protected void damageEntity(@Nonnull DamageSource source, float amount) {
		// NO-OP
	}

	@Override
	public boolean attackEntityFrom(@Nonnull DamageSource source, float amount) {
		if (source != DamageSource.IN_WALL) {
			this.aggro = MAX_AGGRO;
		}
		return false;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		return ZERO_AABB;
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity par1Entity) {
		return null;
	}

	@Override
	public boolean canDespawn() {
		return false;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(57005);
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public float getEyeHeight() {
		return EYE_HEIGHT;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(AGGRO_WATCHER, 0);
	}

	@Override
	public boolean isEntityAlive() {
		return false;
	}

	@Override
	public void onEntityUpdate() {
		// Remove this Monolith if it's not in Limbo or in a pocket dimension
		if (world.provider.getDimension() != DimDoorsConfig.category_dimension.limboDimensionID || !(world.provider instanceof PocketProvider)) {
			this.setDead();
			super.onEntityUpdate();
			return;
		}

		super.onEntityUpdate();

		// Check for players and update aggro levels even if there are no players in range
		EntityPlayer player = world.getClosestPlayerToEntity(this, MAX_AGGRO_RANGE);
		boolean visibility = player != null && player.canEntityBeSeen(this);
		this.updateAggroLevel(player, visibility);

		// Change orientation and face a player if one is in range
		if (player != null) {
			this.facePlayer(player);
			if (!world.isRemote && isDangerous()) {
				// Play sounds on the server side, if the player isn't in Limbo.
				// Limbo is excluded to avoid drowning out its background music.
				// Also, since it's a large open area with many Monoliths, some
				// of the sounds that would usually play for a moment would
				// keep playing constantly and would get very annoying.
				this.playSounds(player);
			}

			if (visibility) {
				// Only spawn particles on the client side and outside Limbo
				if (world.isRemote && isDangerous()) {
					this.spawnParticles(player);
				}

				// Teleport the target player if various conditions are met
				if (aggro >= MAX_AGGRO && !world.isRemote && DimDoorsConfig.monolithTeleportationEnabled && !player.capabilities.isCreativeMode && isDangerous()) {
					this.aggro = 0;
					DimensionPos destination = LimboProvider.getLimboSkySpawn(player);
					DDTeleporter.teleportEntity(player, destination, false);
					//					player.world.playSound(player, DimDoors.modid + ":crack", 13, 1); TODO
				}
			}
		}
	}

	private void updateAggroLevel(EntityPlayer player, boolean visibility) {
		// If we're working on the server side, adjust aggro level
		// If we're working on the client side, retrieve aggro level from dataWatcher
		if (!this.world.isRemote) {
			// Server side...
			// Rapidly increase the aggro level if this Monolith can see the player
			if (visibility) {
				if (this.world.provider.getDimension() == DimDoorsConfig.category_dimension.limboDimensionID) {
					if (isDangerous())
						aggro++;
					else
						aggro += 36;
				} else {
					// Aggro increases faster outside of Limbo
					aggro += 3;
				}
			} else {
				if (isDangerous()) {
					if (aggro > aggroCap) {
						// Decrease aggro over time
						aggro--;
					} else if (player != null && (aggro < aggroCap)) {
						// Increase aggro if a player is within range and aggro < aggroCap
						aggro++;
					}
				} else
					aggro -= 3;
			}
			// Clamp the aggro level
			int maxAggro = isDangerous() ? MAX_AGGRO : 180;
			dataManager.set(AGGRO_WATCHER, MathHelper.clamp(aggro, 0, maxAggro));
		} else {
			// Client side...
			aggro = dataManager.get(AGGRO_WATCHER);
		}
	}

	public int getTextureState() {
		// Determine texture state from aggro progress
		return MathHelper.clamp(MAX_TEXTURE_STATE * aggro / MAX_AGGRO, 0, MAX_TEXTURE_STATE);
	}

	/**
	 * Plays sounds at different levels of aggro, using soundTime to prevent too many sounds at once.
	 *
	 * @param entityPlayer
	 */
	private void playSounds(EntityPlayer entityPlayer) {
		float aggroPercent = this.getAggroProgress();
		if (this.soundTime <= 0) {
			this.playSound(DimSounds.monk, 1F, 1F);
			this.soundTime = 100;
		}
		if ((aggroPercent > 0.70) && this.soundTime < 100) {
			playSound(DimSounds.tearing, 1F, (float) (1 + this.rand.nextGaussian()));
			this.soundTime = 100 + this.rand.nextInt(75);
		}
		if ((aggroPercent > 0.80) && this.soundTime < 200) {
			playSound(DimSounds.tearing, 7F, 1F);
			this.soundTime = 250;
		}
		this.soundTime--;
	}

	private void spawnParticles(EntityPlayer player) {
		int count = 10 * aggro / MAX_AGGRO;
		for (int i = 1; i < count; ++i) {
			player.world.spawnParticle(EnumParticleTypes.PORTAL, player.posX + (this.rand.nextDouble() - 0.5D) * this.width, player.posY + this.rand.nextDouble() * player.height - 0.75D, player.posZ + (this.rand.nextDouble() - 0.5D) * player.width, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
		}
	}

	public float getAggroProgress() {
		return ((float) aggro) / MAX_AGGRO;
	}

	private void facePlayer(EntityPlayer player) {
		double d0 = player.posX - this.posX;
		double d1 = player.posZ - this.posZ;
		double d2 = (player.posY + player.getEyeHeight()) - (this.posY + this.getEyeHeight());
		double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1);
		float f2 = (float) (Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
		this.pitchLevel = (float) -((Math.atan(d2 / d3)) * 180.0D / Math.PI);
		this.rotationYaw = f2;
		this.rotationYawHead = f2;
		this.renderYawOffset = this.rotationYaw;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound rootTag) {
		super.writeEntityToNBT(rootTag);
		rootTag.setInteger("Aggro", this.aggro);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound rootTag) {
		super.readEntityFromNBT(rootTag);

		// Load Monoliths with half aggro so they don't teleport players instantly
		this.aggro = (short) (rootTag.getInteger("Aggro") / 2);
	}

	@Override
	public boolean getCanSpawnHere() {
		@SuppressWarnings("rawtypes") List list = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.posX - 15, posY - 4, this.posZ - 15, this.posX + 15, this.posY + 15, this.posZ + 15));

		if (this.world.provider.getDimension() == DimDoorsConfig.category_dimension.limboDimensionID) {
			if (list.size() > 0) {
				return false;
			}

		} else if (this.world.provider instanceof PocketProvider) {
			if (list.size() > 5 || this.world.canBlockSeeSky(getPosition())) {
				return false;
			}
		}
		return this.world.checkNoEntityCollision(this.getEntityBoundingBox()) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(this.getEntityBoundingBox());
	}
}