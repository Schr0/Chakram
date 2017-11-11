package schr0.chakram;

import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemChakram extends Item
{

	private static final String CHAKRAM_MODIFIER = "Chakram modifier";

	private static final int USING_COUNT_MIN = (1 * 20);

	private static final int CHAGE_AMOUNT_MIN = 1;
	private static final int CHAGE_AMOUNT_MAX = 10;

	private static final int CHAGE_INTERVAL = (20 / 2);

	private final float damageVsEntity;
	private final int enchantability;

	public ItemChakram(ChakramMaterial material)
	{
		super();
		this.maxStackSize = 1;
		this.setMaxDamage(material.getMaxUses());

		this.damageVsEntity = material.getDamageVsEntity();
		this.enchantability = material.getEnchantability();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
	{
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EntityEquipmentSlot.MAINHAND)
		{
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, CHAKRAM_MODIFIER, (double) this.damageVsEntity, 0));
		}

		return multimap;
	}

	@Override
	public int getItemEnchantability()
	{
		return this.enchantability;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		if (enchantment.type == EnumEnchantmentType.WEAPON)
		{
			return true;
		}

		if (enchantment == Enchantments.POWER)
		{
			return true;
		}

		return super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player)
	{
		return false;
	}

	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state)
	{
		Block block = state.getBlock();

		if (block == Blocks.WEB)
		{
			return 15.0F;
		}

		Material material = state.getMaterial();

		if ((material == Material.PLANTS) || (material == Material.VINE) || (material == Material.CORAL) || (material == Material.LEAVES) || (material == Material.GOURD))
		{
			return 1.5F;
		}

		return 1.0F;
	}

	@Override
	public boolean canHarvestBlock(IBlockState blockIn)
	{
		return (blockIn.getBlock() == Blocks.WEB);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving)
	{
		if ((double) state.getBlockHardness(worldIn, pos) != 0.0D)
		{
			if (!worldIn.isRemote)
			{
				stack.damageItem(2, entityLiving);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
	{
		stack.damageItem(1, attacker);

		return true;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack stack = playerIn.getHeldItem(handIn);

		playerIn.setActiveHand(handIn);

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count)
	{
		World world = player.getEntityWorld();

		if (world.isRemote || !(player instanceof EntityPlayer))
		{
			return;
		}

		int usingCount = this.getUsingCount(stack, count);

		if (usingCount < USING_COUNT_MIN)
		{
			return;
		}

		if (usingCount % 20 == 0)
		{
			EntityPlayer entityPlayer = (EntityPlayer) player;
			int chageAmmount = this.getChageAmmount(EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack), usingCount);

			entityPlayer.addExhaustion(0.15F);

			if (chageAmmount == CHAGE_AMOUNT_MAX)
			{
				ChakramPackets.DISPATCHER.sendToAll(new MessageParticleEntity(entityPlayer, ChakramParticles.ITEM_CHAGE_MAX));

				world.playSound(null, entityPlayer.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.25F, 1.0F);
			}
			else
			{
				ChakramPackets.DISPATCHER.sendToAll(new MessageParticleEntity(entityPlayer, ChakramParticles.ITEM_CHAGE));

				world.playSound(null, entityPlayer.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.25F, (0.5F + ((float) chageAmmount / 10)));
			}
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
	{
		if (worldIn.isRemote || !(entityLiving instanceof EntityPlayer))
		{
			return;
		}

		int usingCount = this.getUsingCount(stack, timeLeft);

		if (usingCount < USING_COUNT_MIN)
		{
			return;
		}

		EntityPlayer player = (EntityPlayer) entityLiving;
		EntityChakram entityChakram = new EntityChakram(worldIn, player, stack, this.getChageAmmount(EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack), usingCount));

		entityChakram.setHeadingFromOwner(player);

		worldIn.spawnEntity(entityChakram);

		player.getCooldownTracker().setCooldown(this, CHAGE_INTERVAL);

		player.addStat(StatList.getObjectUseStats(this));

		player.inventory.deleteStack(stack);

		ChakramPackets.DISPATCHER.sendToAll(new MessagePlayerAction(player, ChakramActionTypes.SWING_ARM));

		worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (worldIn.rand.nextFloat() * 0.4F + 0.8F));
	}

	// TODO /* ======================================== MOD START =====================================*/

	public int getUsingCount(ItemStack stack, int timeLeft)
	{
		return (this.getMaxItemUseDuration(stack) - timeLeft);
	}

	public int getChageAmmount(int powerLevel, int usingCount)
	{
		int chageAmount = powerLevel + (usingCount / 20);
		chageAmount = Math.min(chageAmount, CHAGE_AMOUNT_MAX);
		chageAmount = Math.max(chageAmount, CHAGE_AMOUNT_MIN);

		return chageAmount;
	}

}
