package dimdoors.common.world;

import net.minecraft.world.biome.Biome;

public class DimDoorBiomeBase extends Biome {

	public DimDoorBiomeBase(BiomeProperties properties) {
		super(properties);

		decorator.treesPerChunk = 0;
		this.decorator.flowersPerChunk = 0;
		this.decorator.grassPerChunk = 0;

		this.spawnableMonsterList.clear();
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
	}

	@Override
	public boolean canRain() {
		return false;
	}
}
