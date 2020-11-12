package me.PSK1103.GUIMarketplaceDirectory.InvHolders;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ShopInvHolder implements InventoryHolder {
    String key;
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
    }
}
