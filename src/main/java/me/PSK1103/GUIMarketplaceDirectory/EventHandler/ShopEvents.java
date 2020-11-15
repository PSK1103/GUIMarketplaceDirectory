package me.PSK1103.GUIMarketplaceDirectory.EventHandler;

import me.PSK1103.GUIMarketplaceDirectory.GUIMarketplaceDirectory;
import me.PSK1103.GUIMarketplaceDirectory.InvHolders.MarketplaceBookHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.material.FlowerPot;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class ShopEvents implements Listener {

    private GUIMarketplaceDirectory plugin;

    private static final EnumSet<Material> INTERACTABLES = EnumSet.noneOf(Material.class);

    static {
        for (Material m : Material.values()) {
            if(m.name().contains("BUTTON"))
                INTERACTABLES.add(m);

            else if(m.name().contains("DOOR"))
                INTERACTABLES.add(m);

            else if(m.name().contains("BED"))
                INTERACTABLES.add(m);

            else if(m.name().contains("FENCE_GATE"))
                INTERACTABLES.add(m);

            else if(m.name().contains("ITEM_FRAME"))
                INTERACTABLES.add(m);

            else if(m.name().contains("CHEST"))
                INTERACTABLES.add(m);

            else if(m.name().contains("HOPPER"))
                INTERACTABLES.add(m);

            else if(m.name().contains("BARREL"))
                INTERACTABLES.add(m);

            else if(m.name().contains("FURNACE"))
                INTERACTABLES.add(m);

            else if(m.name().contains("DISPENSER"))
                INTERACTABLES.add(m);

            else if(m.name().contains("DROPPER"))
                INTERACTABLES.add(m);

            else if(m.name().contains("SHULKER"))
                INTERACTABLES.add(m);

            else if(m.name().contains("SMOKER"))
                INTERACTABLES.add(m);

            else if(m.name().contains("LOOM"))
                INTERACTABLES.add(m);

            else if(m.name().contains("CARTOGRAPHY"))
                INTERACTABLES.add(m);

            else if(m.name().contains("GRINDSTONE"))
                INTERACTABLES.add(m);

            else if(m.name().contains("STONECUTTER"))
                INTERACTABLES.add(m);

            else if(m.name().contains("ANVIL"))
                INTERACTABLES.add(m);

            else if(m.name().contains("ENCHANTING"))
                INTERACTABLES.add(m);

            else if(m.name().contains("BREWING"))
                INTERACTABLES.add(m);

            else if(m.name().contains("LECTERN"))
                INTERACTABLES.add(m);

            else if(m.name().contains("COMPARATOR"))
                INTERACTABLES.add(m);

            else if(m.name().contains("REPEATER"))
                INTERACTABLES.add(m);

            else if(m.name().contains("NOTE_BLOCK"))
                INTERACTABLES.add(m);

            else if(m.name().contains("TRAPDOOR"))
                INTERACTABLES.add(m);

            else if(m.name().contains("COMMAND"))
                INTERACTABLES.add(m);

            else if(m.name().contains("LEVER"))
                INTERACTABLES.add(m);

            else if(m.name().contains("DRAGON_EGG"))
                INTERACTABLES.add(m);

            else if(m.name().contains("BEACON"))
                INTERACTABLES.add(m);

            else if(m.name().contains("CAKE"))
                INTERACTABLES.add(m);

        }
    }

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
            if(!data.trim().matches("\\s\\Q[\\E.*\\Q]\\E\\s*\\Q[\\E.*\\Q]\\E\\s")) {
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
        if(shopDirectoryOpenEvent.hasItem() && shopDirectoryOpenEvent.getItem().getType() == Material.WRITTEN_BOOK) {
            BookMeta bookMeta = (BookMeta) shopDirectoryOpenEvent.getItem().getItemMeta();
            if(bookMeta.getTitle().equalsIgnoreCase("[Marketplace]") || bookMeta.getTitle().equalsIgnoreCase("[shop init]") || bookMeta.getTitle().equalsIgnoreCase("[init shop]")) {
                if (shopDirectoryOpenEvent.getAction() == Action.RIGHT_CLICK_AIR || (shopDirectoryOpenEvent.getAction() == Action.RIGHT_CLICK_BLOCK && shopDirectoryOpenEvent.getClickedBlock() != null && !isInteractableWithBook(shopDirectoryOpenEvent.getClickedBlock()))) {
                    shopDirectoryOpenEvent.setCancelled(true);
                    if (bookMeta.getTitle().equalsIgnoreCase("[Marketplace]")) {
                        plugin.gui.openShopDirectory(shopDirectoryOpenEvent.getPlayer());
                    }
                    else if (bookMeta.getTitle().equalsIgnoreCase("[shop init]") || bookMeta.getTitle().equalsIgnoreCase("[init shop]")) {
                        if (!plugin.getCustomConfig().getBoolean("multi-owner", false)) {
                            return;
                        }
                        if (plugin.getShopRepo().isShopOwner(shopDirectoryOpenEvent.getPlayer().getUniqueId().toString(), bookMeta.getPage(bookMeta.getPageCount()))) {
                            if (plugin.getShopRepo().isAddingItem(shopDirectoryOpenEvent.getPlayer().getUniqueId().toString())) {
                                shopDirectoryOpenEvent.getPlayer().sendMessage(ChatColor.RED + "Finish adding item first");
                                return;
                            }
                            if (plugin.getShopRepo().getIsUserAddingOwner(shopDirectoryOpenEvent.getPlayer().getUniqueId().toString()) && !plugin.getShopRepo().getIsAddingOwner(bookMeta.getPage(bookMeta.getPageCount()))) {
                                shopDirectoryOpenEvent.getPlayer().sendMessage(ChatColor.RED + "Finish adding owner to other shop first");
                                return;
                            }
                            if (plugin.getShopRepo().isShopUnderEditOrAdd(bookMeta.getPage(bookMeta.getPageCount()))) {
                                shopDirectoryOpenEvent.getPlayer().sendMessage(ChatColor.RED + "This shop is currently under some other operation, try again later");
                                return;
                            }
                            plugin.getShopRepo().startAddingOwner(shopDirectoryOpenEvent.getPlayer().getUniqueId().toString(), bookMeta.getPage(bookMeta.getPageCount()));
                            shopDirectoryOpenEvent.getPlayer().sendMessage(new String[]{ChatColor.GRAY + "Adding another owner...", ChatColor.YELLOW + "Enter player name (nil to cancel)"});
                        }

                    }
                }
            }
        }
    }

    @EventHandler
    public final void selectShop(InventoryClickEvent shopSelectEvent) {
        if(shopSelectEvent.getInventory().getHolder() instanceof MarketplaceBookHolder) {
            MarketplaceBookHolder holder = ((MarketplaceBookHolder) shopSelectEvent.getInventory().getHolder());
            shopSelectEvent.setCancelled(true);

            if(shopSelectEvent.getRawSlot() > shopSelectEvent.getInventory().getSize() && holder.getType() == 3 && shopSelectEvent.getCurrentItem().getType() == Material.AIR && shopSelectEvent.getCursor().getType() == Material.WRITTEN_BOOK) {
                return;
            }

            if(shopSelectEvent.getRawSlot() > shopSelectEvent.getInventory().getSize()) {
                shopSelectEvent.setCancelled(true);
                return;
            }

            if(shopSelectEvent.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
                int currPage = 1;
                if(shopSelectEvent.getInventory().getSize() == 54) {
                    if(shopSelectEvent.getInventory().getItem(45).getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
                        currPage = Integer.parseInt(shopSelectEvent.getInventory().getItem(45).getItemMeta().getDisplayName().substring(5));
                    }
                }
                if(holder.getType() == 0) {
                    shopSelectEvent.getWhoClicked().closeInventory();
                    plugin.gui.openShopInventory((Player) shopSelectEvent.getWhoClicked(), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("key"), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("name"),holder.getType());
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
                    else if(shopSelectEvent.isLeftClick()) {
                        if(plugin.getShopRepo().isUserRejectingShop(shopSelectEvent.getWhoClicked().getUniqueId().toString())) {
                            shopSelectEvent.getWhoClicked().sendMessage(ChatColor.RED + "Confirm rejection of previous shop first!");
                            return;
                        }
                        plugin.getShopRepo().addShopToRejectQueue(shopSelectEvent.getWhoClicked().getUniqueId().toString(),holder.getShops().get(shopSelectEvent.getRawSlot() + 45 * (currPage-1)).get("key"));
                        shopSelectEvent.getWhoClicked().sendMessage(ChatColor.YELLOW + "Do you wish to reject this shop? (Y/N)");
                        shopSelectEvent.getWhoClicked().closeInventory();
                        return;
                    }
                    else if(shopSelectEvent.isShiftClick()){
                        shopSelectEvent.getWhoClicked().closeInventory();
                        plugin.gui.openShopInventory((Player) shopSelectEvent.getWhoClicked(), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("key"), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("name"),holder.getType());
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
                    else {
                        shopSelectEvent.getWhoClicked().closeInventory();
                        plugin.gui.openShopInventory((Player) shopSelectEvent.getWhoClicked(), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("key"), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("name"),holder.getType());
                    }
                    return;
                }
                else if(holder.getType() == 3) {

                    if(shopSelectEvent.isRightClick() && shopSelectEvent.getRawSlot() < Math.min(holder.getShops().size(),54) && shopSelectEvent.getCursor() != null && shopSelectEvent.getCursor().getType() != Material.AIR) {
                        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta meta = (BookMeta) book.getItemMeta();
                        Map<String,String> shopDetails = holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage-1));
                        meta.setTitle(ChatColor.GOLD + shopDetails.get("name"));
                        meta.setPages("[" + shopDetails.get("name") + "]\n[" + shopDetails.get("desc") + "]",shopDetails.get("key"));
                        meta.setAuthor(plugin.getShopRepo().getOwner(shopDetails.get("key")));
                        meta.setGeneration(BookMeta.Generation.COPY_OF_ORIGINAL);
                        book.setItemMeta(meta);
                        shopSelectEvent.setCursor(book);
                    }



                    else {
                        shopSelectEvent.setCancelled(true);
                        shopSelectEvent.getWhoClicked().closeInventory();
                        plugin.gui.openShopInventory((Player) shopSelectEvent.getWhoClicked(), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("key"), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("name"),holder.getType());
                    }

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

    private boolean isInteractableWithBook(Block b) {
        Material m = b.getType();
        return INTERACTABLES.contains(m);
    }
}
