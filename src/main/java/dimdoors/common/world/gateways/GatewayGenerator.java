package dimdoors.common.world.gateways;


import com.google.common.collect.Lists;
import dimdoors.DimDoorsConfig;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.world.PocketProvider;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.List;
import java.util.Random;

public class GatewayGenerator implements IWorldGenerator {

	public static final int MAX_GATEWAY_GENERATION_CHANCE = 10000;
	public static final int MAX_CLUSTER_GENERATION_CHANCE = 10000;
	private static final int CLUSTER_GROWTH_CHANCE = 80;
	private static final int MAX_CLUSTER_GROWTH_CHANCE = 100;
	private static final int MIN_RIFT_Y = 4;
	private static final int MAX_RIFT_Y = 240;
	private static final int CHUNK_LENGTH = 16;
	private static final int GATEWAY_RADIUS = 4;
	private static final int MAX_GATEWAY_GENERATION_ATTEMPTS = 10;
	private static final int OVERWORLD_DIMENSION_ID = 0;
	private static final int NETHER_DIMENSION_ID = -1;
	private static final int END_DIMENSION_ID = 1;
	private static final String SPIRIT_WORLD_NAME = "Spirit World";

	private List<BaseGateway> gateways;
	private BaseGateway defaultGateway;

	public GatewayGenerator() {
		this.initialize();
	}

	private static boolean checkGatewayLocation(World world, BlockPos pos) {
		//Check if the point is within the acceptable altitude range, the block above that point is empty,
		//and the block two levels down is opaque and has a reasonable material. Plus that we're not building
		//on top of bedrock.
		return (pos.getY() >= MIN_RIFT_Y && pos.getY() <= MAX_RIFT_Y && world.isAirBlock(pos.up()) && world.getBlockState(pos).getBlock() != Blocks.BEDROCK &&    //<-- Stops Nether roof spawning. DO NOT REMOVE!
				world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK && checkFoundationMaterial(world, pos.down(2)));
	}

	private static boolean checkFoundationMaterial(World world, BlockPos pos) {
		//We check the material and opacity to prevent generating gateways on top of trees or houses,
		//or on top of strange things like tall grass, water, slabs, or torches.
		//We also want to avoid generating things on top of the Nether's bedrock!
		Material material = world.getBlockState(pos).getMaterial();
		return (material != Material.LEAVES && material != Material.WOOD && material != Material.GOURD && world.isBlockNormalCube(pos, false) && world.getBlockState(pos).getBlock() != Blocks.BEDROCK);
	}

	private void initialize() {
		gateways = Lists.newArrayList();
		defaultGateway = new GatewayTwoPillars();

		// Add gateways here
		gateways.add(new GatewaySandstonePillars());
		gateways.add(new GatewayLimbo());
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		// Don't generate rifts or gateways if the current world is a pocket dimension or the world is remote.
		// Also don't generate anything in the Nether, The End, or in Witchery's Spirit World.
		// We only match against Spirit World using hashing to speed up the process a little (hopefully).
		int dimensionID = world.provider.getDimension();
		if (world.isRemote || (world.provider instanceof PocketProvider) || (dimensionID == END_DIMENSION_ID) || (dimensionID == NETHER_DIMENSION_ID)/* || (world.provider.getDimensionName().hashCode() == SPIRIT_WORLD_NAME.hashCode())*/) {
			return;
		}
		// This check prevents a crash related to superflat worlds not loading World 0
		if (DimensionManager.getWorld(OVERWORLD_DIMENSION_ID) == null) {
			return;
		}

		int x, y, z;
		int attempts;
		boolean valid;
		DimLink link;
		NewDimData dimension;

		// Check if we're allowed to generate rift clusters in this dimension.
		// If so, randomly decide whether to one.
		if (/*TODO mod_pocketDim.worldProperties.RiftClusterDimensions.isAccepted(dimensionID) && */random.nextInt(MAX_CLUSTER_GENERATION_CHANCE) < DimDoorsConfig.clusterGenerationChance) {
			link = null;
			dimension = null;
			do {
				//Pick a random point on the surface of the chunk
				x = chunkX * CHUNK_LENGTH + random.nextInt(CHUNK_LENGTH);
				z = chunkZ * CHUNK_LENGTH + random.nextInt(CHUNK_LENGTH);
				y = world.getHeight(x, z);
				BlockPos pos = new BlockPos(x, y, z);

				//If the point is within the acceptable altitude range, the block above is empty, and we're
				//not building on bedrock, then generate a rift there
				if (y >= MIN_RIFT_Y && y <= MAX_RIFT_Y && world.isAirBlock(pos.up()) && world.getBlockState(pos).getBlock() != Blocks.BEDROCK &&    //<-- Stops Nether roof spawning. DO NOT REMOVE!
						world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK && world.getBlockState(pos.down(2)).getBlock() != Blocks.BEDROCK) {
					//Create a link. If this is not the first time, create a child link and connect it to the first link.
					if (link == null) {
						dimension = PocketManager.getDimensionData(world);
						link = dimension.createLink(x, y + 1, z, LinkType.DUNGEON, 0);
					} else {
						dimension.createChildLink(new BlockPos(x, y + 1, z), link);
					}
				}
			}
			//Randomly decide whether to repeat the process and add another rift to the cluster
			while (random.nextInt(MAX_CLUSTER_GROWTH_CHANCE) < CLUSTER_GROWTH_CHANCE);
		}

		// Check if we can place a Rift Gateway in this dimension, then randomly decide whether to place one.
		// This only happens if a rift cluster was NOT generated.
		else if (/*TODO mod_pocketDim.worldProperties.RiftGatewayDimensions.isAccepted(dimensionID) && */random.nextInt(MAX_GATEWAY_GENERATION_CHANCE) < DimDoorsConfig.gatewayGenerationChance) {
			valid = false;
			x = y = z = 0; //Stop the compiler from freaking out

			//Check locations for the gateway until we are satisfied or run out of attempts.
			for (attempts = 0; attempts < MAX_GATEWAY_GENERATION_ATTEMPTS && !valid; attempts++) {
				//Pick a random point on the surface of the chunk and check its materials
				x = chunkX * CHUNK_LENGTH + random.nextInt(CHUNK_LENGTH);
				z = chunkZ * CHUNK_LENGTH + random.nextInt(CHUNK_LENGTH);
				y = world.getHeight(x, z);
				BlockPos pos = new BlockPos(x, y, z);
				valid = checkGatewayLocation(world, pos);
			}

			// Build the gateway if we found a valid location
			if (valid) {
				List<BaseGateway> validGateways = Lists.newArrayList();
				for (BaseGateway gateway : gateways) {
					if (gateway.isLocationValid(world, x, y, z)) {
						validGateways.add(gateway);
					}
				}
				// Add the default gateway if the rest were rejected
				if (validGateways.isEmpty()) {
					validGateways.add(defaultGateway);
				}
				// Randomly select a gateway from the pool of viable gateways
				validGateways.get(random.nextInt(validGateways.size())).generate(world, x, y - 1, z);
			}
		}
	}
}
