package dimdoors.common.tileentity;


import net.minecraft.util.ITickable;
import net.minecraftforge.common.ForgeChunkManager;

public class TileEntityDimDoorGold extends TileEntityDimDoor implements ITickable/*IChunkLoader TODO*/ {

	private ForgeChunkManager.Ticket chunkTicket;
	private boolean initialized = false;

	/*@Override
	public boolean isInitialized() TODO
	{
		return initialized;
	}*/


	@Override
	public void update() {
		if (!initialized) {
			//			initialize(null); TODO
		}
	}

	/*@Override TODO
	public void initialize(Ticket ticket) {
		initialized = true;
		chunkTicket = ticket;

		// Only do anything if this function is running on the server side
		// NOTE: We don't have to check whether this block is the upper door
		// block or the lower one because only one of them should have a
		// link associated with it.
		if (!worldObj.isRemote) {
			NewDimData dimension = PocketManager.createDimensionData(worldObj);

			// Check whether a ticket has already been assigned to this door
			if (chunkTicket == null) {
				// No ticket yet.
				// Check if this area should be loaded and request a new ticket.
				if (isValidChunkLoaderSetup(dimension)) {
					chunkTicket = ChunkLoaderHelper.createTicket(xCoord, yCoord, zCoord, worldObj);
				}
			} else {
				// A ticket has already been provided.
				// Check if this area should be loaded. If not, release the ticket.
				if (!isValidChunkLoaderSetup(dimension)) {
					ForgeChunkManager.releaseTicket(chunkTicket);
					chunkTicket = null;
				}
			}

			// If chunkTicket isn't null at this point, then this is a valid door setup.
			// The last step is to request force loading of the pocket's chunks.
			if (chunkTicket != null) {
				ChunkLoaderHelper.forcePocketChunks(dimension, chunkTicket);
			}
		}
	}*/

	/*private boolean isValidChunkLoaderSetup(NewDimData dimension) { TODO
		// Check the various conditions that make this a valid door setup.
		// 1. The door must be inside the pocket's XZ boundaries,
		//		to prevent loading of chunks with a distant door
		// 2. The dimension must be a pocket dimension
		// 3. The door must be linked so that it's clear that it's not a normal door

		return (dimension.isPocketDimension() && dimension.getLink(xCoord, yCoord, zCoord) != null && PocketBuilder.calculateDefaultBounds(dimension).contains(xCoord, yCoord, zCoord));
	}

	@Override
	public void invalidate() { TODO
		ForgeChunkManager.releaseTicket(chunkTicket);
		super.invalidate();
	}*/
}
