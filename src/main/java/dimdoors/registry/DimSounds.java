package dimdoors.registry;

import dimdoors.DimDoors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = DimDoors.modid)
public class DimSounds {

	public static SoundEvent riftclose = null;
	public static SoundEvent riftdoor = null;
	public static SoundEvent keyunlock = null;
	public static SoundEvent keylock = null;
	public static SoundEvent doorlocked = null;
	public static SoundEvent doorlockremoved = null;
	public static SoundEvent monk = null;
	public static SoundEvent tearing = null;
	public static SoundEvent riftend = null;
	public static SoundEvent riftstart = null;

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		riftclose = registerSound(event, "riftclose");
		riftdoor = registerSound(event, "riftdoor");
		keyunlock = registerSound(event, "keyunlock");
		keylock = registerSound(event, "keylock");
		doorlocked = registerSound(event, "doorlocked");
		doorlockremoved = registerSound(event, "doorlockremoved");
		monk = registerSound(event, "monk");
		tearing = registerSound(event, "tearing");
		riftend = registerSound(event, "riftend");
		riftstart = registerSound(event, "riftstart");
	}

	private static SoundEvent registerSound(RegistryEvent.Register<SoundEvent> event, String soundName) {
		SoundEvent sound = new SoundEvent(new ResourceLocation(DimDoors.modid, soundName)).setRegistryName(soundName);
		event.getRegistry().register(sound);
		return sound;
	}
}