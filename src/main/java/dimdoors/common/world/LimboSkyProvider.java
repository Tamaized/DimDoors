package dimdoors.common.world;

import dimdoors.DimDoors;
import net.minecraft.util.ResourceLocation;

public class LimboSkyProvider extends CustomSkyProvider {

	@Override
	public ResourceLocation getMoonRenderPath() {
		return new ResourceLocation(DimDoors.modid, "textures/other/limbomoon.png");
	}

	@Override
	public ResourceLocation getSunRenderPath() {
		return new ResourceLocation(DimDoors.modid, "textures/other/limbosun.png");
	}

}