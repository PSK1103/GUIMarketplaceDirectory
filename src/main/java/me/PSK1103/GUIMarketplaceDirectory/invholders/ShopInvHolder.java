package me.PSK1103.GUIMarketplaceDirectory.invholders;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopInvHolder implements InventoryHolder {
    final String key;
    final ItemStack item;
    final int type;
    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    public String getKey() {
        return key;
    }

    public ShopInvHolder(String key) {
        super();
        this.key = key;
        this.type = 0;
        this.item = null;
    }

    public ShopInvHolder(String key,int type) {
        super();
        this.key = key;
        this.type = type;
        this.item = null;
    }

    public ShopInvHolder(String key, ItemStack item, int type) {
        this.key = key;
        this.item = item;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public ItemStack getItem() {
        return item;
    }
}
