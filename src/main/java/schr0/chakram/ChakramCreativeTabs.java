package schr0.chakram;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ChakramCreativeTabs
{

	public static final CreativeTabs ITEM = new CreativeTabs(Chakram.MOD_ID + "." + "item")
	{

		@Override
		public ItemStack getTabIconItem()
		{
			return new ItemStack(ChakramItems.CHAKRAM_NORMAL);
		}

	};

}
