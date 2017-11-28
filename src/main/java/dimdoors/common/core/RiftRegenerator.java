package dimdoors.common.core;


import dimdoors.DimDoors;
import dimdoors.common.blocks.BlockRift;
import dimdoors.common.util.DimensionPos;
import dimdoors.common.util.RandomBetween;
import dimdoors.registry.DimBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.PriorityQueue;
import java.util.Random;

@Mod.EventBusSubscriber(modid = DimDoors.modid)
public class RiftRegenerator {

	// Ranges of regeneration delays, in seconds
	private static final int MIN_FAST_DELAY = 1;
	private static final int MAX_FAST_DELAY = 3;
	private static final int MIN_SLOW_DELAY = 5;
	private static final int MAX_SLOW_DELAY = 15;
	private static final int MIN_RESCHEDULE_DELAY = 4 * 60;
	private static final int MAX_RESCHEDULE_DELAY = 6 * 60;

	private static final int TICKS_PER_SECOND = 20;
	private static final int RIFT_REGENERATION_INTERVAL = 1; // Check the regeneration queue every tick
	private static Random random = new Random();

	private static long tickCount = 0;
	private static PriorityQueue<RiftTicket> ticketQueue = new PriorityQueue<>();

	@SubscribeEvent
	public static void update(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			return;
		processTicketQueue();
		tickCount++;
	}

	public static void scheduleSlowRegeneration(DimLink link) {
		scheduleRegeneration(link, MIN_SLOW_DELAY, MAX_SLOW_DELAY);
	}

	public static void scheduleSlowRegeneration(BlockPos pos, World world) {
		scheduleRegeneration(PocketManager.getLink(pos, world), MIN_SLOW_DELAY, MAX_SLOW_DELAY);
	}

	public static void scheduleFastRegeneration(BlockPos pos, World world) {
		scheduleRegeneration(PocketManager.getLink(pos, world), MIN_FAST_DELAY, MAX_FAST_DELAY);
	}

	private static void scheduleRegeneration(DimLink link, int minDelay, int maxDelay) {
		if (link != null) {
			int tickDelay = RandomBetween.getRandomIntBetween(random, minDelay * TICKS_PER_SECOND, maxDelay * TICKS_PER_SECOND);
			ticketQueue.add(new RiftTicket(link.source(), tickCount + tickDelay));
		}
	}

	private static void processTicketQueue() {
		RiftTicket ticket;
		while (!ticketQueue.isEmpty() && ticketQueue.peek().timestamp() <= tickCount) {
			ticket = ticketQueue.remove();
			regenerateRift(ticket.location());
		}
	}

	private static void regenerateRift(DimensionPos location) {
		int x = location.getX();
		int y = location.getY();
		int z = location.getZ();

		// Try to regenerate a rift, or possibly reschedule its regeneration.
		// The world for the given location must be loaded.
		World world = DimensionManager.getWorld(location.getDimension());
		if (world == null)
			return;

		// There must be a link at the given location.
		DimLink link = PocketManager.getLink(location);
		if (link == null)
			return;

		// The chunk at the given location must be loaded.
		// Note: ChunkProviderServer.chunkExists() returns whether a chunk is
		// loaded, not whether it has already been created.
		if (!world.getChunkProvider().isChunkGeneratedAt(x >> 4, z >> 4))
			return;

		if (DimBlocks.blockRift instanceof BlockRift) {
			BlockRift rift = (BlockRift) DimBlocks.blockRift;
			// If the location is occupied by an immune DD block, then don't regenerate.
			if (rift.isModBlockImmune(world, location))
				return;

			// If the location is occupied by an immune block, then reschedule.
			if (rift.isBlockImmune(world, location)) {
				scheduleRegeneration(link, MIN_RESCHEDULE_DELAY, MAX_RESCHEDULE_DELAY);
			} else {
				// All of the necessary conditions have been met. Restore the rift!
				IBlockState state = world.getBlockState(location);
				if (world.setBlockState(location, DimBlocks.blockRift.getDefaultState()))
					rift.dropWorldThread(state, world, location, random);
			}
		}
	}

}
