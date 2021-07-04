package me.PSK1103.GUIMarketplaceDirectory.eventhandlers;

import io.netty.channel.*;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.PSK1103.GUIMarketplaceDirectory.guimd.GUIMarketplaceDirectory;
import me.PSK1103.GUIMarketplaceDirectory.invholders.MarketplaceBookHolder;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.protocol.game.PacketPlayOutOpenBook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ShopEvents implements Listener {

    class PacketHandler {

        public void removePLayer(Player player) {
            Channel channel = ((CraftPlayer) player).getHandle().b.a.k;
            channel.eventLoop().submit(() -> {
                channel.pipeline().remove(player.getName());
            });
        }

        public void injectPlayer (Player player) throws Exception {
            ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
                @Override
                public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                    if(packet instanceof PacketPlayOutOpenBook) {
                        ItemStack itemInHand = player.getInventory().getItemInMainHand();
                        if(itemInHand.getType() != Material.WRITTEN_BOOK) {
                            super.write(ctx, packet, promise);
                            return;
                        }
                        BookMeta bookMeta = (BookMeta) itemInHand.getItemMeta();
                        if(bookMeta.getTitle() != null && (bookMeta.getTitle().equalsIgnoreCase("[marketplace]") || bookMeta.getTitle().equalsIgnoreCase("[shop init]") || bookMeta.getTitle().equalsIgnoreCase("[init shop]"))) {
                            if (bookMeta.getTitle().equalsIgnoreCase("[Marketplace]")) {
                                Bukkit.getScheduler().runTask(plugin,()-> ShopEvents.this.plugin.gui.openShopDirectory(player));
                            }
                            else if (bookMeta.getTitle().equalsIgnoreCase("[shop init]") || bookMeta.getTitle().equalsIgnoreCase("[init shop]")) {
                                if (!ShopEvents.this.plugin.getCustomConfig().multiOwnerEnabled()) {
                                    return;                                }
                                if (ShopEvents.this.plugin.getShopRepo().isShopOwner(player.getUniqueId().toString(), bookMeta.getPage(bookMeta.getPageCount()))) {
                                    if (ShopEvents.this.plugin.getShopRepo().isAddingItem(player.getUniqueId().toString())) {
                                        player.sendMessage(ChatColor.RED + "Finish adding item first");
                                        return;                                    }
                                    if (ShopEvents.this.plugin.getShopRepo().getIsUserAddingOwner(player.getUniqueId().toString()) && !ShopEvents.this.plugin.getShopRepo().getIsAddingOwner(bookMeta.getPage(bookMeta.getPageCount()))) {
                                        player.sendMessage(ChatColor.RED + "Finish adding owner to other shop first");
                                        return;                                    }
                                    if (ShopEvents.this.plugin.getShopRepo().isShopUnderEditOrAdd(bookMeta.getPage(bookMeta.getPageCount()))) {
                                        player.sendMessage(ChatColor.RED + "This shop is currently under some other operation, try again later");
                                        return;
                                    }
                                    Bukkit.getScheduler().runTask(plugin,()->plugin.gui.openShopEditMenu(player,bookMeta.getPage(bookMeta.getPageCount())));
                                }

                            }
                            return;
                        }
                    }
                    super.write(ctx, packet, promise);
                }
            };

            ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().b.a.k.pipeline();
            if(pipeline.get(player.getName()) == null)
                pipeline.addBefore("packet_handler",player.getName(),channelDuplexHandler);
        }

    }

    private final GUIMarketplaceDirectory plugin;

    private final PacketHandler handler;

    public ShopEvents(GUIMarketplaceDirectory plugin) {
        this.plugin = plugin;
        handler = new PacketHandler();

        Bukkit.getOnlinePlayers().forEach(player -> {
            try {
                handler.injectPlayer(player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    public final void onShopAdd(PlayerEditBookEvent editBookEvent) {
        if(!editBookEvent.isSigning())
            return;

        BookMeta meta = editBookEvent.getNewBookMeta();
        assert meta.getTitle()!=null;
        if(meta.getTitle().toLowerCase().equals("[init shop]") || meta.getTitle().toLowerCase().equals("[shop init]")) {

            StringBuilder desc = new StringBuilder();
            for(int i = 0;i<meta.getPageCount();i++) {
                desc.append(meta.getPage(i+1));
            }
            String data = desc.toString().trim();
            Pattern shopInitPattern = Pattern.compile("\\[([^\\[]*)\\]\\s*\\[([^\\[]*)\\](?:\\s*\\[([^\\[]*)\\])?");
            Matcher shopInitMatcher = shopInitPattern.matcher(data);
            if(!shopInitMatcher.matches()) {
                editBookEvent.getPlayer().sendMessage(ChatColor.RED + "Incorrect shop initialisation, try again");
                editBookEvent.setCancelled(true);
                return;
            }

            if(plugin.getShopRepo().isAddingItem(editBookEvent.getPlayer().getUniqueId().toString())) {
                editBookEvent.getPlayer().sendMessage(ChatColor.RED + "Finish adding item first");
                editBookEvent.setCancelled(true);
                return;
            }

            String name = "", d = "", displayItem;

            displayItem = "WRITTEN_BOOK";

            name = shopInitMatcher.group(1);
            d = shopInitMatcher.group(2);

            System.out.println(shopInitMatcher.group(3));

            if(shopInitMatcher.group(3) != null)
                displayItem = shopInitMatcher.group(3);

            if(plugin.getCustomConfig().getShopDetailsLengthLimit() > 0 && (name.length() + d.length()) > plugin.getCustomConfig().getShopDetailsLengthLimit()) {
                editBookEvent.getPlayer().sendMessage(ChatColor.YELLOW + "Shop name + description length is too long (limit " +
                        plugin.getCustomConfig().getShopDetailsLengthLimit() + " characters)");
                editBookEvent.setCancelled(true);
                return;
            }

            String key = "" + System.currentTimeMillis() + editBookEvent.getPlayer().getUniqueId();
            String loc = editBookEvent.getPlayer().getLocation().getBlockX() + "," + editBookEvent.getPlayer().getLocation().getBlockZ();
            meta.addPage(key);
            meta.setDisplayName(name.contains("&") ? ChatColor.translateAlternateColorCodes('&',name) : (ChatColor.GOLD + name));
            editBookEvent.setNewBookMeta(meta);

            Player player = editBookEvent.getPlayer();

            if(!plugin.getCustomConfig().multiOwnerEnabled()) {
                plugin.getShopRepo().addShopAsOwner(name, d, player.getName(), player.getUniqueId().toString(), key, loc, displayItem);
            }
            else {
                plugin.getShopRepo().addShop(name, d, player.getName(), player.getUniqueId().toString(), key, loc,displayItem);

                if(plugin.getCustomConfig().directoryModerationEnabled() && plugin.getCustomConfig().customApprovalMessageEnabled()) {
                    editBookEvent.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('§', plugin.getCustomConfig().getCustomApprovalMessage()));
                }

                plugin.gui.sendConfirmationMessage(player,"Are you the owner of " + name + " ?");
                return;
            }

            if(plugin.getCustomConfig().directoryModerationEnabled() && plugin.getCustomConfig().customApprovalMessageEnabled()) {
                editBookEvent.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('§', plugin.getCustomConfig().getCustomApprovalMessage()));
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
    public final void onJoin(PlayerJoinEvent e){
        try {
            handler.injectPlayer(e.getPlayer());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler
    public final void onLeave(PlayerQuitEvent e) {
        handler.removePLayer(e.getPlayer());
    }

    @EventHandler
    public final void selectShop(InventoryClickEvent shopSelectEvent) {
        if(shopSelectEvent.getInventory().getHolder() instanceof MarketplaceBookHolder) {
            MarketplaceBookHolder holder = ((MarketplaceBookHolder) shopSelectEvent.getInventory().getHolder());
            shopSelectEvent.setCancelled(true);

            if(shopSelectEvent.getRawSlot() > shopSelectEvent.getInventory().getSize() || shopSelectEvent.getRawSlot() < 0) {
                shopSelectEvent.setCancelled(true);
                return;
            }

            int currPage = 1;

            if(shopSelectEvent.getRawSlot() > 44 && holder.isPaged()) {
                if(shopSelectEvent.getCurrentItem().getType() == Material.LIME_STAINED_GLASS_PANE) {
                    currPage = Integer.parseInt(((TextComponent) shopSelectEvent.getInventory().getItem(45).getItemMeta().displayName()).content().substring(5));
                    plugin.gui.nextPage(((Player) shopSelectEvent.getWhoClicked()),currPage);
                }
                if(shopSelectEvent.getCurrentItem().getType() == Material.ORANGE_STAINED_GLASS_PANE) {
                    currPage = Integer.parseInt(((TextComponent) shopSelectEvent.getInventory().getItem(45).getItemMeta().displayName()).content().substring(5));
                    plugin.gui.prevPage(((Player) shopSelectEvent.getWhoClicked()),currPage);
                }
                return;
            }

            if(shopSelectEvent.getInventory().getSize() == 54) {
                if(shopSelectEvent.getInventory().getItem(45).getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE && (shopSelectEvent.getInventory().getItem(46) == null || shopSelectEvent.getInventory().getItem(46).getType() == Material.AIR)) {
                    currPage = Integer.parseInt(((TextComponent) shopSelectEvent.getInventory().getItem(45).getItemMeta().displayName()).content().substring(5));
                }
            }
            if(holder.getType() == 0) {
                shopSelectEvent.getWhoClicked().closeInventory();
                plugin.gui.openShopInventory((Player) shopSelectEvent.getWhoClicked(), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("key"), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("name"),holder.getType());
            }
            else if (holder.getType() == 1) {
                if(shopSelectEvent.isShiftClick()){
                    shopSelectEvent.getWhoClicked().closeInventory();
                    plugin.gui.openShopInventory((Player) shopSelectEvent.getWhoClicked(), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("key"), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("name"),holder.getType());
                }
                else if(shopSelectEvent.isRightClick()) {
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
                    plugin.gui.sendConfirmationMessage((Player)shopSelectEvent.getWhoClicked(),"Do you wish to reject this shop?");
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
                    plugin.gui.sendConfirmationMessage((Player)shopSelectEvent.getWhoClicked(),"Do you wish to remove this shop?");
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

                if(shopSelectEvent.getCursor().getType() == Material.AIR && shopSelectEvent.isRightClick()) {
                    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                    BookMeta meta = (BookMeta) book.getItemMeta();
                    Map<String,String> shopDetails = holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage-1));
                    meta.setDisplayName(ChatColor.GOLD + shopDetails.get("name"));
                    meta.setTitle("[shop init]");
                    meta.setPages("[" + shopDetails.get("name") + "]\n[" + shopDetails.get("desc") + "]",shopDetails.get("key"));
                    meta.setAuthor(plugin.getShopRepo().getOwner(shopDetails.get("key")));
                    meta.setGeneration(BookMeta.Generation.COPY_OF_ORIGINAL);
                    book.setItemMeta(meta);
                    Player player = ((Player) shopSelectEvent.getWhoClicked());
                    if(player.getInventory().firstEmpty() != -1)
                        player.getInventory().setItem(player.getInventory().firstEmpty(),book);
                    else
                        shopSelectEvent.setCursor(book);
                }

                else {
                    shopSelectEvent.setCancelled(true);
                    shopSelectEvent.getWhoClicked().closeInventory();
                    plugin.gui.openShopInventory((Player) shopSelectEvent.getWhoClicked(), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("key"), holder.getShops().get(shopSelectEvent.getRawSlot() + 45*(currPage - 1)).get("name"),holder.getType());
                }

            }

        }
    }

    @EventHandler
    public final void ownerAddEvent(AsyncChatEvent chatEvent) {

        if(plugin.getShopRepo().getIsUserAddingOwner(chatEvent.getPlayer().getUniqueId().toString())) {
            chatEvent.setCancelled(true);

            String uuid = chatEvent.getPlayer().getUniqueId().toString();
            if (plugin.getShopRepo().getEditType(uuid) <= 0) {
                return;
            }

            int editType = plugin.getShopRepo().getEditType(uuid);

            if (editType == 2) {
                if (((TextComponent) chatEvent.message()).content().equalsIgnoreCase("Y") || ((TextComponent) chatEvent.message()).content().equalsIgnoreCase("yes")) {
                    plugin.getShopRepo().addOwner(uuid, chatEvent.getPlayer());
                    chatEvent.getPlayer().sendMessage(ChatColor.GOLD + "Shop initialised successfully!");
                } else if (((TextComponent) chatEvent.message()).content().equalsIgnoreCase("n") || ((TextComponent) chatEvent.message()).content().equalsIgnoreCase("no")) {
                    plugin.getShopRepo().initShopOwnerAddition(uuid);
                    chatEvent.getPlayer().sendMessage(ChatColor.YELLOW + "Enter owner's name (type nil to cancel)");
                } else {
                    plugin.getShopRepo().stopInitOwner(uuid);
                    chatEvent.getPlayer().sendMessage(ChatColor.GRAY + "Didn't get proper response");
                    plugin.gui.sendConfirmationMessage(chatEvent.getPlayer(),"Are you the owner of this shop?");
                }
            } else if (editType == 1) {

                if (((TextComponent) chatEvent.message()).content().equalsIgnoreCase("nil")) {
                    plugin.getShopRepo().stopInitOwner(uuid);
                    chatEvent.getPlayer().sendMessage(ChatColor.GRAY + "Cancelled adding another owner");
                    return;
                }
                String playerName = ((TextComponent) chatEvent.message()).content();

                List<OfflinePlayer> players;
                if(plugin.getCustomConfig().addingOfflinePlayerAllowed())
                    try {
                        players = Arrays.stream(plugin.getServer().getOfflinePlayers()).filter(offlinePlayer -> offlinePlayer.getName().toUpperCase(Locale.ROOT).startsWith(playerName)).collect(Collectors.toList());
                    } catch (NullPointerException e) {
                        chatEvent.getPlayer().sendMessage(ChatColor.RED + "Player data not found");
                        players = new ArrayList<>();
                    }
                else
                    players = plugin.getServer().matchPlayer(playerName).stream().map(player -> (OfflinePlayer) player).collect(Collectors.toList());


                if (players.size() == 0) {
                    chatEvent.getPlayer().sendMessage(ChatColor.RED + "No player found, try again");
                } else if (players.size() > 1) {
                    chatEvent.getPlayer().sendMessage(ChatColor.YELLOW + "Multiple players found, be more specific");
                } else {
                    plugin.getShopRepo().addOwner(uuid, players.get(0));
                    chatEvent.getPlayer().sendMessage(ChatColor.GOLD + players.get(0).getName() + " successfully added as owner");
                }
            } else if(editType == 3) {
                if (((TextComponent) chatEvent.message()).content().equalsIgnoreCase("nil")) {
                    plugin.getShopRepo().stopInitOwner(uuid);
                    chatEvent.getPlayer().sendMessage(ChatColor.GRAY + "Cancelled setting display item");
                    return;
                }

                String materialName = ((TextComponent) chatEvent.message()).content().trim().replace(' ','_').toUpperCase(Locale.ROOT);
                Material trial = Material.getMaterial(materialName);
                if(trial != null) {
                    chatEvent.getPlayer().sendMessage(ChatColor.GREEN + "Setting shop display item to " + ChatColor.GOLD + trial.getKey().getKey());
                    plugin.getShopRepo().setDisplayItem(uuid,materialName);
                }
                else {
                    chatEvent.getPlayer().sendMessage(ChatColor.RED + "Item name doesn't match to a proper material name. Try again");
                    chatEvent.getPlayer().sendMessage(ChatColor.YELLOW + "Enter display item name (material name only, nil to cancel)");
                    return;
                }
            }
            return;
        }
        if(plugin.getShopRepo().isUserRejectingShop(chatEvent.getPlayer().getUniqueId().toString())) {
            chatEvent.setCancelled(true);
            String message = ((TextComponent) chatEvent.message()).content();
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
            String message = ((TextComponent) chatEvent.message()).content();
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
        }
    }

}
