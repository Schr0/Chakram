package schr0.chakram;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MessageHandlerPlayerAction implements IMessageHandler<MessagePlayerAction, IMessage>
{

	@Override
	public IMessage onMessage(MessagePlayerAction message, MessageContext ctx)
	{
		World world = FMLClientHandler.instance().getClient().world;
		EntityLivingBase ｌivingBase = message.getEntityLivingBase(world);

		if ((world != null) && (ｌivingBase != null))
		{
			Random random = world.rand;

			switch (message.getActionType())
			{
				case ChakramActionTypes.SWING_ARM :

					ｌivingBase.swingArm(ｌivingBase.getActiveHand());

					break;
			}
		}

		return (IMessage) null;
	}

}
