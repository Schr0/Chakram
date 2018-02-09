package schr0.chakram;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MessageHandlerParticleEntity implements IMessageHandler<MessageParticleEntity, IMessage>
{

	@Override
	public IMessage onMessage(MessageParticleEntity message, MessageContext ctx)
	{
		World world = FMLClientHandler.instance().getClient().world;
		Entity entity = message.getEntity(world);

		if ((world != null) && (entity != null))
		{
			Random random = world.rand;

			switch (message.getParticleType())
			{
				case ChakramParticles.ITEM_CHAGE :

					particleItemChage(world, entity, random);

					break;

				case ChakramParticles.ITEM_CHAGE_MAX :

					particleItemChageMax(world, entity, random);

					break;

				case ChakramParticles.ENTITY_UPDATE :

					particleEntityUpdate(world, entity, random);

					break;
			}
		}

		return (IMessage) null;
	}

	// TODO /* ======================================== MOD START =====================================*/

	private static void particleItemChage(World world, Entity entity, Random random)
	{
		for (int count = 0; count < 20; count++)
		{
			double pX = entity.posX + (double) (random.nextFloat() * entity.width * 2.0F) - (double) entity.width;
			double pY = entity.posY + (double) (random.nextFloat() * entity.height);
			double pZ = entity.posZ + (double) (random.nextFloat() * entity.width * 2.0F) - (double) entity.width;

			world.spawnParticle(EnumParticleTypes.CRIT, pX, pY, pZ, 0.0D, 0.0D, 0.0D, new int[0]);
		}
	}

	private static void particleItemChageMax(World world, Entity entity, Random random)
	{
		for (int count = 0; count < 20; count++)
		{
			double pX = entity.posX + (double) (random.nextFloat() * entity.width * 2.0F) - (double) entity.width;
			double pY = entity.posY + (double) (random.nextFloat() * entity.height);
			double pZ = entity.posZ + (double) (random.nextFloat() * entity.width * 2.0F) - (double) entity.width;

			world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, pX, pY, pZ, 0.0D, 0.0D, 0.0D, new int[0]);
		}
	}

	private static void particleEntityUpdate(World world, Entity entity, Random random)
	{
		for (int count = 0; count < 5; count++)
		{
			double pX = entity.posX + (double) (random.nextFloat() * entity.width * 2.0F) - (double) entity.width;
			double pY = entity.posY + (double) (random.nextFloat() * entity.height);
			double pZ = entity.posZ + (double) (random.nextFloat() * entity.width * 2.0F) - (double) entity.width;

			world.spawnParticle(EnumParticleTypes.CRIT, pX, pY, pZ, 0.0D, 0.0D, 0.0D, new int[0]);
		}
	}

}
