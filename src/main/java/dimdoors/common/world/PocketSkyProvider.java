package dimdoors.common.world;

import dimdoors.DimDoors;
import net.minecraft.util.ResourceLocation;

public class PocketSkyProvider extends CustomSkyProvider {

	@Override
	public ResourceLocation getMoonRenderPath() {
		return new ResourceLocation(DimDoors.modid, "textures/other/limboMoon.png");
	}

	@Override
	public ResourceLocation getSunRenderPath() {
		return new ResourceLocation(DimDoors.modid, "textures/other/limboSun.png");
	}

}