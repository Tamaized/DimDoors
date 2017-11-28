package dimdoors.common.world;


import dimdoors.DimDoorsConfig;
import dimdoors.client.world.CloudRenderBlank;
import dimdoors.common.core.DimType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class PocketProvider extends WorldProvider {

	protected IRenderHandler skyRenderer;

	public PocketProvider() {
		this.nether = true;
		this.skyRenderer = new PocketSkyProvider();
	}

	@Nonnull
	@Override
	public String getSaveFolder() {
		return (getDimension() == 0 ? super.getSaveFolder() : "DimensionalDoors/pocketDimID" + getDimension());
	}

	@Nonnull
	@Override
	public Vec3d getSkyColor(@Nonnull Entity cameraEntity, float partialTicks) {
		setCloudRenderer(new CloudRenderBlank());
		return new Vec3d(0, 0, 0);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getFogColor(float par1, float par2) {
		return new Vec3d(0, 0, 0);
	}

	@Override
	public double getHorizon() {
		return world.getHeight();
	}

	@Nonnull
	@Override
	public DimensionType getDimensionType() {
		return DimensionType.getById(getDimension());
	}

	@Nonnull
	@Override
	public IChunkGenerator createChunkGenerator() {
		return new PocketGenerator(world, getDimension(), false);
	}

	@Override
	public boolean canSnowAt(@Nonnull BlockPos pos, boolean checkLight) {
		return false;
	}

	@Override
	public boolean canBlockFreeze(@Nonnull BlockPos pos, boolean byWater) {
		return false;
	}

	@Override
	public float calculateCelestialAngle(long par1, float par3) {
		return 0.5F;
	}

	@Override
	protected void generateLightBrightnessTable() {
		if (!PocketManager.isLoaded()) {
			super.generateLightBrightnessTable();
			return;
		}

		NewDimData data = PocketManager.getDimensionData(getDimension());
		if (data == null || data.type() == DimType.POCKET) {
			super.generateLightBrightnessTable();
			return;
		}
		float modifier = 0.0F;

		for (int steps = 0; steps <= 15; ++steps) {
			float var3 = (float) (Math.pow(steps, 1.5) / Math.pow(15.0F, 1.5));
			this.lightBrightnessTable[15 - steps] = var3;
		}
	}

	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		int respawnDim;

		if (DimDoorsConfig.limboEnabled) {
			respawnDim = DimDoorsConfig.category_dimension.limboDimensionID;
		} else {
			respawnDim = PocketManager.getDimensionData(getDimension()).root().id();
		}
		// TODO: Are we sure we need to load the dimension as well? Why can't the game handle that?
		PocketManager.loadDimension(respawnDim);
		return respawnDim;
	}

	@Override
	public boolean canRespawnHere() {
		return false;
	}

	@Override
	public int getActualHeight() {
		return 256;
	}
}
