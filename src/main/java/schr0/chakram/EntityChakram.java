package schr0.chakram;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityChakram extends Entity
{

	private static final float SIZE_WIDTH = 0.9F;
	private static final float SIZE_HEIGHT = 0.25F;
	private static final int AGE_MAX = (15 * 20);
	private static final int TICKS_INTERVAL = 25;
	private static final int THROWING_INTERVAL = (1 * 20);
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
	private static final String TAG_RETURN_OFFHAND = TAG + "return_offhand";
	private static final String TAG_CHAGE_AMMOUNT = TAG + "chage_ammount";
	private static final String TAG_AGE = TAG + "age";

	private static final String BOOST_MODIFIER = "Boost modifier";
	private static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.<Optional<UUID>> createKey(EntityTameable.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<ItemStack> ITEM = EntityDataManager.<ItemStack> createKey(EntityChakram.class, DataSerializers.ITEM_STACK);

	private boolean isReturnOwner;
	private boolean isReturnOffHand;
	private double accelerationX;
	private double accelerationY;
	private double accelerationZ;
	private int chageAmmount;
	private int age;
	private int ticksAlive;

	public EntityChakram(World world)
	{
		super(world);
		this.setSize(SIZE_WIDTH, SIZE_HEIGHT);
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.accelerationX = this.accelerationY = this.accelerationZ = 0.0D;
		this.ticksAlive = 0;
	}

	public EntityChakram(World world, EntityPlayer player, ItemStack stack, int chageAmmount)
	{
		super(world);
		this.setSize(SIZE_WIDTH, SIZE_HEIGHT);
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.accelerationX = this.accelerationY = this.accelerationZ = 0.0D;
		this.ticksAlive = 0;

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
		compound.setByte(TAG_RETURN_OFFHAND, this.isReturnOffHand() ? (byte) 1 : (byte) 0);
		compound.setByte(TAG_CHAGE_AMMOUNT, (byte) this.getChageAmount());
		compound.setByte(TAG_AGE, (byte) this.getAge());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		boolean isDead = false;

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
			isDead = true;
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
			catch (IllegalArgumentException e)
			{
				isDead = true;
			}
		}

		ItemStack stack = new ItemStack(compound.getCompoundTag(TAG_ITEM));

		if (stack.isEmpty())
		{
			isDead = true;
		}

		this.setEntityItem(stack);
		this.setReturnOwner(compound.getByte(TAG_RETURN_OWNER) == 1);
		this.setReturnOffHand(compound.getByte(TAG_RETURN_OFFHAND) == 1);
		this.setChageAmount(compound.getByte(TAG_CHAGE_AMMOUNT));
		this.setAge(compound.getByte(TAG_AGE));

		if (isDead)
		{
			this.setDead();
		}
	}

	@Override
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
		if (this.isReturnOwner() && this.isOwner(player) && this.isServerWorld())
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
				if (this.isServerWorld())
				{
					if (this.isReturnOffHand() && player.getHeldItemOffhand().isEmpty())
					{
						player.setHeldItem(EnumHand.OFF_HAND, stack);
					}
					else
					{
						if (player.getHeldItemMainhand().isEmpty())
						{
							player.setHeldItem(EnumHand.MAIN_HAND, stack);
						}
						else
						{
							if (!player.inventory.addItemStackToInventory(stack))
							{
								player.dropItem(stack, false);
							}
						}
					}

					player.getCooldownTracker().setCooldown(stack.getItem(), THROWING_INTERVAL);
				}
			}

			if (this.isServerWorld())
			{
				for (Entity riddenEntity : this.getPassengers())
				{
					if (riddenEntity instanceof EntityItem)
					{
						ItemStack entityItemStack = ((EntityItem) riddenEntity).getItem();

						if (!player.inventory.addItemStackToInventory(entityItemStack))
						{
							player.dropItem(entityItemStack, false);
						}
					}
				}
			}
		}
		else
		{
			if (!isBreakItemDmage && this.isServerWorld())
			{
				this.entityDropItem(stack, 0.5F);
			}
		}

		super.setDead();
	}

	@Override
	public void onUpdate()
	{
		if (isOwnerNotExists(this, this.getOwner()))
		{
			boolean isRestartInterval = (TICKS_INTERVAL <= this.ticksExisted);

			if (isRestartInterval && this.isServerWorld())
			{
				this.setDead();
			}

			return;

		}
		else
		{
			if (this.isServerWorld())
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

		++this.ticksAlive;

		RayTraceResult rayTraceResult = ProjectileHelper.forwardsRaycast(this, true, (TICKS_INTERVAL <= this.ticksAlive), this.getOwner());
		BlockPos blockPos = new BlockPos(this);

		if (rayTraceResult != null)
		{
			if (rayTraceResult.entityHit != null)
			{
				Entity entity = rayTraceResult.entityHit;

				if (!this.isOwner(entity))
				{
					this.onHitEntity(entity);
				}
			}
			else
			{
				blockPos = rayTraceResult.getBlockPos();
			}
		}

		if (!this.world.isAirBlock(blockPos))
		{
			IBlockState blockState = this.world.getBlockState(blockPos);
			boolean canDestroyBlock = (blockState.getBlock() == Blocks.WEB) || (blockState.getBlockHardness(this.world, blockPos) == 0.0F);

			if (canDestroyBlock && EntityWither.canDestroyBlock(blockState.getBlock()))
			{
				this.world.destroyBlock(blockPos, true);
			}
		}

		if (this.isReturnOwner())
		{
			double ownerVecX = (this.getOwner().posX - this.posX);
			double ownerVecY = ((this.getOwner().posY + (double) (this.getOwner().height / 2.0F)) - this.posY);
			double ownerVecZ = (this.getOwner().posZ - this.posZ);
			ownerVecX += this.rand.nextGaussian() * 0.4D;
			ownerVecY += this.rand.nextGaussian() * 0.4D;
			ownerVecZ += this.rand.nextGaussian() * 0.4D;
			double distanceSqrt = (double) MathHelper.sqrt(ownerVecX * ownerVecX + ownerVecY * ownerVecY + ownerVecZ * ownerVecZ);
			this.accelerationX = (ownerVecX / distanceSqrt * 0.1D);
			this.accelerationY = (ownerVecY / distanceSqrt * 0.1D);
			this.accelerationZ = (ownerVecZ / distanceSqrt * 0.1D);

			if (!this.isBeingRidden())
			{
				double rangeXYZ = 1.25D;

				for (EntityItem aroundEntityItem : this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().grow(rangeXYZ, rangeXYZ, rangeXYZ)))
				{
					if (aroundEntityItem.getItem().isEmpty())
					{
						continue;
					}

					if (this.isServerWorld())
					{
						aroundEntityItem.startRiding(this);
					}
				}
			}
		}
		else
		{
			boolean isMaxThrowDistance = (this.getThrowDistance() < this.getOwner().getDistanceSq(this));
			boolean isReflectiveBlock = isReflectiveBlock(world, blockPos);

			if (isMaxThrowDistance || isReflectiveBlock)
			{
				this.motionX = this.motionY = this.motionZ = 0.0D;
				this.accelerationX = this.accelerationY = this.accelerationZ = 0.0D;
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

	// TODO /* ======================================== MOD START // =====================================*/

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
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	public boolean isOwner(Entity owner)
	{
		if (owner instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) owner;

			if (player.getUniqueID().equals(this.getOwner().getUniqueID()))
			{
				return true;
			}
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
		this.isReturnOwner = isReturnOwner;
	}

	public boolean isReturnOffHand()
	{
		return this.isReturnOffHand;
	}

	public void setReturnOffHand(boolean isReturnOffHand)
	{
		this.isReturnOffHand = isReturnOffHand;
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
		return AGE_MAX;
	}

	public void setAge(int age)
	{
		this.age = age;
	}

	public void setHeadingFromOwner(EntityPlayer owner)
	{
		Vec3d ownerLookVec = owner.getLookVec();
		double lookDistance = 128.0D;
		double ownerLookVecX = (ownerLookVec.x * lookDistance);
		double ownerLookVecY = (ownerLookVec.y * lookDistance);
		double ownerLookVecZ = (ownerLookVec.z * lookDistance);
		ownerLookVecX += (this.rand.nextGaussian() * 0.4D);
		ownerLookVecY += (this.rand.nextGaussian() * 0.4D);
		ownerLookVecZ += (this.rand.nextGaussian() * 0.4D);
		double distanceSqrt = (double) MathHelper.sqrt(ownerLookVecX * ownerLookVecX + ownerLookVecY * ownerLookVecY + ownerLookVecZ * ownerLookVecZ);
		this.accelerationX = (ownerLookVecX / distanceSqrt * 0.1D);
		this.accelerationY = (ownerLookVecY / distanceSqrt * 0.1D);
		this.accelerationZ = (ownerLookVecZ / distanceSqrt * 0.1D);
		double pX = (owner.posX + ownerLookVec.x * 1.5D);
		double pY = (owner.posY + owner.getEyeHeight() + ownerLookVec.y * 1.5D);
		double pZ = (owner.posZ + ownerLookVec.z * 1.5D);
		this.setLocationAndAngles(pX, pY, pZ, owner.rotationYaw, owner.rotationPitch);
	}

	private void onHitEntity(Entity target)
	{
		if (this.isBurning())
		{
			target.setFire(this.getChageAmount());
		}

		if (this.getOwner() != null)
		{
			EntityPlayer player = (EntityPlayer) this.getOwner();
			ItemStack srcStack = player.getHeldItemMainhand().copy();
			ItemStack dstStack = this.getEntityItem();
			Multimap<String, AttributeModifier> srcStackAttributeModifiers = srcStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
			Multimap<String, AttributeModifier> dstStackAttributeModifiers = dstStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);

			player.setHeldItem(EnumHand.MAIN_HAND, dstStack);
			player.getAttributeMap().applyAttributeModifiers(dstStackAttributeModifiers);

			try
			{
				AttributeModifier boostAttackAttributeModifier = this.getBoostAttackAttributeModifier(player);

				player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(boostAttackAttributeModifier);
				player.attackTargetEntityWithCurrentItem(target);
				player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).removeModifier(boostAttackAttributeModifier);
			}
			catch (IllegalArgumentException e)
			{
				ChakramDebug.infoBugMessage(player, this.getClass());
			}

			player.setHeldItem(EnumHand.MAIN_HAND, srcStack);
			player.getAttributeMap().applyAttributeModifiers(srcStackAttributeModifiers);
		}
		else
		{
			target.attackEntityFrom(DamageSource.GENERIC, ChakramItems.TOOLMATERIAL_CHAKRAM.getAttackDamage());
		}
	}

	private boolean isServerWorld()
	{
		return !this.world.isRemote;
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

		if (this.isInWater())
		{
			speed = (speed / 1.25F);

		}

		return speed;
	}

	private AttributeModifier getBoostAttackAttributeModifier(EntityPlayer owner)
	{
		float srcAttackDamage = (float) owner.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		float boostAttackDamage = srcAttackDamage * (1.0F + ((float) this.getChageAmount() / 10));

		if (this.isReturnOwner())
		{
			boostAttackDamage = (srcAttackDamage / 2);
		}

		if (this.isInWater())
		{
			boostAttackDamage = (srcAttackDamage / 3);
		}

		return new AttributeModifier(BOOST_MODIFIER, boostAttackDamage, 0);
	}

	private static boolean isOwnerNotExists(EntityChakram entityChakram, EntityPlayer owner)
	{
		if (entityChakram == null || owner == null || (entityChakram != null && entityChakram.isDead) || (owner != null && owner.isDead))
		{
			return true;
		}

		return (!entityChakram.world.isBlockLoaded(new BlockPos(entityChakram)));
	}

	private static boolean isReflectiveBlock(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);

		if (state.getMaterial() != Material.AIR)
		{
			AxisAlignedBB axisalignedbb = state.getCollisionBoundingBox(world, pos);

			if (axisalignedbb != Block.NULL_AABB)
			{
				return true;
			}
		}

		return false;
	}

}
