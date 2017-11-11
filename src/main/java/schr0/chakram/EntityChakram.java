package schr0.chakram;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityChakram extends Entity
{

	private static final String BOOST_MODIFIER = "Boost modifier";

	private static final int TICKS_INTERVAL = 25;

	private static final float SPEED_MIN = 0.85F;
	private static final float SPEED_MAX = 1.85F;

	private static final float DISTANCE_MIN = 6.4F;
	private static final float DISTANCE_MAX = 64.0F;

	private static final String TAG = Chakram.MOD_ID + ".";
	private static final String TAG_MOTION_XYZ = TAG + "motion_xyz";
	private static final String TAG_ACCELERATION_XYZ = TAG + "acceleration_xyz";
	private static final String TAG_OWNER_UUID = TAG + "owner_uuid";
	private static final String TAG_ITEM = TAG + "item";
	private static final String TAG_RETURN_OWNER = TAG + "return_owner";
	private static final String TAG_CHAGE_AMMOUNT = TAG + "chage_ammount";
	private static final String TAG_AGE = TAG + "age";

	private double accelerationX;
	private double accelerationY;
	private double accelerationZ;
	private static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.<Optional<UUID>> createKey(EntityTameable.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<ItemStack> ITEM = EntityDataManager.<ItemStack> createKey(EntityChakram.class, DataSerializers.ITEM_STACK);
	private boolean isReturnOwner;
	private int chageAmmount;
	private int age;

	private int ticksInAir;

	public EntityChakram(World world)
	{
		super(world);
		this.setSize(0.95F, 0.25F);
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.accelerationX = this.accelerationY = this.accelerationZ = 0.0D;
		this.ticksInAir = 0;
	}

	public EntityChakram(World world, EntityPlayer player, ItemStack stack, int chageAmmount)
	{
		super(world);
		this.setSize(0.95F, 0.25F);
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.accelerationX = this.accelerationY = this.accelerationZ = 0.0D;
		this.ticksInAir = 0;

		this.setOwnerUUID(player.getUniqueID());
		this.setEntityItem(stack);
		this.setChageAmount(chageAmmount);
		this.setAge(0);
	}

	@Override
	protected void entityInit()
	{
		this.getDataManager().register(OWNER_UUID, Optional.absent());
		this.getDataManager().register(ITEM, ItemStack.EMPTY);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setTag(TAG_MOTION_XYZ, this.newDoubleNBTList(new double[]
		{
				this.motionX, this.motionY, this.motionZ
		}));

		compound.setTag(TAG_ACCELERATION_XYZ, this.newDoubleNBTList(new double[]
		{
				this.accelerationX, this.accelerationY, this.accelerationZ
		}));

		if (this.getOwnerUUID() == null)
		{
			compound.setString(TAG_OWNER_UUID, "");
		}
		else
		{
			compound.setString(TAG_OWNER_UUID, this.getOwnerUUID().toString());
		}

		compound.setTag(TAG_ITEM, this.getEntityItem().writeToNBT(new NBTTagCompound()));
		compound.setByte(TAG_RETURN_OWNER, this.isReturnOwner() ? (byte) 1 : (byte) 0);
		compound.setByte(TAG_CHAGE_AMMOUNT, (byte) this.getChageAmount());
		compound.setByte(TAG_AGE, (byte) this.getAge());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		if (compound.hasKey(TAG_MOTION_XYZ))
		{
			NBTTagList nbttaglist = compound.getTagList(TAG_MOTION_XYZ, 6);

			if (nbttaglist.tagCount() == 3)
			{
				this.motionX = nbttaglist.getDoubleAt(0);
				this.motionY = nbttaglist.getDoubleAt(1);
				this.motionZ = nbttaglist.getDoubleAt(2);
			}
		}
		else
		{
			this.setDead();
		}

		if (compound.hasKey(TAG_ACCELERATION_XYZ))
		{
			NBTTagList nbttaglist = compound.getTagList(TAG_ACCELERATION_XYZ, 6);

			if (nbttaglist.tagCount() == 3)
			{
				this.accelerationX = nbttaglist.getDoubleAt(0);
				this.accelerationY = nbttaglist.getDoubleAt(1);
				this.accelerationZ = nbttaglist.getDoubleAt(2);
			}
		}

		String ownerUUID;

		if (compound.hasKey(TAG_OWNER_UUID))
		{
			ownerUUID = compound.getString(TAG_OWNER_UUID);
		}
		else
		{
			ownerUUID = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), compound.getString("Owner"));
		}

		if (!ownerUUID.isEmpty())
		{
			try
			{
				this.setOwnerUUID(UUID.fromString(ownerUUID));
			}
			catch (Throwable var4)
			{
				this.setDead();
			}
		}

		this.setEntityItem(new ItemStack(compound.getCompoundTag(TAG_ITEM)));
		this.setReturnOwner(compound.getByte(TAG_RETURN_OWNER) == 1);
		this.setChageAmount(compound.getByte(TAG_CHAGE_AMMOUNT));
		this.setAge(compound.getByte(TAG_AGE));
	}

	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance)
	{
		double d0 = (this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D);

		if (Double.isNaN(d0))
		{
			d0 = 4.0D;
		}

		d0 = (d0 * 64.0D);

		return (distance < (d0 * d0));
	}

	@Override
	public String getName()
	{
		return this.getEntityItem().getDisplayName();
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer player)
	{
		if (this.isReturnOwner() && this.isOwner(player) && !this.world.isRemote)
		{
			this.setDead();
		}

		super.onCollideWithPlayer(player);
	}

	@Override
	public void setDead()
	{
		ItemStack stack = this.getEntityItem();
		boolean isBreakItemDmage = (stack.getMaxDamage() <= stack.getItemDamage()) || stack.isEmpty();

		if (this.getOwner() != null)
		{
			EntityPlayer player = this.getOwner();

			if (isBreakItemDmage)
			{
				player.renderBrokenItemStack(stack);
			}
			else
			{
				if (!this.world.isRemote)
				{
					if (player.getHeldItemMainhand().isEmpty())
					{
						player.setHeldItem(EnumHand.MAIN_HAND, stack);
					}
					else
					{
						if (!player.inventory.addItemStackToInventory(stack))
						{
							if (player.getHeldItemOffhand().isEmpty())
							{
								player.setHeldItem(EnumHand.OFF_HAND, stack);
							}
							else
							{
								player.dropItem(stack, false);
							}
						}
					}
				}
			}
		}
		else
		{
			if (!isBreakItemDmage && !this.world.isRemote)
			{
				this.entityDropItem(stack, 0.5F);
			}
		}

		super.setDead();
	}

	@Override
	public void onUpdate()
	{
		if (isOwnerNotExists(this.getOwner(), this))
		{
			boolean isRestartInterval = (TICKS_INTERVAL <= this.ticksExisted);

			if (isRestartInterval && !this.world.isRemote)
			{
				this.setDead();
			}

			return;
		}
		else
		{
			if (!this.world.isRemote)
			{
				int age = this.getAge();

				++age;

				if (this.getMaxAge() < age)
				{
					this.setDead();
				}
				else
				{
					this.setAge(age);
				}
			}
		}

		if (this.isInWater())
		{
			this.extinguish();
		}

		super.onUpdate();

		if (this.world.isRemote)
		{
			return;
		}

		++this.ticksInAir;

		RayTraceResult rayTraceResult = ProjectileHelper.forwardsRaycast(this, true, (TICKS_INTERVAL <= this.ticksInAir), this.getOwner());
		BlockPos blockPos = new BlockPos(this);

		if (rayTraceResult != null)
		{
			if (rayTraceResult.entityHit != null)
			{
				this.onHitEntity(rayTraceResult.entityHit);
			}
			else
			{
				blockPos = rayTraceResult.getBlockPos();
			}
		}

		if (!this.world.isAirBlock(blockPos))
		{
			IBlockState blockState = this.world.getBlockState(blockPos);

			if (EntityWither.canDestroyBlock(blockState.getBlock()))
			{
				if (blockState.getBlockHardness(this.world, blockPos) == 0.0F)
				{
					this.world.destroyBlock(blockPos, true);
				}
			}
		}

		if (this.isReturnOwner())
		{
			double vecX = (this.getOwner().posX - this.posX);
			double vecY = ((this.getOwner().posY + (double) (this.getOwner().height / 2.0F)) - this.posY);
			double vecZ = (this.getOwner().posZ - this.posZ);
			vecX += this.rand.nextGaussian() * 0.4D;
			vecY += this.rand.nextGaussian() * 0.4D;
			vecZ += this.rand.nextGaussian() * 0.4D;
			double distanceSqrt = (double) MathHelper.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
			this.accelerationX = (vecX / distanceSqrt * 0.1D);
			this.accelerationY = (vecY / distanceSqrt * 0.1D);
			this.accelerationZ = (vecZ / distanceSqrt * 0.1D);
		}
		else
		{
			boolean isMaxThrowDistance = (this.getThrowDistance() < this.getOwner().getDistanceSqToEntity(this));
			boolean isReflectiveBlock = isReflectiveBlock(this.world, blockPos);

			if (isMaxThrowDistance || isReflectiveBlock)
			{
				this.setReturnOwner(true);

				return;
			}
		}

		this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);

		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		ProjectileHelper.rotateTowardsMovement(this, 0.05F);

		this.motionX += this.accelerationX;
		this.motionY += this.accelerationY;
		this.motionZ += this.accelerationZ;

		float speed = this.getThrowSpeed();
		this.motionX *= (double) speed;
		this.motionY *= (double) speed;
		this.motionZ *= (double) speed;
		this.setPosition(this.posX, this.posY, this.posZ);

		ChakramPackets.DISPATCHER.sendToAll(new MessageParticleEntity(this, ChakramParticles.ENTITY_UPDATE));

		this.world.playSound(null, blockPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.VOICE, 0.5F, 2.0F + (this.rand.nextFloat() * 0.5F));
	}

	// TODO /* ======================================== MOD START =====================================*/

	public void setHeadingFromOwner(EntityPlayer player)
	{
		Vec3d ownerLookVec3 = player.getLookVec();
		double lookDistance = 100.0D;
		double vecX = (ownerLookVec3.x * lookDistance);
		double vecY = (ownerLookVec3.y * lookDistance);
		double vecZ = (ownerLookVec3.z * lookDistance);
		vecX += (this.rand.nextGaussian() * 0.4D);
		vecY += (this.rand.nextGaussian() * 0.4D);
		vecZ += (this.rand.nextGaussian() * 0.4D);
		double distanceSqrt = (double) MathHelper.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		this.accelerationX = (vecX / distanceSqrt * 0.1D);
		this.accelerationY = (vecY / distanceSqrt * 0.1D);
		this.accelerationZ = (vecZ / distanceSqrt * 0.1D);
		double pX = (player.posX + ownerLookVec3.x * 1.5D);
		double pY = (player.posY + player.getEyeHeight() + ownerLookVec3.y * 1.5D);
		double pZ = (player.posZ + ownerLookVec3.z * 1.5D);
		this.setLocationAndAngles(pX, pY, pZ, player.rotationYaw, player.rotationPitch);
	}

	public ItemStack getEntityItem()
	{
		return (ItemStack) this.getDataManager().get(ITEM);
	}

	public void setEntityItem(ItemStack stack)
	{
		this.getDataManager().set(ITEM, stack);
		this.getDataManager().setDirty(ITEM);
	}

	@Nullable
	public UUID getOwnerUUID()
	{
		return (UUID) ((Optional) this.getDataManager().get(OWNER_UUID)).orNull();
	}

	@Nullable
	public EntityPlayer getOwner()
	{
		try
		{
			UUID uuid = this.getOwnerUUID();

			return (uuid == null) ? null : this.world.getPlayerEntityByUUID(uuid);
		}
		catch (IllegalArgumentException var2)
		{
			return null;
		}
	}

	public boolean isOwner(EntityPlayer player)
	{
		if (player.getUniqueID().equals(this.getOwner().getUniqueID()))
		{
			return true;
		}

		return false;
	}

	public void setOwnerUUID(@Nullable UUID uuid)
	{
		this.getDataManager().set(OWNER_UUID, Optional.fromNullable(uuid));
	}

	public boolean isReturnOwner()
	{
		return this.isReturnOwner;
	}

	public void setReturnOwner(boolean isReturnOwner)
	{
		if (isReturnOwner)
		{
			this.motionX = this.motionY = this.motionZ = 0.0D;
		}

		this.isReturnOwner = isReturnOwner;
	}

	public int getChageAmount()
	{
		return this.chageAmmount;
	}

	public void setChageAmount(int chageAmount)
	{
		this.chageAmmount = chageAmount;
	}

	public int getAge()
	{
		return this.age;
	}

	public int getMaxAge()
	{
		return (60 * 20);
	}

	public void setAge(int age)
	{
		this.age = age;
	}

	private void onHitEntity(Entity target)
	{
		if (this.isBurning())
		{
			target.setFire(this.getChageAmount() * 20);
		}

		EntityPlayer player = (EntityPlayer) this.getOwner();
		ItemStack srcStack = player.getHeldItemMainhand();
		IAttributeInstance attackDamageAttribute = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);

		ItemStack entityStack = this.getEntityItem();
		Multimap<String, AttributeModifier> entityStackAttributeModifiers = entityStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);

		FMLLog.info("pre : %f", (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
		player.setHeldItem(EnumHand.MAIN_HAND, entityStack);
		player.getAttributeMap().applyAttributeModifiers(entityStackAttributeModifiers);

		AttributeModifier boostAttackAttributeModifier = this.getBoostAttackAttributeModifier((float) attackDamageAttribute.getAttributeValue());

		attackDamageAttribute.applyModifier(boostAttackAttributeModifier);

		player.attackTargetEntityWithCurrentItem(target);
		FMLLog.info("hit : %f", (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());

		attackDamageAttribute.removeModifier(boostAttackAttributeModifier);

		player.getAttributeMap().removeAttributeModifiers(entityStackAttributeModifiers);
		player.setHeldItem(EnumHand.MAIN_HAND, srcStack);
		FMLLog.info("post : %f", (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
	}

	private float getThrowDistance()
	{
		float distance = DISTANCE_MIN * (float) this.getChageAmount();
		distance = Math.min(distance, DISTANCE_MAX);
		distance = Math.max(distance, DISTANCE_MIN);

		return (distance * distance);
	}

	private float getThrowSpeed()
	{
		float speed = SPEED_MIN + ((float) this.getChageAmount() / 10);
		speed = Math.min(speed, SPEED_MAX);
		speed = Math.max(speed, SPEED_MIN);

		if (this.isReturnOwner())
		{
			speed = SPEED_MIN;
		}

		return speed;
	}

	private AttributeModifier getBoostAttackAttributeModifier(float srcAttackDamage)
	{
		float boostAmount = 1.0F + ((float) this.getChageAmount() / 10);
		float entityAttackDamage = (srcAttackDamage * boostAmount);

		return new AttributeModifier(BOOST_MODIFIER, (entityAttackDamage - srcAttackDamage), 0);
	}

	private static boolean isOwnerNotExists(EntityPlayer player, EntityChakram entity)
	{
		if (player == null || (player != null && player.isDead) || entity == null || (entity != null && entity.isDead))
		{
			return true;
		}

		return (!entity.world.isBlockLoaded(entity.getPosition()));
	}

	private static boolean isReflectiveBlock(World world, BlockPos pos)
	{
		return (0.0F < world.getBlockState(pos).getBlockHardness(world, pos));
	}

}
