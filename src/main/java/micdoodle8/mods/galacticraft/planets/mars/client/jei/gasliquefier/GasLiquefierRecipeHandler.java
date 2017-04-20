package micdoodle8.mods.galacticraft.planets.mars.client.jei.gasliquefier;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import micdoodle8.mods.galacticraft.core.client.jei.RecipeCategories;
import micdoodle8.mods.galacticraft.core.util.GCLog;
import micdoodle8.mods.galacticraft.planets.mars.client.jei.methanesynthesizer.MethaneSynthesizerRecipeWrapper;

import javax.annotation.Nonnull;

public class GasLiquefierRecipeHandler implements IRecipeHandler<GasLiquefierRecipeWrapper>
{
    @Nonnull
    @Override
    public Class<GasLiquefierRecipeWrapper> getRecipeClass()
    {
        return GasLiquefierRecipeWrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return RecipeCategories.GAS_LIQUEFIER_ID;
    }

    @Override
    public String getRecipeCategoryUid(GasLiquefierRecipeWrapper recipe)
    {
        return this.getRecipeCategoryUid();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull GasLiquefierRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull GasLiquefierRecipeWrapper recipe)
    {
        if (recipe.getInputs().size() != 1)
        {
            GCLog.severe(this.getClass().getSimpleName() + " JEI recipe has wrong number of inputs!");
        }
        if (recipe.getOutputs().size() != 1)
        {
            GCLog.severe(this.getClass().getSimpleName() + " JEI recipe has wrong number of outputs!");
        }
        return true;
    }
}