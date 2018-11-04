package schr0.chakram;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import schr0.chakram.api.ChakramAPI;

@Mod(modid = Chakram.MOD_ID, name = Chakram.MOD_NAME, version = Chakram.MOD_VERSION)
public class Chakram
{

	@Mod.Instance(Chakram.MOD_ID)
	public static Chakram instance;

	/**
	 * ModのID.
	 */
	public static final String MOD_ID = ChakramAPI.MOD_ID;

	/**
	 * Modの名前.
	 */
	public static final String MOD_NAME = ChakramAPI.MOD_NAME;

	/**
	 * Modのバージョン.
	 */
	public static final String MOD_VERSION = "2.0.0";

	/**
	 * ResourceLocationのDomain.
	 */
	public static final String MOD_RESOURCE_DOMAIN = MOD_ID + ":";

	/**
	 * 初期・設定イベント.
	 */
	@Mod.EventHandler
	public void construction(FMLConstructionEvent event)
	{
		MinecraftForge.EVENT_BUS.register(this);

		if (event.getSide().isClient())
		{
			(new ChakramEntitys()).registerRenders();
		}
	}

	/**
	 * 事前・設定イベント.
	 */
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		// none

		if (event.getSide().isClient())
		{
			// none
		}
	}

	/**
	 * 事中・設定イベント.
	 */
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		// none

		if (event.getSide().isClient())
		{
			(new ChakramPackets()).registerClientMessages();
		}
	}

	/**
	 * 事後・設定イベント.
	 */
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		// none

		if (event.getSide().isClient())
		{
			// none
		}
	}

	// TODO /* ======================================== MOD START =====================================*/

	/**
	 * Itemの登録.
	 */
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();

		(new ChakramItems()).registerItems(registry);
	}

	/**
	 * Item・Blockモデルの登録.
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event)
	{
		(new ChakramItems()).registerModels();
	}

	/**
	 * Entityの登録.
	 */
	@SubscribeEvent
	public void registerEntitys(RegistryEvent.Register<EntityEntry> event)
	{
		(new ChakramEntitys()).registerEntitys();
	}

	/**
	 * Recipeの登録.
	 */
	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event)
	{
		IForgeRegistry<IRecipe> registry = event.getRegistry();

		(new ChakramRecipes()).registerRecipes(registry);
	}

	/*
		@SubscribeEvent
		public void onLivingDamageEvent(LivingDamageEvent event)
		{
			EntityLivingBase entity = event.getEntityLiving();
			DamageSource damageSource = event.getSource();
	
			if (damageSource.getTrueSource() instanceof EntityPlayer)
			{
				FMLLog.info("Chakram Damage : %f", event.getAmount());
			}
		}
	// */

}
