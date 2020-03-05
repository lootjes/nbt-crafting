package de.siphalor.nbtcrafting.recipetype.anvil;

import com.google.common.collect.ImmutableMap;
import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import de.siphalor.nbtcrafting.api.nbt.NbtHelper;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.ingredient.IIngredient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

import java.util.Map;

public class AnvilRecipe implements Recipe<Inventory>, ServerRecipe {
	private final Identifier identifier;
	public final Ingredient base;
	public final Ingredient ingredient;
	public final ItemStack output;
	public final int levels;
	private final Dollar[] dollars;

	public AnvilRecipe(Identifier id, Ingredient base, Ingredient ingredient, ItemStack output, int levels) {
		this.identifier = id;
		this.base = base;
		this.ingredient = ingredient;
		this.output = output;
		this.levels = levels;
		this.dollars = DollarParser.extractDollars(output.getTag(), true);
	}

	public void write(PacketByteBuf packetByteBuf) {
		packetByteBuf.writeIdentifier(identifier);
		base.write(packetByteBuf);
		ingredient.write(packetByteBuf);
		packetByteBuf.writeItemStack(output);
		packetByteBuf.writeVarInt(levels);
	}

	public static AnvilRecipe from(PacketByteBuf packetByteBuf) {
		Identifier identifier = packetByteBuf.readIdentifier();
		Ingredient base = Ingredient.fromPacket(packetByteBuf);
		Ingredient ingredient = Ingredient.fromPacket(packetByteBuf);
		ItemStack output = packetByteBuf.readItemStack();
		int levels = packetByteBuf.readVarInt();
		return new AnvilRecipe(identifier, base, ingredient, output, levels);
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		if(ingredient != null && !ingredient.test(inventory.getInvStack(1))) return false;
		return base.test(inventory.getInvStack(0));
	}

	@Override
	public ItemStack craft(Inventory inventory) {
		Map<String, Object> reference = ImmutableMap.of("base", NbtHelper.getTagOrEmpty(inventory.getInvStack(0)), "ingredient", NbtHelper.getTagOrEmpty(inventory.getInvStack(1)));

		//noinspection ConstantConditions
		ItemStack remainder = ((IIngredient)(Object) base).getRecipeRemainder(inventory.getInvStack(0), reference);
		if(remainder != null) inventory.setInvStack(0, remainder);
		//noinspection ConstantConditions
		remainder = ((IIngredient)(Object) base).getRecipeRemainder(inventory.getInvStack(1), reference);
		if(remainder != null) inventory.setInvStack(1, remainder);

		return RecipeUtil.applyDollars(output.copy(), dollars, reference);
	}

	@Override
	public boolean fits(int var1, int var2) {
		return false;
	}

	@Override
	public ItemStack getOutput() {
		return output;
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NbtCrafting.ANVIL_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return NbtCrafting.ANVIL_RECIPE_TYPE;
	}
}