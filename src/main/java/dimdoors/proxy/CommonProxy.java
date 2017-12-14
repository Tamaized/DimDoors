package dimdoors.proxy;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class CommonProxy {

	public abstract void preInit();

	public abstract void init();

	public abstract void postInit();

	public void spawnParticle(ParticleType type, World world, Vec3d pos, Vec3d vel) {

	}

	public enum ParticleType {
		CLOSING_RIFT, GOGGLE_RIFT
	}

}
