package dimdoors.common.world;


import dimdoors.client.world.CloudRenderBlank;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class PersonalPocketProvider extends PocketProvider {

	private IRenderHandler skyRenderer;

	public PersonalPocketProvider() {
		super();
	}

	@Nonnull
	@Override
	public Vec3d getSkyColor(@Nonnull Entity cameraEntity, float partialTicks) {
		setCloudRenderer(new CloudRenderBlank());
		return new Vec3d(1, 1, 1);
	}

	@Override
	public boolean isSurfaceWorld() {
		return false;
	}

	@Override
	protected void generateLightBrightnessTable() {
		float f = 0.0F;

		for (int i = 0; i <= 15; ++i) {
			float f1 = 1.0F - (float) i / 15.0F;
			this.lightBrightnessTable[i] = (15);
		}
	}

	@Override
	public double getHorizon() {
		return world.getHeight() - 256;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getFogColor(float par1, float par2) {
		return new Vec3d(1, 1, 1);
	}

	@Override
	public int getActualHeight() {
		return -256;
	}
}
