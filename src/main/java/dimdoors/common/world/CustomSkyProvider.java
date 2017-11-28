package dimdoors.common.world;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

public class CustomSkyProvider extends IRenderHandler {

	private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
	int starGLCallList;
	int glSkyList;
	int glSkyList2;

	public ResourceLocation getMoonRenderPath() {
		return null;
	}

	public ResourceLocation getSunRenderPath() {
		return null;
	}

	@Override
	public void render(float par1, WorldClient world, Minecraft mc) {
		starGLCallList = GLAllocation.generateDisplayLists(3);
		glSkyList = this.starGLCallList + 1;
		glSkyList2 = this.starGLCallList + 2;
		GlStateManager.disableFog();
		GlStateManager.disableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableDepth();
		mc.renderEngine.bindTexture((locationEndSkyPng));

		if (mc.world.provider.isSurfaceWorld()) {
			GlStateManager.disableTexture2D();
			Vec3d vec3 = mc.getRenderViewEntity() == null ? new Vec3d(0, 0, 0) : world.getSkyColor(mc.getRenderViewEntity(), par1);
			float f1 = (float) vec3.x;
			float f2 = (float) vec3.y;
			float f3 = (float) vec3.z;
			float f4;

			if (mc.gameSettings.anaglyph) {
				float f5 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
				float f6 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
				f4 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
				f1 = f5;
				f2 = f6;
				f3 = f4;
			}

			GlStateManager.color(f1, f2, f3);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buf = tessellator.getBuffer();
			GlStateManager.disableDepth();
			GlStateManager.enableFog();
			GlStateManager.color(f1, f2, f3);
			GlStateManager.callList(this.glSkyList);
			GlStateManager.disableFog();
			GlStateManager.disableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderHelper.disableStandardItemLighting();
			float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(par1), par1);
			float f7;
			float f8;
			float f9;
			float f10;

			if (afloat != null) {
				GlStateManager.disableTexture2D();
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				GlStateManager.pushMatrix();
				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(MathHelper.sin(world.getCelestialAngleRadians(par1)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
				f4 = afloat[0];
				f7 = afloat[1];
				f8 = afloat[2];
				float f11;

				if (mc.gameSettings.anaglyph) {
					f9 = (f4 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
					f10 = (f4 * 30.0F + f7 * 70.0F) / 100.0F;
					f11 = (f4 * 30.0F + f8 * 70.0F) / 100.0F;
					f4 = f9;
					f7 = f10;
					f8 = f11;
				}

				buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				buf.pos(0.0D, 100.0D, 0.0D).color(f4, f7, f8, afloat[3]);
				byte b0 = 16;

				for (int j = 0; j <= b0; ++j) {
					f11 = j * (float) Math.PI * 2.0F / b0;
					float f12 = MathHelper.sin(f11);
					float f13 = MathHelper.cos(f11);
					buf.pos(f12 * 120.0F, f13 * 120.0F, -f13 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F);
				}

				tessellator.draw();
				GlStateManager.popMatrix();
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}

			GlStateManager.enableTexture2D();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
			GlStateManager.pushMatrix();
			f4 = 1.0F - world.getRainStrength(par1);
			f7 = 0.0F;
			f8 = 0.0F;
			f9 = 0.0F;
			GlStateManager.color(1.0F, 1.0F, 1.0F, f4);
			GlStateManager.translate(f7, f8, f9);
			GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(world.getCelestialAngle(par1) * 360.0F, 1.0F, 0.0F, 0.0F);
			f10 = 30.0F;
			mc.renderEngine.bindTexture(this.getSunRenderPath());
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos((-f10), 100.0D, (-f10)).tex(0.0D, 0.0D);
			buf.pos(f10, 100.0D, (-f10)).tex(1.0D, 0.0D);
			buf.pos(f10, 100.0D, f10).tex(1.0D, 1.0D);
			buf.pos((-f10), 100.0D, f10).tex(0.0D, 1.0D);
			tessellator.draw();
			f10 = 20.0F;
			mc.renderEngine.bindTexture(this.getMoonRenderPath());
			int k = world.getMoonPhase();
			int l = k % 4;
			int i1 = k / 4 % 2;
			float f14 = l + 0;
			float f15 = i1 + 0;
			float f16 = l + 1;
			float f17 = i1 + 1;
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos((-f10), -100.0D, f10).tex(f16, f17);
			buf.pos(f10, -100.0D, f10).tex(f14, f17);
			buf.pos(f10, -100.0D, (-f10)).tex(f14, f15);
			buf.pos((-f10), -100.0D, (-f10)).tex(f16, f15);
			tessellator.draw();
			GlStateManager.disableTexture2D();
			float f18 = world.getStarBrightness(par1) * f4;

			if (f18 > 0.0F) {
				GlStateManager.color(f18, f18, f18, f18);
				GlStateManager.callList(this.starGLCallList);
			}

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.enableFog();
			GlStateManager.popMatrix();
			GlStateManager.disableTexture2D();
			GlStateManager.color(0.0F, 0.0F, 0.0F);
			double d0 = mc.player.posY - world.getHorizon();

			if (d0 < 0.0D) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0.0F, 12.0F, 0.0F);
				GlStateManager.callList(this.glSkyList2);
				GlStateManager.popMatrix();
				f8 = 1.0F;
				f9 = -((float) (d0 + 65.0D));
				f10 = -f8;
				buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				buf.pos((-f8), f9, f8).color(0, 0, 0, 255);
				buf.pos(f8, f9, f8).color(0, 0, 0, 255);
				buf.pos(f8, f10, f8).color(0, 0, 0, 255);
				buf.pos((-f8), f10, f8).color(0, 0, 0, 255);
				buf.pos((-f8), f10, (-f8)).color(0, 0, 0, 255);
				buf.pos(f8, f10, (-f8)).color(0, 0, 0, 255);
				buf.pos(f8, f9, (-f8)).color(0, 0, 0, 255);
				buf.pos((-f8), f9, (-f8)).color(0, 0, 0, 255);
				buf.pos(f8, f10, (-f8)).color(0, 0, 0, 255);
				buf.pos(f8, f10, f8).color(0, 0, 0, 255);
				buf.pos(f8, f9, f8).color(0, 0, 0, 255);
				buf.pos(f8, f9, (-f8)).color(0, 0, 0, 255);
				buf.pos((-f8), f9, (-f8)).color(0, 0, 0, 255);
				buf.pos((-f8), f9, f8).color(0, 0, 0, 255);
				buf.pos((-f8), f10, f8).color(0, 0, 0, 255);
				buf.pos((-f8), f10, (-f8)).color(0, 0, 0, 255);
				buf.pos((-f8), f10, (-f8)).color(0, 0, 0, 255);
				buf.pos((-f8), f10, f8).color(0, 0, 0, 255);
				buf.pos(f8, f10, f8).color(0, 0, 0, 255);
				buf.pos(f8, f10, (-f8)).color(0, 0, 0, 255);
				tessellator.draw();
			}

			if (world.provider.isSkyColored()) {
				GlStateManager.color(f1 * 0.2F + 0.04F, f2 * 0.2F + 0.04F, f3 * 0.6F + 0.1F);
			} else {
				GlStateManager.color(f1, f2, f3);
			}

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, -((float) (d0 - 16.0D)), 0.0F);
			GlStateManager.callList(this.glSkyList2);
			GlStateManager.popMatrix();
			GlStateManager.enableTexture2D();
			GlStateManager.enableDepth();
		}

	}

}