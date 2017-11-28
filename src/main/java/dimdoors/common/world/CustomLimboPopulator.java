package dimdoors.common.world;

import dimdoors.DimDoors;
import dimdoors.DimDoorsConfig;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.entity.EntityMonolith;
import dimdoors.common.helpers.YCoordHelper;
import dimdoors.common.util.ChunkLocation;
import dimdoors.registry.DimBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = DimDoors.modid)
public class CustomLimboPopulator {

	public static final int MAX_MONOLITH_SPAWNING_CHANCE = 100;
	private static final String MOB_SPAWNING_RULE = "doMobSpawning";
	private static final int MAX_MONOLITH_SPAWN_Y = 245;
	private static final int CHUNK_SIZE = 16;
	private static final int MONOLITH_SPAWNING_INTERVAL = 1;

	private static final ConcurrentLinkedQueue<ChunkLocation> locations = new ConcurrentLinkedQueue<>();

	public static boolean isMobSpawningAllowed() {
		//This function is used to retrieve the value of doMobSpawning. The code is the same
		//as the code used by Minecraft. Jaitsu requested this to make testing easier. ~SenseiKiwi

		GameRules rules = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getGameRules();
		return rules.getBoolean(MOB_SPAWNING_RULE);
	}

	@SubscribeEvent
	public static void onTick(TickEvent.WorldTickEvent e) {
		if (e.world.isRemote || e.phase != TickEvent.Phase.END)
			return;

		World limboWorld = null;

		// Check if any new spawning requests have come in
		if (!locations.isEmpty()) {
			// Check if mob spawning is allowed
			if (isMobSpawningAllowed()) {
				// Loop over the locations and call the appropriate function depending
				// on whether the request is for Limbo or for a pocket dimension.
				for (ChunkLocation location : locations) {
					if (location.getDimension() == DimDoorsConfig.category_dimension.limboDimensionID) {
						// Limbo chunk

						// SenseiKiwi: Check if we haven't loaded Limbo for another request in this request
						// cycle. If so, try to load Limbo up. This solves a strange issue with ChickenChunks
						// where CC somehow forces chunks to generate in Limbo if LimboProvider.canRespawnHere()
						// is true, yet when execution reaches this point, Limbo isn't loaded anymore! My theory
						// is that CC force-loads a chunk for some reason, but since there are no players around,
						// Limbo immediately unloads after standard world gen runs, and before this code can run.

						if (limboWorld == null) {
							limboWorld = PocketManager.loadDimension(DimDoorsConfig.category_dimension.limboDimensionID);
							if (limboWorld == null)
								return; // Give up
						}
						placeMonolithsInLimbo(limboWorld, location.getChunkPos().x, location.getChunkPos().z);
						DimDoors.gatewayGenerator.generate(limboWorld.rand, location.getChunkPos().x, location.getChunkPos().z, limboWorld, null, limboWorld.getChunkProvider());
					} else {
						//Pocket dimension chunk
						placeMonolithsInPocket(location.getDimension(), location.getChunkPos());
					}
				}
			}

			locations.clear();
		}
	}

	public static void registerChunkForPopulation(int dimensionID, ChunkPos chunkpos) {
		ChunkLocation location = new ChunkLocation(dimensionID, chunkpos);
		locations.add(location);
	}

	private static void placeMonolithsInPocket(int dimensionID, ChunkPos chunkpos) {
		NewDimData dimension = PocketManager.getDimensionData(dimensionID);
		World pocket = DimensionManager.getWorld(dimensionID);

		if (pocket == null || dimension == null || dimension.dungeon() == null || dimension.dungeon().isOpen()) {
			return;
		}

		int sanity = 0;
		Block block;
		boolean didSpawn = false;

		//The following initialization code is based on code from ChunkProviderGenerate.
		//It makes our generation depend on the world seed.
		Random random = new Random(pocket.getSeed() ^ 0xA210FE65F20017D6L);
		long factorA = random.nextLong() / 2L * 2L + 1L;
		long factorB = random.nextLong() / 2L * 2L + 1L;
		random.setSeed(chunkpos.x * factorA + chunkpos.z * factorB ^ pocket.getSeed());

		//The following code really, really needs to be rewritten... "sanity" is not a proper variable name. ~SenseiKiwi
		int x, y, z;
		do {
			//Select a random column within the chunk
			x = chunkpos.x * CHUNK_SIZE + random.nextInt(CHUNK_SIZE);
			z = chunkpos.z * CHUNK_SIZE + random.nextInt(CHUNK_SIZE);
			y = MAX_MONOLITH_SPAWN_Y;

			while (pocket.isAirBlock(new BlockPos(x, y, z)) && y > 0)
				y--;
			block = pocket.getBlockState(new BlockPos(x, y, z)).getBlock();
			while ((block == DimBlocks.blockDimWall || block == DimBlocks.blockDimWallPerm) && y > 0) {
				y--;
				block = pocket.getBlockState(new BlockPos(x, y, z)).getBlock();
			}
			while (pocket.isAirBlock(new BlockPos(x, y, z)) && y > 0)
				y--;
			if (y > 0) {
				int jumpSanity = 0;
				int jumpHeight;
				do {
					jumpHeight = y + random.nextInt(10);
					jumpSanity++;
				} while (!pocket.isAirBlock(new BlockPos(x, jumpHeight + 6, z)) && jumpSanity < 20);

				EntityMonolith monolith = new EntityMonolith(pocket);
				monolith.setLocationAndAngles(x, jumpHeight - (5 - monolith.getRenderSizeModifier() * 5), z, 1, 1);
				pocket.spawnEntity(monolith);
				didSpawn = true;
			}
			sanity++;
		} while (sanity < 5 && !didSpawn);
	}

	private static void placeMonolithsInLimbo(World limbo, int chunkX, int chunkZ) {
		//The following initialization code is based on code from ChunkProviderGenerate.
		//It makes our generation depend on the world seed.
		Random random = new Random(limbo.getSeed() ^ 0xB5130C4ACC71A822L);
		long factorA = random.nextLong() / 2L * 2L + 1L;
		long factorB = random.nextLong() / 2L * 2L + 1L;
		random.setSeed(chunkX * factorA + chunkZ * factorB ^ limbo.getSeed());

		//Okay, the following code is full of magic constants and makes little sense. =/ ~SenseiKiwi
		if (random.nextInt(MAX_MONOLITH_SPAWNING_CHANCE) < DimDoorsConfig.monolithSpawningChance) {
			int y = 0;
			int yTest;
			do {
				int x = chunkX * CHUNK_SIZE + random.nextInt(CHUNK_SIZE);
				int z = chunkZ * CHUNK_SIZE + random.nextInt(CHUNK_SIZE);

				while (limbo.isAirBlock(new BlockPos(x, y, z)) && y < 255)
					y++;
				y = YCoordHelper.getFirstUncovered(limbo, x, y + 2, z);
				yTest = YCoordHelper.getFirstUncovered(limbo, x, y + 5, z);
				if (yTest > 245) {
					return;
				}

				int jumpSanity = 0;
				int jumpHeight;
				do {
					jumpHeight = y + random.nextInt(25);
					jumpSanity++;
				} while (!limbo.isAirBlock(new BlockPos(x, jumpHeight + 6, z)) && jumpSanity < 20);


				EntityMonolith monolith = new EntityMonolith(limbo);
				monolith.setLocationAndAngles(x, jumpHeight, z, 1, 1);
				limbo.spawnEntity(monolith);
			} while (yTest > y);
		}
	}
}
