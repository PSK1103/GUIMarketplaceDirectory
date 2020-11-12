package me.PSK1103.GUIMarketplaceDirectory.InvHolders;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MarketplaceBookHolder implements InventoryHolder {
    List<Map<String,String>> shops;
    int type;

    public MarketplaceBookHolder(List<Map<String, String>> shops) {
        this.shops = shops;
        this.type = 0;
    }

    public MarketplaceBookHolder(List<Map<String, String>> shops,int type) {
        this.shops = shops;
        this.type = type;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    public List<Map<String, String>> getShops() {
        return shops;
    }

    public int getType() {
        return type;
    }
}
