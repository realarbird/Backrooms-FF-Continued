package com.sp.init;

import com.sp.SPBRevamped;
import com.sp.item.custom.Backshroom;
import com.sp.item.custom.CannedFood;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final Item BACKSHROOM = registerItem("backshroom",
            new Backshroom(new Item.Properties().setId(itemKey("backshroom")).food(ModFoodComponents.BACKSHROOM, ModFoodComponents.BACKSHROOM_CONSUMABLE)));

    public static final Item CANNED_FOOD = registerItem("canned_food",
            new CannedFood(new Item.Properties().setId(itemKey("canned_food")).food(ModFoodComponents.CANNED_FOOD, ModFoodComponents.CANNED_FOOD_CONSUMABLE)));

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, name));
    }

    private static Item registerItem(String name, Item item){
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, name), item);
    }

    public static void registerModItems() {
        SPBRevamped.LOGGER.info("Registering Mod Items for " + SPBRevamped.MOD_ID);
    }
}
