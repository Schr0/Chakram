package schr0.chakram;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ChakramPackets
{

	public static final String CHANNEL_NAME = Chakram.MOD_ID;
	public static final SimpleNetworkWrapper DISPATCHER = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);

	public static final int ID_PLAYER_ACTION = 0;
	public static final int ID_PARTICLE_ENTITY = 1;

	public void registerMessages()
	{
		// none
	}

	@SideOnly(Side.CLIENT)
	public void registerClientMessages()
	{
		DISPATCHER.registerMessage(MessageHandlerPlayerAction.class, MessagePlayerAction.class, ID_PLAYER_ACTION, Side.CLIENT);
		DISPATCHER.registerMessage(MessageHandlerParticleEntity.class, MessageParticleEntity.class, ID_PARTICLE_ENTITY, Side.CLIENT);
	}

}
