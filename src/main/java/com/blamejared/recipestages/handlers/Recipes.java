package com.blamejared.recipestages.handlers;

import com.blamejared.recipestages.recipes.RecipeStage;
import com.blamejared.recipestages.reference.Reference;
import crafttweaker.*;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.*;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.recipes.*;
import crafttweaker.mc1120.CraftTweaker;
import crafttweaker.mc1120.recipes.*;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.*;
import stanhebben.zenscript.annotations.*;
import stanhebben.zenscript.annotations.Optional;

import java.util.*;


@ZenClass("mods.recipestages.Recipes")
@ZenRegister
public class Recipes {
    
    public static List<IRecipe> recipes = new LinkedList<>();
    public static Map<ResourceLocation, IItemStack> recipeOutputCache = new HashMap<>();
    private static TIntSet usedHashes = new TIntHashSet();
    
    @ZenMethod
    public static void addShaped(String name, String stage, IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        ShapedRecipe recipe = new ShapedRecipe(name, output, ingredients, function, action, false);
        IRecipe irecipe = RecipeConverter.convert(recipe, new ResourceLocation("crafttweaker", name));
        CraftTweaker.LATE_ACTIONS.add(new ActionAddRecipe(new RecipeStage(stage, irecipe, false, recipe.getWidth(), recipe.getHeight()).setRegistryName(new ResourceLocation("crafttweaker", name))));
    }
    
    @ZenMethod
    public static void addShapedMirrored(String name, String stage, IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        ShapedRecipe recipe = new ShapedRecipe(name, output, ingredients, function, action, true);
        IRecipe irecipe = RecipeConverter.convert(recipe, new ResourceLocation("crafttweaker", name));
        CraftTweaker.LATE_ACTIONS.add(new ActionAddRecipe(new RecipeStage(stage, irecipe, false, recipe.getWidth(), recipe.getHeight()).setRegistryName(new ResourceLocation("crafttweaker", name))));
    }
    
    
    @ZenMethod
    public static void addShapeless(String name, String stage, IItemStack output, IIngredient[] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        boolean valid = output != null;
        for(IIngredient ing : ingredients) {
            if(ing == null) {
                valid = false;
            }
        }
        if(!valid) {
            CraftTweakerAPI.logError("Null not allowed in shapeless recipes! Recipe for: " + output + " not created!");
            return;
        }
        ShapelessRecipe recipe = new ShapelessRecipe(name, output, ingredients, function, action);
        IRecipe irecipe = RecipeConverter.convert(recipe, new ResourceLocation("crafttweaker", name));
        CraftTweaker.LATE_ACTIONS.add(new ActionAddRecipe(new RecipeStage(stage, irecipe, true).setRegistryName(new ResourceLocation("crafttweaker", name))));
    }
    
    @ZenMethod
    public static void addShaped(String stage, IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        String name = calculateName(output, ingredients);
        ShapedRecipe recipe = new ShapedRecipe(name, output, ingredients, function, action, false);
        IRecipe irecipe = RecipeConverter.convert(recipe, new ResourceLocation("crafttweaker", name));
        CraftTweaker.LATE_ACTIONS.add(new ActionAddRecipe(new RecipeStage(stage, irecipe, false, recipe.getWidth(), recipe.getHeight()).setRegistryName(new ResourceLocation("crafttweaker", name))));
    }
    
    @ZenMethod
    public static void addShapedMirrored(String stage, IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        String name = calculateName(output, ingredients);
        ShapedRecipe recipe = new ShapedRecipe(name, output, ingredients, function, action, true);
        IRecipe irecipe = RecipeConverter.convert(recipe, new ResourceLocation("crafttweaker", name));
        CraftTweaker.LATE_ACTIONS.add(new ActionAddRecipe(new RecipeStage(stage, irecipe, false, recipe.getWidth(), recipe.getHeight()).setRegistryName(new ResourceLocation("crafttweaker", name))));
    }
    
    @ZenMethod
    public static void addShapeless(String stage, IItemStack output, IIngredient[] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        boolean valid = output != null;
        for(IIngredient ing : ingredients) {
            if(ing == null) {
                valid = false;
            }
        }
        if(!valid) {
            CraftTweakerAPI.logError("Null not allowed in shapeless recipes! Recipe for: " + output + " not created!");
            return;
        }
        String name = calculateNameShapeless(output, ingredients);
        ShapelessRecipe recipe = new ShapelessRecipe(name, output, ingredients, function, action);
        IRecipe irecipe = RecipeConverter.convert(recipe, new ResourceLocation("crafttweaker", name));
        CraftTweaker.LATE_ACTIONS.add(new ActionAddRecipe(new RecipeStage(stage, irecipe, true).setRegistryName(new ResourceLocation("crafttweaker", name))));
    }
    
    @ZenMethod
    public static void setRecipeStage(String stage, IIngredient output) {
        if (recipeOutputCache.isEmpty()) {
            for (Map.Entry<ResourceLocation, IRecipe> entry : ForgeRegistries.RECIPES.getEntries()) {
                IItemStack stack = CraftTweakerMC.getIItemStack(entry.getValue().getRecipeOutput());
                if (stack != null) {
                    recipeOutputCache.put(entry.getKey(), stack);
                }
            }
        }
        for (Map.Entry<ResourceLocation, IItemStack> entry : recipeOutputCache.entrySet()) {
            IItemStack stack = entry.getValue();
            if (output.matches(stack)) {
                IRecipe recipe = ForgeRegistries.RECIPES.getValue(entry.getKey());
                recipes.add(recipe);
            }
        }
        if(!recipes.isEmpty())
            CraftTweaker.LATE_ACTIONS.add(new ActionSetStage(recipes, stage));
        
    }
    
    @ZenMethod
    public static void setRecipeStage(String stage, String recipeName) {
        IRecipe recipe = ForgeRegistries.RECIPES.getValue(new ResourceLocation(recipeName));
        CraftTweaker.LATE_ACTIONS.add(new ActionSetStage(Collections.singletonList(recipe), stage));
        
    }
    
    private static class ActionSetStage implements IAction {
        
        private final List<IRecipe> recipes;
        private final String stage;
        
        public ActionSetStage(List<IRecipe> recipe, String stage) {
            this.recipes = recipe;
            this.stage = stage;
        }
        
        @Override
        public void apply() {
            for(IRecipe irecipe : recipes) {
                ResourceLocation registryName = irecipe.getRegistryName();
                if (registryName == null)
                    continue;

                int width = 0, height = 0;
                if(irecipe instanceof IShapedRecipe) {
                    width = ((IShapedRecipe) irecipe).getRecipeWidth();
                    height = ((IShapedRecipe) irecipe).getRecipeHeight();
                } else if(irecipe instanceof ShapedRecipe) {
                    width = ((ShapedRecipe) irecipe).getWidth();
                    height = ((ShapedRecipe) irecipe).getHeight();
                } else if(irecipe instanceof ShapedRecipeAdvanced) {
                    width = ((ShapedRecipe) ((ShapedRecipeAdvanced) irecipe).getRecipe()).getWidth();
                    height = ((ShapedRecipe) ((ShapedRecipeAdvanced) irecipe).getRecipe()).getHeight();
                }

                boolean shapeless = (width == 0 && height == 0);
                ResourceLocation newRegistryName = new ResourceLocation(Reference.MOD_ID, registryName.getResourcePath());
                IRecipe recipe = new RecipeStage(stage, irecipe, shapeless, width, height).setRegistryName(newRegistryName);
                ForgeRegistries.RECIPES.register(recipe);
                Recipes.recipes.add(recipe);
            }
        }
        
        @Override
        public String describe() {
            return "Setting the stage for recipes that output: " + recipes.get(0).getRecipeOutput().getDisplayName();
        }
    }
    
    private static String calculateName(IIngredient output, IIngredient[][] ingredients) {
        StringBuilder sb = new StringBuilder();
        sb.append(saveToString(output));
        
        for(IIngredient[] ingredient : ingredients) {
            for(IIngredient iIngredient : ingredient) {
                sb.append(saveToString(iIngredient));
            }
        }
        
        int hash = sb.toString().hashCode();
        while(usedHashes.contains(hash))
            ++hash;
        usedHashes.add(hash);
        
        return "ct_shaped" + hash;
    }
    
    public static String calculateNameShapeless(IIngredient output, IIngredient[] ingredients) {
        StringBuilder sb = new StringBuilder();
        sb.append(saveToString(output));
        
        for(IIngredient ingredient : ingredients) {
            sb.append(saveToString(ingredient));
        }
        
        int hash = sb.toString().hashCode();
        while(usedHashes.contains(hash))
            ++hash;
        usedHashes.add(hash);
        
        return "ct_shapeless" + hash;
    }
    
    public static String saveToString(IIngredient ingredient) {
        if(ingredient == null) {
            return "_";
        } else {
            return ingredient.toString();
        }
    }
    
    public static String cleanRecipeName(String s) {
        if(s.contains(":"))
            CraftTweakerAPI.logWarning("String may not contain a \":\"");
        return s.replace(":", "_");
    }
    
    private static class ActionAddRecipe implements IAction {
        
        private final IRecipe recipe;
        
        public ActionAddRecipe(IRecipe recipe) {
            this.recipe = recipe;
        }
        
        @Override
        public void apply() {
            ForgeRegistries.RECIPES.register(recipe);
            recipes.add(recipe);
        }
        
        @Override
        public String describe() {
            return "Adding recipe for " + recipe.getRecipeOutput().getDisplayName();
        }
        
    }
    
    
}
