package dimdoors;

import dimdoors.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

@Mod(modid = DimDoors.modid, name = "Dimensional Doors Reborn", version = DimDoors.version, acceptedMinecraftVersions = "[1.12,)")
public class DimDoors {

	public static final String version = "${version}";
	public static final String modid = "dimdoors";

	@SidedProxy(clientSide = "dimdoors.proxy.ClientProxy", serverSide = "dimdoors.proxy.ServerProxy")
	public static CommonProxy proxy;

	@Mod.Instance(DimDoors.modid)
	public static DimDoors instance;

	@Mod.EventHandler
	public void onPreInitialization(FMLPreInitializationEvent event) {
		proxy.preInit();
		//		DimDoorsNetwork.init(); TODO
	}

	@Mod.EventHandler
	public void onInitialization(FMLInitializationEvent event) {
		proxy.init();
		// Initialize LimboDecay instance: required for BlockLimbo
		//		limboDecay = new LimboDecay(properties); TODO

		//		DungeonHelper.initialize(); TODO
		//		gatewayGenerator = new GatewayGenerator(properties); TODO
		//		GameRegistry.registerWorldGenerator(mod_pocketDim.gatewayGenerator, 0); TODO

		// Register loot chests
		//		DDLoot.registerInfo(properties); TODO
	}

	@Mod.EventHandler
	public void onPostInitialization(FMLPostInitializationEvent event) {
		proxy.postInit();
		//		ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoaderHelper()); TODO
	}

	@Mod.EventHandler
	public void onServerStopped(FMLServerStoppedEvent event) {
		/*try { TODO
			PocketManager.tryUnload();
			if (deathTracker != null) {
				deathTracker.writeToFile();
				deathTracker = null;
			}
			worldProperties = null;
			currrentSaveRootDirectory = null;

			// Unregister all tick receivers from serverTickHandler to avoid leaking
			// scheduled tasks between single-player game sessions
			if (serverTickHandler != null)
				serverTickHandler.unregisterReceivers();
			spawner = null;
			riftRegenerator = null;
			limboDecayScheduler = null;
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	@Mod.EventHandler
	public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
		/*currrentSaveRootDirectory = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath(); TODO

		// Load the config file that's specific to this world
		worldProperties = new DDWorldProperties(new File(currrentSaveRootDirectory + "/DimensionalDoors/DimDoorsWorld.cfg"));

		// Initialize a new DeathTracker
		deathTracker = new DeathTracker(currrentSaveRootDirectory + "/DimensionalDoors/data/deaths.txt");

		// Register regular tick receivers
		// CustomLimboPopulator should be initialized before any provider instances are created
		spawner = new CustomLimboPopulator(serverTickHandler, properties);
		riftRegenerator = new RiftRegenerator(serverTickHandler, blockRift);
		limboDecayScheduler = new LimboDecayScheduler(serverTickHandler, limboDecay);

		hooks.setSessionFields(worldProperties, riftRegenerator);*/
	}

	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		/*// Register commands with the server TODO
		event.registerServerCommand( CommandResetDungeons.instance() );
		event.registerServerCommand( CommandCreateDungeonRift.instance() );
		event.registerServerCommand( CommandListDungeons.instance() );
		event.registerServerCommand( CommandCreateRandomRift.instance() );
		event.registerServerCommand( CommandDeleteRifts.instance() );
		event.registerServerCommand( CommandExportDungeon.instance() );
		event.registerServerCommand( CommandCreatePocket.instance() );
		event.registerServerCommand( CommandTeleportPlayer.instance() );

		try
		{
			ChunkLoaderHelper.loadForcedChunkWorlds(event);
		}
		catch (Exception e)
		{
			System.err.println("Failed to load chunk loaders for Dimensional Doors. The following error occurred:");
			System.err.println(e.toString());
		}*/
	}

}
