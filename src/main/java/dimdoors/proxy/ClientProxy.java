package dimdoors.proxy;

import dimdoors.client.entity.RenderRift;
import dimdoors.client.particle.ClosingRiftFX;
import dimdoors.client.particle.GoggleRiftFX;
import dimdoors.common.entity.EntityRift;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {
		RenderingRegistry.registerEntityRenderingHandler(EntityRift.class, RenderRift::new);
	}

	@Override
	public void init() {

	}

	@Override
	public void postInit() {

	}

	@Override
	public void spawnParticle(ParticleType type, World world, Vec3d pos, Vec3d vel) {
		Particle particle = null;
		switch (type) {
			case GOGGLE_RIFT:
				particle = new GoggleRiftFX(world, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z, Minecraft.getMinecraft().effectRenderer);
				break;
			case CLOSING_RIFT:
				particle = new ClosingRiftFX(world, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z, Minecraft.getMinecraft().effectRenderer);
				break;
		}
		if (particle != null)
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}
}
