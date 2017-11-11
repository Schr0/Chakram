package schr0.chakram;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MessagePlayerAction implements IMessage
{

	private int entitiyID;
	private int actionType;

	public MessagePlayerAction()
	{
		// none
	}

	public MessagePlayerAction(EntityLivingBase entity, int actionType)
	{
		this.entitiyID = entity.getEntityId();
		this.actionType = actionType;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.entitiyID = buf.readInt();
		this.actionType = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.entitiyID);
		buf.writeInt(this.actionType);
	}

	// TODO /* ======================================== MOD START =====================================*/

	@Nullable
	public EntityLivingBase getEntityLivingBase(World world)
	{
		Entity entity = world.getEntityByID(this.entitiyID);

		if (entity instanceof EntityLivingBase)
		{
			return (EntityLivingBase) entity;
		}

		return (EntityLivingBase) null;
	}

	public int getActionType()
	{
		return this.actionType;
	}

}
