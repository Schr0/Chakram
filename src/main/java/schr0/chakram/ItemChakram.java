package schr0.chakram;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemChakram extends ItemSword
{

	private static final int THROWING_INTERVAL = (1 * 20);;
	private static final int CHAGE_AMOUNT_MIN = 1;
	private static final int CHAGE_AMOUNT_MAX = 10;

	public ItemChakram()
	{
		super(ChakramItems.TOOLMATERIAL_CHAKRAM);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		TextComponentTranslation info = new TextComponentTranslation("item.chakram.tooltip", new Object[0]);

		info.getStyle().setColor(TextFormatting.BLUE);
		info.getStyle().setItalic(true);

		tooltip.add(info.getFormattedText());
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		if (enchantment == Enchantments.POWER)
		{
			return true;
		}

		return super.canApplyAtEnchantingTable(stack, enchantment);
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
		playerIn.setActiveHand(handIn);

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
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

		if ((0 < usingCount) && (usingCount % 20 == 0))
		{
			EntityPlayer entityPlayer = (EntityPlayer) player;
			int chageAmmount = this.getChageAmmount(stack, usingCount);

			entityPlayer.addExhaustion(0.1F);

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

		EntityPlayer player = (EntityPlayer) entityLiving;
		int usingCount = this.getUsingCount(stack, timeLeft);
		int chageAmmount = this.getChageAmmount(stack, usingCount);
		EntityChakram entityChakram = new EntityChakram(worldIn, player, stack, chageAmmount);

		entityChakram.setHeadingFromOwner(player);

		entityChakram.setReturnOffHand(ItemStack.areItemStacksEqual(stack, player.getHeldItemOffhand()));

		worldIn.spawnEntity(entityChakram);

		player.addExhaustion(0.20F);

		player.getCooldownTracker().setCooldown(this, THROWING_INTERVAL);

		player.addStat(StatList.getObjectUseStats(this));

		player.inventory.deleteStack(stack);

		ChakramPackets.DISPATCHER.sendToAll(new MessagePlayerAction(player, ChakramActionTypes.SWING_ARM));

		worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (worldIn.rand.nextFloat() * 0.4F + 0.8F));

		ChakramAdvancements.completeThrowChakram(player);
	}

	// TODO /* ======================================== MOD START // =====================================*/

	public int getUsingCount(ItemStack stack, int tickCount)
	{
		return (this.getMaxItemUseDuration(stack) - tickCount);
	}

	public int getChageAmmount(ItemStack stack, int usingCount)
	{
		int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
		int chageAmount = powerLevel + (usingCount / 20);
		chageAmount = Math.min(chageAmount, CHAGE_AMOUNT_MAX);
		chageAmount = Math.max(chageAmount, CHAGE_AMOUNT_MIN);

		return chageAmount;
	}

}
