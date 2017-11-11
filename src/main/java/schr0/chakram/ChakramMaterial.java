package schr0.chakram;

public enum ChakramMaterial
{

	NORMAL(300, 5.0F, 15);

	private final int maxUses;
	private final float damageVsEntity;
	private final int enchantability;

	private ChakramMaterial(int maxUses, float damageVsEntity, int enchantability)
	{
		this.maxUses = maxUses;
		this.damageVsEntity = damageVsEntity;
		this.enchantability = enchantability;
	}

	public int getMaxUses()
	{
		return this.maxUses;
	}

	public float getDamageVsEntity()
	{
		return this.damageVsEntity;
	}

	public int getEnchantability()
	{
		return this.enchantability;
	}

}
