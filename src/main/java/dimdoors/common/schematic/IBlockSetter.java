package dimdoors.common.schematic;


import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public interface IBlockSetter {

	void setBlock(World world, int x, int y, int z, IBlockState state);

}
