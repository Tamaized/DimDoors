package dimdoors.client.particle;


import dimdoors.common.core.PocketManager;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GoggleRiftFX extends ParticleFirework.Spark {
	private final ParticleManager field_92047_az;
	private int field_92049_a = 160;
	private boolean field_92054_ax;
	private boolean field_92048_ay;
	private float field_92050_aA;
	private float field_92051_aB;
	private float field_92052_aC;
	private boolean field_92053_aD;

	public GoggleRiftFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, ParticleManager par14EffectRenderer) {

		super(par1World, par2, par4, par6, par12, par12, par12, par14EffectRenderer);
		this.motionX = par8;
		this.motionY = par10;
		this.motionZ = par12;
		this.field_92047_az = par14EffectRenderer;
		this.particleScale *= 0.75F;
		this.particleMaxAge = 40 + this.rand.nextInt(26);
	}

	public void func_92045_e(boolean par1) {
		this.field_92054_ax = par1;
	}

	public void func_92043_f(boolean par1) {
		this.field_92048_ay = par1;
	}

	public void func_92044_a(int par1) {
		float var2 = ((par1 & 16711680) >> 16) / 255.0F;
		float var3 = ((par1 & 65280) >> 8) / 255.0F;
		float var4 = ((par1 & 255) >> 0) / 255.0F;
		float var5 = 1.0F;
		this.setRBGColorF(var2 * var5, var3 * var5, var4 * var5);
	}

	public void func_92046_g(int par1) {
		this.field_92050_aA = ((par1 & 16711680) >> 16) / 255.0F;
		this.field_92051_aB = ((par1 & 65280) >> 8) / 255.0F;
		this.field_92052_aC = ((par1 & 255) >> 0) / 255.0F;
		this.field_92053_aD = true;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!this.field_92048_ay || this.particleAge < this.particleMaxAge / 3 || (this.particleAge + this.particleMaxAge) / 3 % 2 == 0) {


			this.doRenderParticle(buffer, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
		}
	}

	public void doRenderParticle(BufferBuilder buffer, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		float f = (float)this.particleTextureIndexX / 16.0F;
		float f1 = f + 0.0624375F;
		float f2 = (float)this.particleTextureIndexY / 16.0F;
		float f3 = f2 + 0.0624375F;
		float f4 = 0.1F * this.particleScale;

		if (this.particleTexture != null)
		{
			f = this.particleTexture.getMinU();
			f1 = this.particleTexture.getMaxU();
			f2 = this.particleTexture.getMinV();
			f3 = this.particleTexture.getMaxV();
		}

		float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
		int i = this.getBrightnessForRender(partialTicks);
		int j = i >> 16 & 65535;
		int k = i & 65535;
		Vec3d[] avec3d = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};

		if (this.particleAngle != 0.0F)
		{
			float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
			float f9 = MathHelper.cos(f8 * 0.5F);
			float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.x;
			float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.y;
			float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.z;
			Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

			for (int l = 0; l < 4; ++l)
			{
				avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
			}
		}

		float f14 = 0F;

		if (PocketManager.createDimensionData(world).isPocketDimension()) {
			f14 = 0.7F;
		}
		particleAlpha = 0.7F;

		buffer.pos((double)f5 + avec3d[0].x, (double)f6 + avec3d[0].y, (double)f7 + avec3d[0].z).tex((double)f1, (double)f3).color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[1].x, (double)f6 + avec3d[1].y, (double)f7 + avec3d[1].z).tex((double)f1, (double)f2).color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[2].x, (double)f6 + avec3d[2].y, (double)f7 + avec3d[2].z).tex((double)f, (double)f2).color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha).lightmap(j, k).endVertex();
		buffer.pos((double)f5 + avec3d[3].x, (double)f6 + avec3d[3].y, (double)f7 + avec3d[3].z).tex((double)f, (double)f3).color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha).lightmap(j, k).endVertex();
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge) {
			setExpired();
		}

		if (this.particleAge > this.particleMaxAge / 2) {
			this.setAlphaF(1.0F - ((float) this.particleAge - (float) (this.particleMaxAge / 2)) / this.particleMaxAge);

			if (this.field_92053_aD) {
				this.particleRed += (this.field_92050_aA - this.particleRed) * 0.2F;
				this.particleGreen += (this.field_92051_aB - this.particleGreen) * 0.2F;
				this.particleBlue += (this.field_92052_aC - this.particleBlue) * 0.2F;
			}
		}

		this.setParticleTextureIndex(this.field_92049_a + (7 - this.particleAge * 8 / this.particleMaxAge));
		// this.motionY -= 0.004D;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9100000262260437D;
		this.motionY *= 0.9100000262260437D;
		this.motionZ *= 0.9100000262260437D;

		if (this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}

		if (this.field_92054_ax && this.particleAge < this.particleMaxAge / 2 && (this.particleAge + this.particleMaxAge) % 2 == 0) {
			GoggleRiftFX var1 = new GoggleRiftFX(this.world, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D, this.field_92047_az);
			var1.setRBGColorF(this.particleRed, this.particleGreen, this.particleBlue);
			var1.particleAge = var1.particleMaxAge / 2;

			if (this.field_92053_aD) {
				var1.field_92053_aD = true;
				var1.field_92050_aA = this.field_92050_aA;
				var1.field_92051_aB = this.field_92051_aB;
				var1.field_92052_aC = this.field_92052_aC;
			}

			var1.field_92048_ay = this.field_92048_ay;
			this.field_92047_az.addEffect(var1);
		}
	}

	@Override
	public int getBrightnessForRender(float par1) {
		return 15728880;
	}

}
