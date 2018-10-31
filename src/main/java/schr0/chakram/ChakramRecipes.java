package schr0.chakram;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

public class ChakramRecipes
{

	public static final String KEY_RES = Chakram.MOD_ID;
	public static final ResourceLocation RES_CHAKRAM_NORMAL = new ResourceLocation(KEY_RES, ChakramItems.NAME_CHAKRAM_NORMAL);

	public void registerRecipes(IForgeRegistry<IRecipe> registry)
	{
		registry.register(getItemChakramNormal());
	}

	// TODO /* ======================================== MOD START =====================================*/

	private static IRecipe getItemChakramNormal()
	{
		return new ShapedOreRecipe(RES_CHAKRAM_NORMAL, new ItemStack(ChakramItems.CHAKRAM_NORMAL), new Object[]
		{
				" X ",
				"XYX",
				"ZX ",

				'X', new ItemStack(Items.IRON_INGOT),
				'Y', new ItemStack(Items.ENDER_PEARL),
				'Z', new ItemStack(Items.LEATHER),

		}).setRegistryName(RES_CHAKRAM_NORMAL);
	}

}
