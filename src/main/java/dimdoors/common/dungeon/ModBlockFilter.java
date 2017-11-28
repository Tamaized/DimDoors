package dimdoors.common.dungeon;


import dimdoors.common.schematic.SchematicFilter;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.List;

public class ModBlockFilter extends SchematicFilter {

	private List<Block> exceptions;
	private Block replacementBlock;
	private byte replacementMetadata;

	public ModBlockFilter(List<Block> exceptions, Block replacementBlock, byte replacementMetadata) {
		super("ModBlockFilter");
		this.exceptions = exceptions;
		this.replacementBlock = replacementBlock;
		this.replacementMetadata = replacementMetadata;
	}

	@Override
	protected boolean applyToBlock(int index, Block[] blocks, byte[] metadata) {
		int k;
		Block current = blocks[index];
		ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(current);
		if (rl == null || rl.getResourceDomain().equalsIgnoreCase("minecraft")) {
			//This might be a mod block. Check if an exception exists.
			for (k = 0; k < exceptions.size(); k++) {
				if (current == exceptions.get(k)) {
					//Exception found, not considered a mod block
					return false;
				}
			}
			//No matching exception found. Replace the block.
			blocks[index] = replacementBlock;
			metadata[index] = replacementMetadata;
			return true;
		}
		return false;
	}

	@Override
	protected boolean terminates() {
		return false;
	}
}
