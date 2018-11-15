package schr0.chakram;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ChakramEntitys
{

	public static final String NAME_CHAKRAM = ChakramItems.NAME_CHAKRAM;
	public static final int ID_CHAKRAM = 0;
	public static final int TRACKING_RANGE = 250;
	public static final int UPDATE_FREQUENCY = 1;
	public static final boolean SENDS_VELOCITY_UPDATES = true;

	public void registerEntitys()
	{
		registerEntity(EntityChakram.class, NAME_CHAKRAM, ID_CHAKRAM, Chakram.instance, TRACKING_RANGE, UPDATE_FREQUENCY, SENDS_VELOCITY_UPDATES);
	}

	@SideOnly(Side.CLIENT)
	public void registerRenders()
	{
		Minecraft mc = Minecraft.getMinecraft();

		RenderingRegistry.registerEntityRenderingHandler(EntityChakram.class, new IRenderFactory()
		{
			@Override
			public Render createRenderFor(RenderManager renderManager)
			{
				return new RenderChakram(renderManager, mc.getRenderItem());
			}
		});
	}

	// TODO /* ======================================== MOD START =====================================*/

	private static void registerEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates)
	{
		EntityRegistry.registerModEntity(new ResourceLocation(Chakram.MOD_ID, entityName), entityClass, entityName, id, mod, trackingRange, updateFrequency, sendsVelocityUpdates);
	}

}
