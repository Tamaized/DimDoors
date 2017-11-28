package dimdoors.registry;

import dimdoors.DimDoors;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@GameRegistry.ObjectHolder(DimDoors.modid)
@Mod.EventBusSubscriber(modid = DimDoors.modid)
public class DimItems {

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
		event.getRegistry().register(new Item().setRegistryName("world_thread"));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("ConstantConditions")
	public static void registerModels(ModelRegistryEvent event) {

	}

}
