package schr0.chakram;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderChakram extends Render<EntityChakram>
{

	private static final float ADJUST_POS_Y = 0.25F;
	private RenderItem itemRenderer;

	public RenderChakram(RenderManager renderManagerIn, RenderItem itemRendererIn)
	{
		super(renderManagerIn);

		this.itemRenderer = itemRendererIn;
	}

	public void doRender(EntityChakram entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.enableRescaleNormal();

		float rotationX = 90.0F;
		float rotationZ = getRotationaSpeed(entity.ticksExisted + partialTicks);
		GlStateManager.rotate(rotationX, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(rotationZ, 0.0F, 0.0F, 1.0F);

		float adjustPosX = 0.0F;
		float adjustPosY = -(entity.width / 2.0F) + ADJUST_POS_Y;
		float adjustPosZ = -(entity.height / 2.0F);
		GlStateManager.translate(adjustPosX, adjustPosY, adjustPosZ);

		float size = 2.0F;
		GlStateManager.scale(size, size, size);

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		if (this.renderOutlines)
		{
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(this.getTeamColor(entity));
		}

		this.itemRenderer.renderItem(getStackToRender(entity), ItemCameraTransforms.TransformType.GROUND);

		if (this.renderOutlines)
		{
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityChakram entity)
	{
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

	// TODO /* ======================================== MOD START =====================================*/

	private static float getRotationaSpeed(float ticks)
	{
		return (ticks * 100);
	}

	private static ItemStack getStackToRender(EntityChakram entity)
	{
		return entity.getEntityItem();
	}

}
