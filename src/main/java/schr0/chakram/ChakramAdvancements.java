package schr0.chakram;

import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public class ChakramAdvancements
{

	public static final String RES_KEY = Chakram.MOD_ID;

	public static boolean complete(EntityPlayer player, ResourceLocation key)
	{
		if (player instanceof EntityPlayerMP)
		{
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			Advancement advancement = playerMP.getServer().getAdvancementManager().getAdvancement(key);

			if (advancement != null)
			{
				playerMP.getAdvancements().grantCriterion(advancement, "mod_trigger");

				return true;
			}
		}

		return false;
	}

	public static boolean completeThrowing(EntityPlayer player)
	{
		ResourceLocation key = new ResourceLocation(RES_KEY, "adventure/throwing");

		return complete(player, key);
	}

	public static boolean completeCollectItems(EntityPlayer player)
	{
		ResourceLocation key = new ResourceLocation(RES_KEY, "adventure/collect_items");

		return complete(player, key);
	}

	public static boolean completeThrowAttack(EntityPlayer player)
	{
		ResourceLocation key = new ResourceLocation(RES_KEY, "adventure/throw_attack");

		return complete(player, key);
	}

	public static boolean completeFullPower(EntityPlayer player)
	{
		ResourceLocation key = new ResourceLocation(RES_KEY, "adventure/full_power");

		return complete(player, key);
	}

	public static boolean completeWelcomeHome(EntityPlayer player)
	{
		ResourceLocation key = new ResourceLocation(RES_KEY, "adventure/welcome_home");

		return complete(player, key);
	}

}
