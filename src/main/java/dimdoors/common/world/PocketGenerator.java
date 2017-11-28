package dimdoors.common.world;


import com.google.common.collect.Lists;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PocketGenerator implements IChunkGenerator {

	private World world;

	public PocketGenerator(World par1World, long par2, boolean par4) {
		this.world = par1World;
	}

	@Nonnull
	@Override
	public Chunk generateChunk(int chunkX, int chunkZ) {
		ChunkPrimer primer = new ChunkPrimer();
		Chunk chunk = new Chunk(world, primer, chunkX, chunkZ);

		if (!chunk.isTerrainPopulated()) {
			chunk.setTerrainPopulated(true);
			CustomLimboPopulator.registerChunkForPopulation(world.provider.getDimension(), new ChunkPos(chunkX, chunkZ));
		}
		return chunk;
	}

	@Override
	public void populate(int x, int z) {

	}

	@Override
	public boolean generateStructures(@Nonnull Chunk chunkIn, int x, int z) {
		return false;
	}

	@Nonnull
	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType, @Nonnull BlockPos pos) {
		NewDimData dimension = PocketManager.createDimensionData(this.world);
		if (dimension != null && dimension.dungeon() != null && !dimension.dungeon().isOpen()) {
			return this.world.getBiome(pos).getSpawnableList(creatureType);
		}
		return Lists.newArrayList();
	}

	@Nullable
	@Override
	public BlockPos getNearestStructurePos(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos position, boolean findUnexplored) {
		return null;
	}

	@Override
	public void recreateStructures(@Nonnull Chunk chunkIn, int x, int z) {

	}

	@Override
	public boolean isInsideStructure(@Nonnull World worldIn, @Nonnull String structureName, @Nonnull BlockPos pos) {
		return false;
	}
}