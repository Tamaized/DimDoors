package dimdoors.registry;

import dimdoors.DimDoors;
import dimdoors.common.world.DimDoorBiomeBase;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(DimDoors.modid)
@Mod.EventBusSubscriber(modid = DimDoors.modid)
public class DimBiomes {

	@GameRegistry.ObjectHolder("limbo")
	public static final Biome limboBiome;
	@GameRegistry.ObjectHolder("pocket")
	public static final Biome pocketBiome;

	static {
		limboBiome = null;
		pocketBiome = null;
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Biome> event) {
		event.getRegistry().register(new DimDoorBiomeBase(new Biome.BiomeProperties("Limbo")).setRegistryName("limbo"));
		event.getRegistry().register(new DimDoorBiomeBase(new Biome.BiomeProperties("Pocket Dimension")).setRegistryName("pocket"));
	}

}
