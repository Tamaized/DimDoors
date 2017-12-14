package dimdoors.client.entity;

import dimdoors.common.entity.EntityRift;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RenderRift extends Render<EntityRift> {

	public RenderRift(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(@Nonnull EntityRift entity, double x, double y, double z, float entityYaw, float partialTicks) { // :waitwhat: TODO this all looks useless
		/*// prepare fb for drawing
		GL11.glPushMatrix();

		// make the rift render on both sides, disable texture mapping and
		// lighting
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL_TEXTURE_2D);
		GL11.glDisable(GL_LIGHTING);
		GL11.glEnable(GL_BLEND);
		*//**
		 * GL11.glLogicOp(GL11.GL_INVERT);
		 * GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
		 *//*
		TileEntityRift rift = (TileEntityRift) te;
		// draws the verticies corresponding to the passed it

		GL11.glDisable(GL_BLEND);
		// reenable all the stuff we disabled
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL_TEXTURE_2D);

		GL11.glPopMatrix();*/
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(@Nonnull EntityRift entity) {
		return null;
	}
}