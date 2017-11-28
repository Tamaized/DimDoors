package dimdoors.registry;

import com.google.common.collect.Lists;
import dimdoors.DimDoors;
import dimdoors.common.blocks.BlockUnraveledFabric;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

@GameRegistry.ObjectHolder(DimDoors.modid)
@Mod.EventBusSubscriber(modid = DimDoors.modid)
public class DimBlocks {

	@GameRegistry.ObjectHolder("quartz_door")
	public static final Block quartzDoor = Blocks.AIR;
	@GameRegistry.ObjectHolder("personal_dim_door")
	public static final Block personalDimDoor = Blocks.AIR;
	@GameRegistry.ObjectHolder("transient_door")
	public static final Block transientDoor = Blocks.AIR;
	@GameRegistry.ObjectHolder("warp_door")
	public static final Block warpDoor = Blocks.AIR;
	@GameRegistry.ObjectHolder("golden_door")
	public static final Block goldenDoor = Blocks.AIR;
	@GameRegistry.ObjectHolder("golden_dimensional_door")
	public static final Block goldenDimensionalDoor = Blocks.AIR;
	@GameRegistry.ObjectHolder("unstable_door")
	public static final Block unstableDoor = Blocks.AIR;
	@GameRegistry.ObjectHolder("limbo")
	public static final Block blockLimbo = Blocks.AIR;
	@GameRegistry.ObjectHolder("dimensional_door")
	public static final Block dimensionalDoor = Blocks.AIR;
	@GameRegistry.ObjectHolder("block_dim_wall")
	public static final Block blockDimWall = Blocks.AIR;
	@GameRegistry.ObjectHolder("trans_trapdoor")
	public static final Block transTrapdoor = Blocks.AIR;
	@GameRegistry.ObjectHolder("block_dim_wall_perm")
	public static final Block blockDimWallPerm = Blocks.AIR;
	@GameRegistry.ObjectHolder("block_rift")
	public static final Block blockRift = Blocks.AIR;
	private static final List<Item> itemblocks = Lists.newArrayList();

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> reg = event.getRegistry();
		reg.register(new Block(Material.ROCK, MapColor.QUARTZ).setRegistryName("quartz_door"));
//		reg.register(new Block(Material.IRON, MapColor.IRON).setRegistryName("personal_dim_door"));
//		reg.register(new Block(Material.IRON, MapColor.IRON).setRegistryName("transient_door"));
//		reg.register(new Block(Material.IRON, MapColor.IRON).setRegistryName("warp_door"));
//		reg.register(new Block(Material.IRON, MapColor.GOLD).setRegistryName("golden_door"));
//		reg.register(new Block(Material.IRON, MapColor.GOLD).setRegistryName("golden_dimensional_door"));
//		reg.register(new Block(Material.IRON, MapColor.DIAMOND).setRegistryName("unstable_door"));
		reg.register(new BlockUnraveledFabric().setRegistryName("unraveledfabric").setUnlocalizedName("unraveledfabric").setHardness(0.2F).setLightLevel(0.0F));
//		reg.register(new Block(Material.IRON, MapColor.IRON).setRegistryName("dimensional_door"));
//		reg.register(new Block(Material.IRON, MapColor.IRON).setRegistryName("block_dim_wall"));
//		reg.register(new Block(Material.IRON, MapColor.IRON).setRegistryName("trans_trapdoor"));
//		reg.register(new Block(Material.IRON, MapColor.IRON).setRegistryName("block_dim_wall_perm"));
//		reg.register(new Block(Material.IRON, MapColor.IRON).setRegistryName("block_rift"));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		registerItemBlock(reg, blockLimbo);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("ConstantConditions")
	public static void registerModels(ModelRegistryEvent event) {
		for (Item item : itemblocks)
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "normal"));
	}

	@SuppressWarnings("ConstantConditions")
	private static void registerItemBlock(IForgeRegistry<Item> reg, Block b) {
		ItemBlock item = new ItemBlock(b);
		item.setRegistryName(b.getRegistryName());
		reg.register(item);
		itemblocks.add(item);
	}

}
