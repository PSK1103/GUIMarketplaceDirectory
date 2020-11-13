package me.PSK1103.GUIMarketplaceDirectory.EventHandler;

import me.PSK1103.GUIMarketplaceDirectory.GUIMarketplaceDirectory;
import me.PSK1103.GUIMarketplaceDirectory.InvHolders.ShopInvHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class ItemEvents implements Listener {
    GUIMarketplaceDirectory plugin;

    public ItemEvents(GUIMarketplaceDirectory plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public final void addItem(InventoryClickEvent itemEvent) {
        if(!itemEvent.isRightClick())
            return;
        if(itemEvent.getCursor() != null && itemEvent.getCursor().getType() == Material.WRITTEN_BOOK) {
            itemEvent.setCancelled(true);
            BookMeta bookMeta = ((BookMeta) itemEvent.getCursor().getItemMeta());

            if(!bookMeta.getTitle().toLowerCase().equals("[shop init]") && !bookMeta.getTitle().toLowerCase().equals("init shop")) {
                return;
            }

            ItemStack item = itemEvent.getCurrentItem();
            String name = item.getType().getKey().getKey().toUpperCase();
            System.out.println(name);
            Player player = ((Player) itemEvent.getWhoClicked());
            Inventory inventory = itemEvent.getInventory();
            if(inventory.firstEmpty() != -1) {
                inventory.setItem(inventory.firstEmpty(), itemEvent.getCursor());
                itemEvent.setCursor(new ItemStack(Material.AIR));
                player.updateInventory();
            }
            else if(player.getInventory().firstEmpty() !=-1) {
                player.getInventory().setItem(player.getInventory().firstEmpty(),itemEvent.getCursor());
                itemEvent.setCursor(new ItemStack(Material.AIR));
                player.updateInventory();
            }
            player.closeInventory();

            if(!plugin.getShopRepo().isShopOwner(player.getUniqueId().toString(),bookMeta.getPage(bookMeta.getPageCount()))) {
                player.sendMessage(ChatColor.RED + "You do not have permission to edit this shop");
                return;
            }

            if(plugin.getShopRepo().getIsAddingOwner(bookMeta.getPage(bookMeta.getPageCount())) || plugin.getShopRepo().isUserRejectingShop(player.getUniqueId().toString()) || plugin.getShopRepo().isUserRemovingShop(player.getUniqueId().toString())) {
                player.sendMessage(ChatColor.RED + "Cannot add items to shop right now");
                return;
            }

            if(plugin.getShopRepo().getIsUserAddingOwner(player.getUniqueId().toString())) {
                player.sendMessage(ChatColor.RED + "Finish adding owner to shop first");
                return;
            }

            if(plugin.getShopRepo().isShopUnderEditOrAdd(bookMeta.getPage(bookMeta.getPageCount()))) {
                player.sendMessage(ChatColor.RED + "This shop is currently under some other operation, try again later");
                return;
            }

            player.sendMessage(ChatColor.GREEN + "Set quantity (in format shulker:stack:num)");
            if(item.getItemMeta().hasDisplayName()) {
                int res = plugin.getShopRepo().initItemAddition(player.getUniqueId().toString(), bookMeta.getPage(bookMeta.getPageCount()), name,item.getItemMeta().getDisplayName());
                if (res == -1) {
                    player.sendMessage(ChatColor.RED + "shop not found!");
                }
                else if (res == 0) {
                    player.sendMessage(ChatColor.GRAY + "Cancelling addition of previous item");
                }
            }
            else {
                int res = plugin.getShopRepo().initItemAddition(player.getUniqueId().toString(), bookMeta.getPage(bookMeta.getPageCount()), name);
                if (res == -1) {
                    player.sendMessage(ChatColor.RED + "shop not found!");
                }
                else if (res == 0) {
                    player.sendMessage(ChatColor.GRAY + "Cancelling addition of previous item");
                }
            }
        }
    }

    @EventHandler
    public final void addItemData(AsyncPlayerChatEvent addItemDetails) {
        if(addItemDetails.getMessage().matches("\\d+:\\d+:\\d+") && plugin.getShopRepo().isAddingItem(addItemDetails.getPlayer().getUniqueId().toString())) {
            plugin.getShopRepo().setQty(addItemDetails.getMessage(),addItemDetails.getPlayer().getUniqueId().toString());
            addItemDetails.getPlayer().sendMessage(ChatColor.GREEN + "Enter price");
            addItemDetails.setCancelled(true);
            return;
        }
        if(addItemDetails.getMessage().matches("\\d+") && plugin.getShopRepo().isAddingItem(addItemDetails.getPlayer().getUniqueId().toString())) {
            plugin.getShopRepo().setPrice(Integer.parseInt(addItemDetails.getMessage()),addItemDetails.getPlayer().getUniqueId().toString());
            addItemDetails.getPlayer().sendMessage(ChatColor.GOLD + "Item added successfully!");
            addItemDetails.setCancelled(true);
            return;
        }

        if(plugin.getShopRepo().isAddingItem(addItemDetails.getPlayer().getUniqueId().toString())) {
            plugin.getShopRepo().stopEditing(addItemDetails.getPlayer().getUniqueId().toString());
            addItemDetails.getPlayer().sendMessage(ChatColor.GRAY + "Cancelled item addition");
        }
    }

    @EventHandler
    public final void checkForAlternatives(InventoryClickEvent itemCheckEvent) {
        if(itemCheckEvent.getInventory().getHolder() instanceof ShopInvHolder) {
            itemCheckEvent.setCancelled(true);

            if(((ShopInvHolder) itemCheckEvent.getInventory().getHolder()).getKey().length() == 0) {
                return;
            }

            if(itemCheckEvent.isRightClick() && itemCheckEvent.getCurrentItem() != null && itemCheckEvent.getCurrentItem().getType() != Material.AIR && itemCheckEvent.getCurrentItem().getType() != Material.BARRIER) {
                plugin.getShopRepo().findBetterAlternative(((Player) itemCheckEvent.getWhoClicked()),((ShopInvHolder)itemCheckEvent.getInventory().getHolder()).getKey() ,itemCheckEvent.getRawSlot());
            }
        }
    }

}
