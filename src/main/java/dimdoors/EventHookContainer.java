package dimdoors;


import dimdoors.common.core.DDTeleporter;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.NewDimData;
import dimdoors.common.core.PocketManager;
import dimdoors.common.core.RiftRegenerator;
import dimdoors.common.items.BaseItemDoor;
import dimdoors.common.util.DimensionPos;
import dimdoors.common.world.LimboProvider;
import dimdoors.common.world.PocketProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = DimDoors.modid)
public class EventHookContainer {

	private static final int MAX_FOOD_LEVEL = 20;

	private static RiftRegenerator regenerator;

	public void setSessionFields(RiftRegenerator regenerator) {
		// SenseiKiwi:
		// Why have a setter rather than accessing mod_pocketDim directly?
		// I want to make this dependency explicit in our code.
		EventHookContainer.regenerator = regenerator;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onInitMapGen(InitMapGenEvent event) {
		// Replace the Nether fortress generator with our own only if any
		// gateways would ever generate. This allows admins to disable our
		// fortress overriding without disabling all gateways.
		/*
		 * if (properties.FortressGatewayGenerationChance > 0 &&
		 * properties.WorldRiftGenerationEnabled && event.type ==
		 * InitMapGenEvent.EventType.NETHER_BRIDGE) { event.newGen = new
		 * DDNetherFortressGenerator(); }
		 */
	}

	@SubscribeEvent
	public static void onPlayerEvent(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		ItemStack stack = event.getItemStack();
			/*if (stack.getItem() instanceof ItemWarpDoor) { TODO
				NewDimData data = PocketManager.getDimensionData(world);

				if (data.type() == DimensionType.PERSONAL) {
					mod_pocketDim.sendChat(event.entityPlayer, ("Something prevents the Warp Door from tunneling out here"));
					event.setCanceled(true);
					return;
				}
			}*/
			if (BaseItemDoor.tryToPlaceDoor(stack, event.getEntityPlayer(), world, event.getPos(), event.getFace())) {
				// Cancel the event so that we don't get two doors from vanilla doors
				event.setCanceled(true);
			}

	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		// We need to initialize PocketManager here because onServerAboutToStart
		// fires before we can use DimensionManager and onServerStarting fires
		// after the game tries to generate terrain. If a gateway tries to
		// generate before PocketManager has initialized, we get a crash.
		if (!event.getWorld().isRemote && !PocketManager.isLoaded()) {
			PocketManager.load();
		}
	}

	@SubscribeEvent
	public static void onPlayerFall(LivingFallEvent event) {
		event.setCanceled(event.getEntity().world.provider.getDimension() == DimDoorsConfig.category_dimension.limboDimensionID);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static boolean onDeathWithHighPriority(LivingDeathEvent event) {
		// Teleport the entity to Limbo if it's a player in a pocket dimension
		// and if Limbo preserves player inventories. We'll check again in a
		// low-priority event handler to give other mods a chance to save the
		// player if Limbo does _not_ preserve inventories.

		Entity entity = event.getEntity();

		if (DimDoorsConfig.limboEnabled && DimDoorsConfig.limboReturnsInventoryEnabled && entity instanceof EntityPlayer && isValidSourceForLimbo(entity.world.provider)) {
			if (entity.world.provider instanceof PocketProvider) {
				EntityPlayer player = (EntityPlayer) entity;
//				mod_pocketDim.deathTracker.addUsername(player.getGameProfile().getName()); TODO
				revivePlayerInLimbo(player);
				event.setCanceled(true);
				return false;
			} else if (entity.world.provider instanceof LimboProvider && event.getSource() == DamageSource.OUT_OF_WORLD) {
				EntityPlayer player = (EntityPlayer) entity;
				revivePlayerInLimbo(player);
//				mod_pocketDim.sendChat(player, "Search for the dark red pools which accumulate in the lower reaches of Limbo"); TODO
				event.setCanceled(true);
				return false;
			}
		}
		return true;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static boolean onDeathWithLowPriority(LivingDeathEvent event) {
		// This low-priority handler gives mods a chance to save a player from
		// death before we apply teleporting them to Limbo _without_ preserving
		// their inventory. We also check if the player died in a pocket
		// dimension and record it, regardless of whether the player will be
		// sent to Limbo.

		Entity entity = event.getEntity();

		if (entity instanceof EntityPlayer && isValidSourceForLimbo(entity.world.provider)) {
			EntityPlayer player = (EntityPlayer) entity;
//			mod_pocketDim.deathTracker.addUsername(player.getGameProfile().getName()); TODO

			if (DimDoorsConfig.limboEnabled && !DimDoorsConfig.limboReturnsInventoryEnabled) {
				player.inventory.clear();
				revivePlayerInLimbo(player);
				event.setCanceled(true);
			}
			return false;
		}
		return true;
	}

	private static boolean isValidSourceForLimbo(WorldProvider provider) {
		// Returns whether a given world is a valid place for sending a player
		// to Limbo. We can send someone to Limbo from a certain dimension if
		// Universal Limbo is enabled and the source dimension is not Limbo, or
		// if the source dimension is a pocket dimension.

		return false;//TODO (worldProperties.UniversalLimboEnabled && provider.dimensionId != properties.LimboDimensionID) || (provider instanceof PocketProvider);
	}

	private static void revivePlayerInLimbo(EntityPlayer player) {
		player.extinguish();
		player.clearActivePotions();
		player.setHealth(player.getMaxHealth());
		player.getFoodStats().addStats(MAX_FOOD_LEVEL, 0);
		DimensionPos destination = LimboProvider.getLimboSkySpawn(player);
		DDTeleporter.teleportEntity(player, destination, false);
	}

	@SubscribeEvent
	public static void onWorldSave(WorldEvent.Save event) {
		if (event.getWorld().provider.getDimension() == 0) {
			PocketManager.save(true);

			/*if (mod_pocketDim.deathTracker != null && mod_pocketDim.deathTracker.isModified()) {
				mod_pocketDim.deathTracker.writeToFile();
			}*/ // TODO
		}
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event) {
		// Schedule rift regeneration for any links located in this chunk.
		// This event runs on both the client and server. Allow server only.
		// Also, check that PocketManager is loaded, because onChunkLoad() can
		// fire while chunks are being initialized in a new world, before
		// onWorldLoad() fires.
		Chunk chunk = event.getChunk();
		if (!chunk.getWorld().isRemote && PocketManager.isLoaded()) {
			NewDimData dimension = PocketManager.createDimensionData(chunk.getWorld());
			for (DimLink link : dimension.getChunkLinks(chunk.x, chunk.z)) {
				regenerator.scheduleSlowRegeneration(link);
			}
		}
	}
}