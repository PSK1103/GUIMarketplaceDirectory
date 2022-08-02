package me.PSK1103.GUIMarketplaceDirectory.eventhandlers;

import me.PSK1103.GUIMarketplaceDirectory.GUIMarketplaceDirectory;
import me.PSK1103.GUIMarketplaceDirectory.invholders.ShopInvHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class ItemEvents implements Listener {
    final GUIMarketplaceDirectory plugin;

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

            if(item == null || item.getType().isAir()) {
                return;
            }
            ItemMeta tempMeta = item.getItemMeta();
            Map<Enchantment,Integer> enchantmentMap = tempMeta.getEnchants();
            String name = item.getType().getKey().getKey().toUpperCase();
            Player player = ((Player) itemEvent.getWhoClicked());
            Inventory inventory = itemEvent.getInventory();
            if(player.getInventory().firstEmpty() !=-1) {
                player.getInventory().setItem(player.getInventory().firstEmpty(),itemEvent.getCursor());
                itemEvent.setCursor(new ItemStack(Material.AIR));
                player.updateInventory();
            }
            else if(inventory.firstEmpty() != -1) {
                inventory.setItem(inventory.firstEmpty(), itemEvent.getCursor());
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

            List<ItemStack> matchingItems = plugin.getShopRepo().getMatchingItems(bookMeta.getPage(bookMeta.getPageCount()),item.getType().getKey().getKey().toUpperCase());

            if(matchingItems == null) {
                player.sendMessage(ChatColor.RED + "Shop doesn't exist");
                return;
            }

            if(matchingItems.size() == 0) {
                player.sendMessage(ChatColor.GREEN + "Set quantity (in format shulker:stack:num)");
                int res = plugin.getShopRepo().initItemAddition(player.getUniqueId().toString(), bookMeta.getPage(bookMeta.getPageCount()), name, item);
                if (res == -1) {
                    player.sendMessage(ChatColor.RED + "shop not found!");
                } else if (res == 0) {
                    player.sendMessage(ChatColor.GRAY + "Cancelling addition of previous item");
                } else if (res == 2) {
                    player.sendMessage(new String[]{ChatColor.YELLOW + "The head item you're trying to add has no proper skullOwner nbt tags", ChatColor.YELLOW + "place it on the ground and pick it up and then try to add", ChatColor.YELLOW + "Continue adding if this isn't a mistake"});
                }
            }
            else {
                plugin.gui.openItemAddMenu(player,bookMeta.getPage(bookMeta.getPageCount()),matchingItems,item);
            }
        }
    }

    @EventHandler
    public final void addItemData(AsyncPlayerChatEvent addItemDetails) {
        if(addItemDetails.getMessage().matches("\\d+:\\d+:\\d+") && plugin.getShopRepo().isAddingItem(addItemDetails.getPlayer().getUniqueId().toString())) {
            plugin.getShopRepo().setQty(addItemDetails.getMessage(),addItemDetails.getPlayer().getUniqueId().toString());
            addItemDetails.getPlayer().sendMessage(ChatColor.GREEN + "Enter price (in diamonds)");
            addItemDetails.setCancelled(true);
            return;
        }
        if(addItemDetails.getMessage().matches("-?\\d+") && plugin.getShopRepo().isAddingItem(addItemDetails.getPlayer().getUniqueId().toString())) {
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
            if(itemCheckEvent.getCurrentItem() == null || itemCheckEvent.getCurrentItem().getType() == Material.AIR)
                return;

            ShopInvHolder holder = (ShopInvHolder)itemCheckEvent.getInventory().getHolder();

            if(holder.getKey().length() == 0 && holder.getType() < 6) {
                return;
            }

            if(itemCheckEvent.getRawSlot() > itemCheckEvent.getInventory().getSize()) {
                return;
            }


            int type = holder.getType();
            Player player = ((Player) itemCheckEvent.getWhoClicked());
            if(type < 4) {

                int currPage = 1;

                if(itemCheckEvent.getRawSlot() >= (itemCheckEvent.getInventory().getSize() - 9) && itemCheckEvent.getInventory().getSize() == 54) {
                    if(holder.isPaged()) {
                        if (itemCheckEvent.getCurrentItem().getType() == Material.LIME_STAINED_GLASS_PANE) {
                            currPage = Integer.parseInt(itemCheckEvent.getInventory().getItem(45).getItemMeta().getDisplayName().substring(5));
                            plugin.gui.nextInvPage(((Player) itemCheckEvent.getWhoClicked()), currPage);
                        }
                        if (itemCheckEvent.getCurrentItem().getType() == Material.ORANGE_STAINED_GLASS_PANE) {
                            currPage = Integer.parseInt(itemCheckEvent.getInventory().getItem(45).getItemMeta().getDisplayName().substring(5));
                            plugin.gui.prevInvPage(((Player) itemCheckEvent.getWhoClicked()), currPage);
                        }
                    }
                    if (itemCheckEvent.getCurrentItem() != null && itemCheckEvent.getRawSlot() == itemCheckEvent.getInventory().getSize() - 1) {
                        player.closeInventory();
                        if (type == 0) {
                            plugin.gui.openShopDirectory(player);
                        } else
                            plugin.gui.openShopDirectoryModerator(player, type);

                        return;
                    }
                    return;
                }

                if (itemCheckEvent.isRightClick() && itemCheckEvent.getCurrentItem() != null && itemCheckEvent.getCurrentItem().getType() != Material.AIR && itemCheckEvent.getCurrentItem().getType() != Material.BARRIER && itemCheckEvent.getRawSlot()<45) {
                    if (itemCheckEvent.getRawSlot() == itemCheckEvent.getInventory().getSize() - 1) {
                        player.closeInventory();
                        if (type == 0) {
                            plugin.gui.openShopDirectory(player);
                        } else
                            plugin.gui.openShopDirectoryModerator(player, type);
                    }
                    try {
                        currPage = Integer.parseInt(itemCheckEvent.getInventory().getItem(45).getItemMeta().getDisplayName().substring(5));
                    }
                    catch (Exception ignored) {}
                    plugin.getShopRepo().findBetterAlternative(player, holder.getKey(), holder.getItemId((currPage-1)*45 + itemCheckEvent.getRawSlot()));
                }
                if (itemCheckEvent.getRawSlot() == itemCheckEvent.getInventory().getSize() - 1) {
                    player.closeInventory();
                    if (type == 0) {
                        plugin.gui.openShopDirectory(player);
                    } else
                        plugin.gui.openShopDirectoryModerator(player, type);
                }
            }
            else {
                if(type == 4) {
                    if(itemCheckEvent.getRawSlot() == 1) {
                        player.closeInventory();
                        int res = plugin.getShopRepo().startAddingOwner(player.getUniqueId().toString(), holder.getKey());
                        if(res == -1)
                            player.sendMessage(ChatColor.RED + "This shop doesn't exist");
                        else
                            player.sendMessage(new String[]{ChatColor.GRAY + "Adding another owner...", ChatColor.YELLOW + "Enter player name (nil to cancel)"});
                    }
                    else if (itemCheckEvent.getRawSlot() == 4) {
                        player.closeInventory();
                        int res = plugin.getShopRepo().startSettingDisplayItem(player.getUniqueId().toString(), holder.getKey());
                        if(res == -1)
                            player.sendMessage(ChatColor.RED + "This shop doesn't exist");
                        else
                            player.sendMessage(ChatColor.YELLOW + "Enter display item name (material name only, nil to cancel)");
                    }
                    else if(itemCheckEvent.getRawSlot() == 7) {
                        player.closeInventory();
                        int res = plugin.getShopRepo().startRemovingShop(player.getUniqueId().toString(),holder.getKey());
                        if(res == -1)
                            player.sendMessage(ChatColor.RED + "This shop doesn't exist");
                        else
                            plugin.gui.sendConfirmationMessage(player,"Do you wish to remove this shop?");
                    }
                }
                else if(type == 5) {
                    if(itemCheckEvent.getRawSlot() == itemCheckEvent.getInventory().getSize()-7) {
                        player.closeInventory();
                        ItemStack item = holder.getItem();
                        player.sendMessage(ChatColor.GREEN + "Set quantity (in format shulker:stack:num)");
                        int res = plugin.getShopRepo().initItemAddition(player.getUniqueId().toString(), holder.getKey(), item.getType().getKey().getKey().toUpperCase(), item);
                        if (res == -1) {
                            player.sendMessage(ChatColor.RED + "shop not found!");
                        } else if (res == 0) {
                            player.sendMessage(ChatColor.GRAY + "Cancelling addition of previous item");
                        } else if (res == 2) {
                            player.sendMessage(new String[]{ChatColor.YELLOW + "The head item you're trying to add has no proper skullOwner nbt tags", ChatColor.YELLOW + "place it on the ground and pick it up and then try to add", ChatColor.YELLOW + "Continue adding if this isn't a mistake"});
                        }
                    }

                    else if(itemCheckEvent.getRawSlot() == itemCheckEvent.getInventory().getSize()-3) {
                        player.closeInventory();
                        plugin.getShopRepo().removeMatchingItems(holder.getKey(),holder.getItem().getType().getKey().getKey().toUpperCase());
                        player.closeInventory();
                        player.sendMessage(ChatColor.YELLOW + "All matching items removed");
                    }

                    else if(itemCheckEvent.getRawSlot()<itemCheckEvent.getInventory().getSize()-9 && itemCheckEvent.getCurrentItem()!=null && itemCheckEvent.isRightClick() && itemCheckEvent.getCurrentItem().getType()!= Material.AIR) {
                        plugin.getShopRepo().removeItem(holder.getKey(), itemCheckEvent.getCurrentItem());
                        List<ItemStack> matchingItems = plugin.getShopRepo().getMatchingItems(holder.getKey(), itemCheckEvent.getCurrentItem().getType().getKey().getKey().toUpperCase());
                        if(matchingItems.size() == 0)
                            player.closeInventory();

                        plugin.gui.openItemAddMenu(player, holder.getKey(), matchingItems, itemCheckEvent.getCurrentItem());
                    }
                } else if(type == 6 && itemCheckEvent.isRightClick() && itemCheckEvent.getRawSlot() < Math.min(itemCheckEvent.getInventory().getSize(),holder.getShops().size())) {
                    player.closeInventory();
                    plugin.gui.openShopInventory(player,holder.getShops().get(itemCheckEvent.getRawSlot()).get("id"),holder.getShops().get(itemCheckEvent.getRawSlot()).get("name"),0);
                }
            }
        }
    }

}
