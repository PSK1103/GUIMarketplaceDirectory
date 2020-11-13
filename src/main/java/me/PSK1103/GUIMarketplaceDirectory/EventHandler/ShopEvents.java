package me.PSK1103.GUIMarketplaceDirectory.EventHandler;

import me.PSK1103.GUIMarketplaceDirectory.GUIMarketplaceDirectory;
import me.PSK1103.GUIMarketplaceDirectory.InvHolders.MarketplaceBookHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class ShopEvents implements Listener {

    private GUIMarketplaceDirectory plugin;

    public ShopEvents(GUIMarketplaceDirectory plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public final void onShopAdd(PlayerEditBookEvent editBookEvent) {
        if(!editBookEvent.isSigning())
            return;

        BookMeta meta = editBookEvent.getNewBookMeta();
        if(meta.getTitle().toLowerCase().equals("[init shop]") || meta.getTitle().toLowerCase().equals("[shop init]")) {

            StringBuilder desc = new StringBuilder();
            for(int i = 0;i<meta.getPageCount();i++) {
                desc.append(meta.getPage(i+1));
            }
            String data = desc.toString();
            if(!data.matches("\\Q[\\E.*\\Q]\\E\\s*\\Q[\\E.*\\Q]\\E")) {
                editBookEvent.getPlayer().sendMessage(ChatColor.RED + "Incorrect shop initialisation, try again");
                editBookEvent.setCancelled(true);
                return;
            }

            if(plugin.getShopRepo().isAddingItem(editBookEvent.getPlayer().getUniqueId().toString())) {
                editBookEvent.getPlayer().sendMessage(ChatColor.RED + "Finish adding item first");
                editBookEvent.setCancelled(true);
                return;
            }

            String name = data.substring(data.indexOf("[")+1,data.indexOf("]"));
            String d = data.substring(data.lastIndexOf("[")+1,data.lastIndexOf("]"));;
            String key = "" + System.currentTimeMillis() + editBookEvent.getPlayer().getUniqueId().toString();
            String loc = editBookEvent.getPlayer().getLocation().getBlockX() + "," + editBookEvent.getPlayer().getLocation().getBlockZ();
            meta.addPage(key);
            meta.setDisplayName(ChatColor.GOLD + name);
            editBookEvent.setNewBookMeta(meta);

            if(!plugin.getCustomConfig().getBoolean("multi-owner",false))
                plugin.getShopRepo().addShopAsOwner(name,d,editBookEvent.getPlayer().getName(),editBookEvent.getPlayer().getUniqueId().toString(),key,loc);
            else {
                Player player = editBookEvent.getPlayer();
                plugin.getShopRepo().addShop(name,d,player.getName(),player.getUniqueId().toString(),key,loc);
                player.sendMessage(ChatColor.YELLOW + "Are you the owner of " + name + " ? (Y/N)");
                return;
            }

            editBookEvent.getPlayer().sendMessage(ChatColor.GOLD + "Shop initialised successfully!");
        }
        else if(meta.getTitle().equalsIgnoreCase("[Marketplace]")) {
            meta.setDisplayName(ChatColor.GOLD + "Marketplace Directory");
            editBookEvent.setNewBookMeta(meta);
            if(meta.getPage(1).contains("PSK is the best")) {
                editBookEvent.getPlayer().sendMessage(ChatColor.AQUA + "Gee thanks!");
            }
        }
    }

    @EventHandler
    public final void openShopDirectory(PlayerInteractEvent shopDirectoryOpenEvent) {
        if(shopDirectoryOpenEvent.hasItem() && shopDirectoryOpenEvent.getItem().getType() == Material.WRITTEN_BOOK && shopDirectoryOpenEvent.getAction() == Action.RIGHT_CLICK_AIR) {
            BookMeta bookMeta = (BookMeta) shopDirectoryOpenEvent.getItem().getItemMeta();
            if(bookMeta.getTitle().equals("[Marketplace]")) {
                shopDirectoryOpenEvent.setCancelled(true);
                plugin.gui.openShopDirectory(shopDirectoryOpenEvent.getPlayer());
            }
            if(bookMeta.getTitle().toLowerCase().equals("[shop init]") || bookMeta.getTitle().equals("[init shop]")) {
                shopDirectoryOpenEvent.setCancelled(true);
                if(!plugin.getCustomConfig().getBoolean("multi-owner",false)) {
                    return;
                }
                if(plugin.getShopRepo().isShopOwner(shopDirectoryOpenEvent.getPlayer().getUniqueId().toString(),bookMeta.getPage(bookMeta.getPageCount()))) {
                    if(plugin.getShopRepo().isAddingItem(shopDirectoryOpenEvent.getPlayer().getUniqueId().toString())) {
                        shopDirectoryOpenEvent.getPlayer().sendMessage(ChatColor.RED + "Finish adding item first");
                        return;
                    }
                    if(plugin.getShopRepo().getIsUserAddingOwner(shopDirectoryOpenEvent.getPlayer().getUniqueId().toString()) && !plugin.getShopRepo().getIsAddingOwner(bookMeta.getPage(bookMeta.getPageCount()))) {
                        shopDirectoryOpenEvent.getPlayer().sendMessage(ChatColor.RED + "Finish adding owner to other shop first");
                        return;
                    }
                    if(plugin.getShopRepo().isShopUnderEditOrAdd(bookMeta.getPage(bookMeta.getPageCount()))) {
                        shopDirectoryOpenEvent.getPlayer().sendMessage(ChatColor.RED + "This shop is currently under some other operation, try again later");
                        return;
                    }
                    plugin.getShopRepo().startAddingOwner(shopDirectoryOpenEvent.getPlayer().getUniqueId().toString(),bookMeta.getPage(bookMeta.getPageCount()));
                    shopDirectoryOpenEvent.getPlayer().sendMessage(new String[]{ChatColor.GRAY + "Adding another owner...",ChatColor.YELLOW + "Enter player name (nil to cancel)"});
                }

            }
        }
    }

    @EventHandler
    public final void selectShop(InventoryClickEvent shopSelectEvent) {
        if(shopSelectEvent.getInventory().getHolder() instanceof MarketplaceBookHolder) {
            shopSelectEvent.setCancelled(true);
            if(shopSelectEvent.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
                int currPage = 1;
                if(shopSelectEvent.getInventory().getSize() == 54) {
                    if(shopSelectEvent.getInventory().getItem(45).getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
                        currPage = Integer.parseInt(shopSelectEvent.getInventory().getItem(45).getItemMeta().getDisplayName().substring(5));
                    }
                }
                MarketplaceBookHolder holder = ((MarketplaceBookHolder) shopSelectEvent.getInventory().getHolder());
                if(holder.getType() == 0) {
                    shopSelectEvent.getWhoClicked().closeInventory();
                    plugin.gui.openShopInventory((Player) shopSelectEvent.getWhoClicked(), shopSelectEvent.getRawSlot(), currPage, ((MarketplaceBookHolder) shopSelectEvent.getInventory().getHolder()).getShops());
                }
                else if (holder.getType() == 1) {
                    if(shopSelectEvent.isRightClick()) {
                        if(plugin.getShopRepo().isUserRejectingShop(shopSelectEvent.getWhoClicked().getUniqueId().toString()) || plugin.getShopRepo().isUserRemovingShop(shopSelectEvent.getWhoClicked().getUniqueId().toString())) {
                            shopSelectEvent.getWhoClicked().sendMessage(ChatColor.RED + "Confirm rejection of previous shop first!");
                            return;
                        }
                        plugin.getShopRepo().approveShop(holder.getShops().get(shopSelectEvent.getRawSlot() + 45 * (currPage-1)).get("key"));
                        shopSelectEvent.getWhoClicked().sendMessage(ChatColor.GREEN + "Shop approved");
                        shopSelectEvent.getWhoClicked().closeInventory();
                        plugin.gui.openShopDirectoryModerator(((Player) shopSelectEvent.getWhoClicked()),1);
                        return;
                    }
                    if(shopSelectEvent.isLeftClick()) {
                        if(plugin.getShopRepo().isUserRejectingShop(shopSelectEvent.getWhoClicked().getUniqueId().toString())) {
                            shopSelectEvent.getWhoClicked().sendMessage(ChatColor.RED + "Confirm rejection of previous shop first!");
                            return;
                        }
                        plugin.getShopRepo().addShopToRejectQueue(shopSelectEvent.getWhoClicked().getUniqueId().toString(),holder.getShops().get(shopSelectEvent.getRawSlot() + 45 * (currPage-1)).get("key"));
                        shopSelectEvent.getWhoClicked().sendMessage(ChatColor.YELLOW + "Do you wish to reject this shop? (Y/N)");
                        shopSelectEvent.getWhoClicked().closeInventory();
                        return;
                    }
                }
                else if(holder.getType() == 2) {
                    if(shopSelectEvent.isRightClick()) {
                        if(plugin.getShopRepo().isUserRemovingShop(shopSelectEvent.getWhoClicked().getUniqueId().toString())) {
                            shopSelectEvent.getWhoClicked().sendMessage(ChatColor.RED + "Confirm removal of previous shop first!");
                            return;
                        }
                        plugin.getShopRepo().addShopToRemoveQueue(shopSelectEvent.getWhoClicked().getUniqueId().toString(),holder.getShops().get(shopSelectEvent.getRawSlot() + 45 * (currPage-1)).get("key"));
                        shopSelectEvent.getWhoClicked().sendMessage(ChatColor.YELLOW + "Do you wish to remove this shop? (Y/N)");
                        shopSelectEvent.getWhoClicked().closeInventory();
                        return;
                    }
                    return;
                }
            }
            if(shopSelectEvent.getCurrentItem().getType() == Material.LIME_STAINED_GLASS_PANE) {
                int currPage = Integer.parseInt(shopSelectEvent.getInventory().getItem(45).getItemMeta().getDisplayName().substring(5));
                plugin.gui.nextPage(((Player) shopSelectEvent.getWhoClicked()),currPage);
            }
            if(shopSelectEvent.getCurrentItem().getType() == Material.ORANGE_STAINED_GLASS_PANE) {
                int currPage = Integer.parseInt(shopSelectEvent.getInventory().getItem(45).getItemMeta().getDisplayName().substring(5));
                plugin.gui.prevPage(((Player) shopSelectEvent.getWhoClicked()),currPage);
            }
        }
    }

    @EventHandler
    public final void ownerAddEvent(AsyncPlayerChatEvent chatEvent) {

        if(plugin.getShopRepo().getIsUserAddingOwner(chatEvent.getPlayer().getUniqueId().toString())) {
            chatEvent.setCancelled(true);

            String uuid = chatEvent.getPlayer().getUniqueId().toString();
            if (plugin.getShopRepo().getEditType(uuid) <= 0) {
                return;
            }

            if (plugin.getShopRepo().getEditType(uuid) == 2) {
                if (chatEvent.getMessage().equalsIgnoreCase("Y") || chatEvent.getMessage().equalsIgnoreCase("yes")) {
                    plugin.getShopRepo().addOwner(uuid, chatEvent.getPlayer());
                    chatEvent.getPlayer().sendMessage(ChatColor.GOLD + "Shop initialised successfully!");
                } else if (chatEvent.getMessage().equalsIgnoreCase("n") || chatEvent.getMessage().equalsIgnoreCase("no")) {
                    plugin.getShopRepo().initShopOwnerAddition(uuid);
                    chatEvent.getPlayer().sendMessage(ChatColor.YELLOW + "Enter owner's name (type nil to cancel)");
                } else {
                    plugin.getShopRepo().stopInitOwner(uuid);
                    chatEvent.getPlayer().sendMessage(new String[]{ChatColor.GRAY + "Didn't get proper response", ChatColor.YELLOW + "Are you the owner of this shop? (Y/N)"});
                }
            } else if (plugin.getShopRepo().getEditType(uuid) == 1) {

                if (chatEvent.getMessage().equalsIgnoreCase("nil")) {
                    plugin.getShopRepo().stopInitOwner(uuid);
                    chatEvent.getPlayer().sendMessage(ChatColor.GRAY + "Cancelled adding another owner");
                    return;
                }
                String playerName = chatEvent.getMessage();
                List<Player> players = plugin.getServer().matchPlayer(playerName);
                if (players.size() == 0) {
                    chatEvent.getPlayer().sendMessage(ChatColor.RED + "No player found, try again");
                } else if (players.size() > 1) {
                    chatEvent.getPlayer().sendMessage(ChatColor.YELLOW + "Multiple players found, be more specific");
                } else {
                    plugin.getShopRepo().addOwner(chatEvent.getPlayer().getUniqueId().toString(), players.get(0));
                    chatEvent.getPlayer().sendMessage(ChatColor.GOLD + "Owner added successfully");
                }
            }
            return;
        }
        if(plugin.getShopRepo().isUserRejectingShop(chatEvent.getPlayer().getUniqueId().toString())) {
            chatEvent.setCancelled(true);
            String message = chatEvent.getMessage();
            if(message.equalsIgnoreCase("y") || message.equalsIgnoreCase("yes")) {
                plugin.getShopRepo().rejectShop(chatEvent.getPlayer().getUniqueId().toString());
                chatEvent.getPlayer().sendMessage(ChatColor.GREEN + "Rejected shop successfully");
            }
            else if(message.equalsIgnoreCase("n") || message.equalsIgnoreCase("no")) {
                plugin.getShopRepo().cancelRejectShop(chatEvent.getPlayer().getUniqueId().toString());
                chatEvent.getPlayer().sendMessage(ChatColor.YELLOW + "Cancelled shop rejection");
            }
            else {
                plugin.getShopRepo().cancelRejectShop(chatEvent.getPlayer().getUniqueId().toString());
                chatEvent.getPlayer().sendMessage(ChatColor.GRAY + "Didn't get proper response, cancelling rejection");
            }
            return;
        }

        if(plugin.getShopRepo().isUserRemovingShop(chatEvent.getPlayer().getUniqueId().toString())) {
            chatEvent.setCancelled(true);
            String message = chatEvent.getMessage();
            if(message.equalsIgnoreCase("y") || message.equalsIgnoreCase("yes")) {
                plugin.getShopRepo().removeShop(chatEvent.getPlayer().getUniqueId().toString());
                chatEvent.getPlayer().sendMessage(ChatColor.GREEN + "Removed shop successfully");
            }
            else if(message.equalsIgnoreCase("n") || message.equalsIgnoreCase("no")) {
                plugin.getShopRepo().cancelRemoveShop(chatEvent.getPlayer().getUniqueId().toString());
                chatEvent.getPlayer().sendMessage(ChatColor.YELLOW + "Cancelled shop removal");
            }
            else {
                plugin.getShopRepo().cancelRemoveShop(chatEvent.getPlayer().getUniqueId().toString());
                chatEvent.getPlayer().sendMessage(ChatColor.GRAY + "Didn't get proper response, cancelling removal");
            }
            return;
        }
    }
}
