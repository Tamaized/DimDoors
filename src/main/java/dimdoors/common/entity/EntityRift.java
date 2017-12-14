package dimdoors.common.entity;

import dimdoors.DimDoors;
import dimdoors.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityRift extends Entity { // TODO

	public EntityRift(World worldIn) {
		super(worldIn);
	}

	public EntityRift(World worldIn, BlockPos pos) {
		super(worldIn);
		setPosition(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		DimDoors.proxy.spawnParticle(CommonProxy.ParticleType.GOGGLE_RIFT, world, getPositionVector(), new Vec3d(rand.nextGaussian() * 0.01D, rand.nextGaussian()  * 0.01D, rand.nextGaussian() * 0.01D));
	}

	@Override
	protected void entityInit() {

	}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {

	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {

	}

	public static boolean isThereARiftAt(World world, BlockPos pos){
		return !world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1))).isEmpty();
	}
}
