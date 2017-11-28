package dimdoors.common.world;


import dimdoors.DimDoorsConfig;
import dimdoors.client.world.CloudRenderBlank;
import dimdoors.common.util.DimensionPos;
import dimdoors.common.util.RandomBetween;
import dimdoors.registry.DimBiomes;
import dimdoors.registry.DimBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LimboProvider extends WorldProvider {

	private IRenderHandler skyRenderer;

	public LimboProvider() {
		nether = false;
		this.skyRenderer = new LimboSkyProvider();
	}

	public static DimensionPos getLimboSkySpawn(EntityPlayer player) {
		int x = (int) (player.posX) + RandomBetween.getRandomIntBetween(player.world.rand, -DimDoorsConfig.limboEntryRange, DimDoorsConfig.limboEntryRange);
		int z = (int) (player.posZ) + RandomBetween.getRandomIntBetween(player.world.rand, -DimDoorsConfig.limboEntryRange, DimDoorsConfig.limboEntryRange);
		return new DimensionPos(x, 700, z, DimDoorsConfig.category_dimension.limboDimensionID);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return this.skyRenderer;
	}

	@Nonnull
	@Override
	public Biome getBiomeForCoords(@Nonnull BlockPos pos) {
		return DimBiomes.limboBiome;
	}

	@Override
	public boolean canRespawnHere() {
		return DimDoorsConfig.hardcoreLimboEnabled;
	}

	@Override
	public boolean isBlockHighHumidity(BlockPos pos) {
		return false;
	}

	@Override
	public boolean canSnowAt(BlockPos pos, boolean checkLight) {
		return false;
	}

	@Override
	protected void generateLightBrightnessTable() {
		float modifier = 0.0F;

		for (int steps = 0; steps <= 15; ++steps) {
			float var3 = 1.0F - steps / 15.0F;
			this.lightBrightnessTable[steps] = ((0.0F + var3) / (var3 * 3.0F + 1.0F) * (1.0F - modifier) + modifier) * 3;
			//     System.out.println( this.lightBrightnessTable[steps]+"light");
		}
	}

	@Nullable
	@Override
	public BlockPos getSpawnCoordinate() {
		return super.getRandomizedSpawnPoint();
	}

	@Override
	public float calculateCelestialAngle(long par1, float par3) {
		int var4 = (int) (par1 % 24000L);
		float var5 = (var4 + par3) / 24000.0F - 0.25F;

		if (var5 < 0.0F) {
			++var5;
		}

		if (var5 > 1.0F) {
			--var5;
		}

		float var6 = var5;
		var5 = 1.0F - (float) ((Math.cos(var5 * Math.PI) + 1.0D) / 2.0D);
		var5 = var6 + (var5 - var6) / 3.0F;
		return 0;
	}

	@SideOnly(Side.CLIENT)
	public int getMoonPhase(long par1, float par3) {
		return 4;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public String getSaveFolder() {
		return (getDimension() == 0 ? super.getSaveFolder() : "DimensionalDoors/Limbo" + getDimension());
	}

	@Override
	public boolean canCoordinateBeSpawn(int x, int z) {
		Block block = world.getBlockState(world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z))).getBlock();
		return block == DimBlocks.blockLimbo;
	}

	@Override
	public double getHorizon() {
		return world.getHeight() / 4 - 800;
	}

	@Nonnull
	@Override
	public DimensionType getDimensionType() {
		return DimensionType.getById(DimDoorsConfig.category_dimension.limboDimensionID);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getSkyColor(@Nonnull Entity cameraEntity, float partialTicks) {
		setCloudRenderer(new CloudRenderBlank()); // TODO: ?????? move this elsewhere wtf
		return new Vec3d(0, 0, 0);

	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getFogColor(float par1, float par2) {
		return new Vec3d(0.2D, 0.2D, 0.2D);

	}

	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		return 0;
	}

	@Nonnull
	@Override
	public IChunkGenerator createChunkGenerator() {
		//TODO: ...We're passing the LimboGenerator a fixed seed. We should be passing the world seed! @_@ ~SenseiKiwi
		return new LimboGenerator(world, 45);
	}

	@Override
	public boolean canBlockFreeze(BlockPos pos, boolean byWater) {
		return false;
	}

	@Nonnull
	@Override
	public BlockPos getRandomizedSpawnPoint() {
		int x = RandomBetween.getRandomIntBetween(world.rand, -500, 500);
		int z = RandomBetween.getRandomIntBetween(world.rand, -500, 500);
		return new BlockPos(x, 700, z);
	}
}