package schr0.chakram;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

public class ChakramDebug
{

	public static void infoBugMessage(EntityPlayer player, Class bugClass)
	{
		player.sendMessage(new TextComponentString(bugClass + " でバグ発生中！ 楽しく遊んでるのに、ごめんね！ 報告してくれると助かります！"));
	}

}
