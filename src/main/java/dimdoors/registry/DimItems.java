package dimdoors.registry;

import com.google.common.collect.Lists;
import dimdoors.DimDoors;
import dimdoors.common.items.ItemDimensionalDoor;
import dimdoors.common.items.ItemDoorBase;
import dimdoors.common.items.ItemRiftBlade;
import dimdoors.common.items.ItemRiftRemover;
import dimdoors.common.items.ItemRiftSignature;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
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
public class DimItems {

	private static final List<Item> items = Lists.newArrayList();

	@GameRegistry.ObjectHolder("golden_dimensional_door")
	public static Item itemGoldenDimensionalDoor = Items.AIR;
	@GameRegistry.ObjectHolder("golden_door")
	public static Item itemGoldenDoor = Items.AIR;
	@GameRegistry.ObjectHolder("world_thread")
	public static Item itemWorldThread = Items.AIR;
	@GameRegistry.ObjectHolder("rift_blade")
	public static Item itemRiftBlade = Items.AIR;
	@GameRegistry.ObjectHolder("dimensional_door")
	public static Item itemDimensionalDoor = Items.AIR;
	@GameRegistry.ObjectHolder("warp_door")
	public static Item itemWarpDoor = Items.AIR;
	@GameRegistry.ObjectHolder("rift_remover")
	public static Item itemRiftRemover = Items.AIR;
	@GameRegistry.ObjectHolder("rift_signature")
	public static Item itemRiftSignature = Items.AIR;
	@GameRegistry.ObjectHolder("stable_fabric")
	public static Item itemStableFabric = Items.AIR;
	@GameRegistry.ObjectHolder("unstable_door")
	public static Item itemUnstableDoor = Items.AIR;
	@GameRegistry.ObjectHolder("dd_key")
	public static Item itemDDKey = Items.AIR;
	@GameRegistry.ObjectHolder("quartz_door")
	public static Item itemQuartzDoor = Items.AIR;
	@GameRegistry.ObjectHolder("personal_door")
	public static Item itemPersonalDoor = Items.AIR;
	@GameRegistry.ObjectHolder("stablized_rift_signature")
	public static Item itemStabilizedRiftSignature = Items.AIR;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		ItemDoor door;
		register(reg, door = (ItemDoor) new ItemDoorBase(DimBlocks.goldenDoor).setRegistryName("golden_door"));
		register(reg, new ItemDimensionalDoor(DimBlocks.goldenDimensionalDoor, door).setRegistryName("golden_dimensional_door"));
		register(reg, new Item().setRegistryName("world_thread"));
		register(reg, new ItemRiftBlade().setRegistryName("rift_blade"));
		register(reg, new ItemDimensionalDoor(DimBlocks.dimensionalDoor, (ItemDoor) Items.IRON_DOOR).setRegistryName("dimensional_door"));
		register(reg, new ItemDimensionalDoor(DimBlocks.warpDoor, (ItemDoor) Items.IRON_DOOR).setRegistryName("warp_door"));
		register(reg, new ItemRiftRemover().setRegistryName("rift_remover"));
		register(reg, new ItemRiftSignature().setRegistryName("rift_signature"));
		register(reg, new Item().setRegistryName("stable_fabric"));
		register(reg, new Item().setRegistryName("unstable_door"));
		register(reg, new Item().setRegistryName("dd_key"));
		register(reg, new Item().setRegistryName("quartz_door"));
		register(reg, new Item().setRegistryName("personal_door"));
		register(reg, new Item().setRegistryName("stablized_rift_signature"));
	}

	public static void register(IForgeRegistry<Item> reg, Item item) {
		items.add(item);
		reg.register(item);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerModels(ModelRegistryEvent event) {
		for (Item item : items)
			if (item.getRegistryName() != null)
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

}
