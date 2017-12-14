package dimdoors;

import dimdoors.common.blocks.BlockRift;
import dimdoors.common.world.fortresses.DDStructureNetherBridgeStart;
import dimdoors.common.world.gateways.GatewayGenerator;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = DimDoors.modid)
@Config(modid = DimDoors.modid)
public class DimDoorsConfig {

	@Config.Name("Dimension")
	public static Dimension category_dimension = new Dimension();

	public static class Dimension {

		@Config.Name("Limbo Dimension ID")
		public int limboDimensionID = -23;
		@Config.Name("Pocket Provider ID")
		public int pocketProviderID = 124;
		@Config.Name("Personal Pocket Provider ID")
		public int personalPocketProviderID = 125;

	}

	@Config.Name("Loot")
	public static Loot category_loot = new Loot();

	public static class Loot {

		@Config.Name("Enable Rift Blade Loot")
		public static boolean riftBladeLootEnabled = true;

		@Config.Name("Enable Fabric of Reality Loot")
		public static boolean fabricOfRealityLootEnabled = true;

		@Config.Name("Enable World Thread Loot")
		public static boolean worldThreadLootEnabled = true;

	}

	@Config.Name("Enable Rift Spread")
	@Config.Comment("Sets whether rifts create more rifts when they are near other rifts")
	public static boolean riftSpreadEnabled = true;

	@Config.Name("Enable Rift Griefing")
	@Config.Comment("Sets whether rifts destroy blocks around them or not")
	public static boolean riftGriefingEnabled = true;

	@Config.Name("Enable Endermen Spawning from Rifts")
	@Config.Comment("Sets whether groups of connected rifts will spawn Endermen")
	public static boolean riftsSpawnEndermenEnabled = true;

	@Config.Name("Enable Limbo")
	@Config.Comment("Sets whether players are teleported to Limbo when they die in any pocket dimension")
	public static boolean limboEnabled = true;

	@Config.Name("Enable Hardcore Limbo")
	@Config.Comment("Sets whether players that die in Limbo will respawn there")
	public static boolean hardcoreLimboEnabled = false;

	@Config.Name("Enable Limbo Returns Inventory")
	@Config.Comment("Sets whether players keep their inventories upon dying and respawning in Limbo")
	public static boolean limboReturnsInventoryEnabled = true;

	@Config.Name("Enable Door Rendering")
	public static boolean doorRenderingEnabled = true;

	@Config.Name("EXPLOSIONS!!???!!!?!?!!")
	public static boolean tntEnabled = false;

	@Config.Name("Enable Monolith Teleportation")
	@Config.Comment("Sets whether Monoliths can teleport players")
	public static boolean monolithTeleportationEnabled = true;

	@Config.Name("Docile Monoliths in Limbo")
	@Config.Comment("Sets whether monoliths in Limbo stare at the player rather than attack")
	public static boolean dangerousLimboMonolithsDisabled = true;

	@Config.Name("HOWMUCHTNT")
	@Config.Comment("Weighs the chance that a block will not be TNT. Must be greater than or equal to 0. EXPLOSIONS must be set to true for this to have any effect.")
	public static int nonTntWeight = 25;

	@Config.Name("Cluster Generation Chance")
	@Config.Comment("Sets the chance (out of " + GatewayGenerator.MAX_CLUSTER_GENERATION_CHANCE + ") that a cluster of rifts will generate in a given chunk. The default chance is 2.")
	public static int clusterGenerationChance = 2;

	@Config.Name("Gateway Generation Chance")
	@Config.Comment("Sets the chance (out of " + GatewayGenerator.MAX_GATEWAY_GENERATION_CHANCE + ") that a Rift Gateway will generate in a given chunk. The default chance is 15.")
	public static int gatewayGenerationChance = 15;

	@Config.Name("Fortress Gateway Generation Chance")
	@Config.Comment("Sets the chance (out of " + DDStructureNetherBridgeStart.MAX_GATEWAY_GENERATION_CHANCE + ") that a Rift Gateway will generate as part of a Nether Fortress. The default chance is 33.")
	public static int fortressGatewayGenerationChance = 33;

	@Config.Name("Monolith Spawning Chance")
	@Config.Comment("Sets the chance (out of 100) that Monoliths will spawn in a given Limbo chunk. The default chance is 28.")
	public static int monolithSpawningChance = 28;

	@Config.Name("World Thread Drop Chance")
	@Config.Comment("Sets the chance (out of " + BlockRift.MAX_WORLD_THREAD_DROP_CHANCE + ") that a rift will drop World Thread when it destroys a block. The default chance is 50.")
	public static int worldThreadDropChance = 50;

	@Config.Name("Limbo Entry Range")
	@Config.Comment("Sets the farthest distance that players may be moved at random when sent to Limbo. Must be greater than or equal to 0.")
	public static int limboEntryRange = 500;

	@Config.Name("Limbo Return Range")
	@Config.Comment("Sets the farthest distance that players may be moved at random when sent from Limbo to the Overworld. Must be greater than or equal to 0.")
	public static int limboReturnRange = 500;

	@Config.Name("World Thread Requirement Level")
	@Config.Comment("Controls the amount of World Thread needed to craft Stable Fabric. The number must be an integer from 1 to 4. The levels change the recipe to use 1, 2, 4, or 8 threads, respectively. The default level is 4.")
	public static int worldThreadRequirementLevel = 4;

	public static String customSchematicDirectory = "";

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(DimDoors.modid))
			ConfigManager.sync(DimDoors.modid, Config.Type.INSTANCE);
	}

}
