package me.PSK1103.GUIMarketplaceDirectory.utils;


import me.PSK1103.GUIMarketplaceDirectory.guimd.GUIMarketplaceDirectory;
import me.PSK1103.GUIMarketplaceDirectory.invholders.MarketplaceBookHolder;
import me.PSK1103.GUIMarketplaceDirectory.invholders.ShopInvHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.ChatPaginator;

import java.util.*;
import java.util.logging.Logger;

public class GUI {
    private final GUIMarketplaceDirectory plugin;
    private final HashMap<String,String> colors;
    private final Logger logger;

    public GUI(GUIMarketplaceDirectory plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger();
        colors = new HashMap<>();
        colors.put("name",plugin.getCustomConfig().getDefaultShopNameColor());
        colors.put("desc",plugin.getCustomConfig().getDefaultShopDescColor());
        colors.put("owner",plugin.getCustomConfig().getDefaultShopOwnerColor());
        colors.put("loc",plugin.getCustomConfig().getDefaultShopLocColor());
    }

    public void sendConfirmationMessage(Player player, String msg) {
        Component yes = Component.text(ChatColor.GOLD + "" + ChatColor.BOLD + "Y").clickEvent(net.kyori.adventure.text.event.ClickEvent.clickEvent(net.kyori.adventure.text.event.ClickEvent.Action.RUN_COMMAND,"Y"));

        Component no = Component.text(ChatColor.GOLD + "" + ChatColor.BOLD + "N").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,"N"));

        player.sendMessage(Component.text(msg + " (").color(NamedTextColor.YELLOW).append(yes).append(Component.text("/")).append(no).append(Component.text(")")).color(NamedTextColor.YELLOW));
    }

    public void openShopDirectory(Player player) {
        List<Map<String,String>> shops = plugin.getShopRepo().getShopDetails();
        Inventory shopDirectory = Bukkit.createInventory(new MarketplaceBookHolder(shops), Math.min(9*(shops.size()/9 + (shops.size()%9 == 0 ? 0 : 1)),54) + (shops.size() == 0 ? 9 : 0), Component.text("Marketplace Directory"));
        for(int i=0;i<(shops.size() > 54 ? 45 : shops.size());i++) {
            ItemStack shopItem;
            try {
                shopItem = new ItemStack(Material.getMaterial(shops.get(i).get("displayItem")));
            }
            catch (Exception e) {
                shopItem = new ItemStack(Material.WRITTEN_BOOK);
            }
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.displayName(Component.text(shops.get(i).get("name").contains("&") ? ChatColor.translateAlternateColorCodes('&',shops.get(i).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("name") + shops.get(i).get("name"))));
            List<String> l = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(shops.get(i).get("desc").contains("&") ? ChatColor.translateAlternateColorCodes('&',shops.get(i).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("desc") + shops.get(i).get("desc")),30)));
            List<Component> lore = new ArrayList<>();
            l.forEach(s -> lore.add(Component.text(s)));
            lore.add(Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("loc") + shops.get(i).get("loc"))));
            lore.add(0, Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("owner") + shops.get(i).get("owners"))));
            shopMeta.lore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            shopDirectory.setItem(i,shopItem);
        }

        if(shops.size() > 54) {
            ((MarketplaceBookHolder) shopDirectory.getHolder()).setPaged();
            ItemStack nextPage = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.displayName(Component.text("Next Page"));
            nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            nextPage.setItemMeta(nextPageMeta);
            shopDirectory.setItem(50,nextPage);
            ItemStack prevPage = new ItemStack(Material.BARRIER);
            ItemMeta prevPageMeta = prevPage.getItemMeta();
            prevPageMeta.displayName(Component.text("Previous Page"));
            prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            prevPage.setItemMeta(prevPageMeta);
            shopDirectory.setItem(48,prevPage);
            ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta pageNumMeta = pageNum.getItemMeta();
            pageNumMeta.displayName(Component.text("Page 1"));
            pageNum.setItemMeta(pageNumMeta);
            shopDirectory.setItem(45,pageNum);
        }

        player.openInventory(shopDirectory);

    }

    public void openShopInventory(Player player, String key,String name,int type) {

        List<Object> res = plugin.getShopRepo().getShopInv(key);
        List<ItemStack> inv = (List<ItemStack>) res.get(0);
        List<Integer> itemIds = (List<Integer>) res.get(1);

        Inventory shopInventory = Bukkit.createInventory(new ShopInvHolder(key,type,inv, itemIds),Math.min(9*(inv.size()/9),45) + 9, Component.text(name));
        for(int i=0;i<Math.min(inv.size(),45);i++) {
            shopInventory.setItem(i,inv.get(i));
        }
        if(inv.size() == 0) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            meta.displayName(Component.text(ChatColor.RED + "This shop is empty!"));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            empty.setItemMeta(meta);
            shopInventory.setItem(4,empty);
        }
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text(ChatColor.YELLOW + "Go Back"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        back.setItemMeta(meta);
        shopInventory.setItem(Math.min(9*(inv.size()/9),45) + 8,back);
        if(inv.size()>45) {
            ((ShopInvHolder) shopInventory.getHolder()).setPaged();
            ItemStack nextPage = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.displayName(Component.text("Next Page"));
            nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            nextPage.setItemMeta(nextPageMeta);
            shopInventory.setItem(50,nextPage);
            ItemStack prevPage = new ItemStack(Material.BARRIER);
            ItemMeta prevPageMeta = prevPage.getItemMeta();
            prevPageMeta.displayName(Component.text("Previous Page"));
            prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            prevPage.setItemMeta(prevPageMeta);
            shopInventory.setItem(48,prevPage);
            ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta pageNumMeta = pageNum.getItemMeta();
            pageNumMeta.displayName(Component.text("Page 1"));
            pageNum.setItemMeta(pageNumMeta);
            shopInventory.setItem(45,pageNum);
        }
        player.openInventory(shopInventory);
    }

    public void nextInvPage(Player player, int currPage) {
        Inventory nextPageInv = player.getOpenInventory().getTopInventory();
        ShopInvHolder holder = (ShopInvHolder) nextPageInv.getHolder();
        List<ItemStack> inv = holder.getInv();
        int type = holder.getType();
        nextPageInv.clear();
        for(int i=0;i<Math.min(inv.size(),(currPage+1)*45)-currPage*45;i++) {
            nextPageInv.setItem(i,inv.get(i+currPage*45));
        }

        ItemStack nextPage = inv.size() > (currPage+1)*45 ? new ItemStack(Material.LIME_STAINED_GLASS_PANE): new ItemStack(Material.BARRIER);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.displayName(Component.text("Next Page"));
        nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        nextPage.setItemMeta(nextPageMeta);
        nextPageInv.setItem(50,nextPage);
        ItemStack prevPage = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta prevPageMeta = prevPage.getItemMeta();
        prevPageMeta.displayName(Component.text("Previous Page"));
        prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        prevPage.setItemMeta(prevPageMeta);
        nextPageInv.setItem(48,prevPage);
        ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta pageNumMeta = pageNum.getItemMeta();
        pageNumMeta.displayName(Component.text("Page " + (currPage+1)));
        pageNum.setItemMeta(pageNumMeta);
        nextPageInv.setItem(45,pageNum);
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text(ChatColor.YELLOW + "Go Back"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        back.setItemMeta(meta);
        nextPageInv.setItem(53,back);
        player.updateInventory();
    }

    public void prevInvPage(Player player, int currPage) {
        currPage-=2;
        Inventory prevPageInv = player.getOpenInventory().getTopInventory();
        ShopInvHolder holder = (ShopInvHolder) prevPageInv.getHolder();
        List<ItemStack> inv = holder.getInv();
        int type = holder.getType();
        prevPageInv.clear();
        for(int i=0;i<Math.min(inv.size(),(currPage+1)*45)-currPage*45;i++) {
            prevPageInv.setItem(i,inv.get(i));
        }
        ItemStack nextPage = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.displayName(Component.text("Next Page"));
        nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        nextPage.setItemMeta(nextPageMeta);
        prevPageInv.setItem(50,nextPage);
        ItemStack prevPage = currPage > 0 ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE) : new ItemStack(Material.BARRIER);
        ItemMeta prevPageMeta = prevPage.getItemMeta();
        prevPageMeta.displayName(Component.text("Previous Page"));
        prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        prevPage.setItemMeta(prevPageMeta);
        prevPageInv.setItem(48,prevPage);
        ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta pageNumMeta = pageNum.getItemMeta();
        pageNumMeta.displayName(Component.text("Page " + (currPage+1)));
        pageNum.setItemMeta(pageNumMeta);
        prevPageInv.setItem(45,pageNum);
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text(ChatColor.YELLOW + "Go Back"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        back.setItemMeta(meta);
        prevPageInv.setItem(53,back);
        player.updateInventory();
    }

    public void nextPage(Player player, int currPage) {
        Inventory nextPageInv = player.getOpenInventory().getTopInventory();
        MarketplaceBookHolder holder = (MarketplaceBookHolder) nextPageInv.getHolder();
        List<Map<String,String>> shops = holder.getShops();
        int type = holder.getType();
        nextPageInv.clear();
        for(int i=0;i<Math.min(shops.size(),(currPage+1)*45)-currPage*45;i++) {
            ItemStack shopItem;
            try {
                shopItem = new ItemStack(Material.getMaterial(shops.get(i+currPage*45).get("displayItem")));
            }
            catch (Exception e) {
                shopItem = new ItemStack(Material.WRITTEN_BOOK);
            }
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.displayName(Component.text(shops.get(i+currPage*45).get("name").contains("&") ? ChatColor.translateAlternateColorCodes('&',shops.get(i+currPage*45).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("name") +  shops.get(i+currPage*45).get("name"))));
            List<String> l = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(shops.get(i+currPage*45).get("desc").contains("&") ? ChatColor.translateAlternateColorCodes('&',shops.get(i+currPage*45).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("desc") +  shops.get(i+currPage*45).get("desc")),30)));
            List<Component> lore = new ArrayList<>();
            l.forEach(s -> lore.add(Component.text(s)));
            lore.add(Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("loc") +  shops.get(i+currPage*45).get("loc"))));
            lore.add(0, Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("owner") +  shops.get(i+currPage*45).get("owners"))));
            if(type == 1) {
                lore.add(Component.text(ChatColor.GREEN + "Right click to approve"));
                lore.add(Component.text(ChatColor.RED + "Left click to reject"));
            }
            else if (type == 2) {
                lore.add(Component.text(ChatColor.RED + "Right click to delete"));
            }
            else if(type == 3) {
                lore.add(Component.text(ChatColor.AQUA + "Right click to recover"));
            }
            shopMeta.lore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            nextPageInv.setItem(i,shopItem);
        }
        ItemStack nextPage = shops.size() > (currPage+1)*45 ? new ItemStack(Material.LIME_STAINED_GLASS_PANE): new ItemStack(Material.BARRIER);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.displayName(Component.text("Next Page"));
        nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        nextPage.setItemMeta(nextPageMeta);
        nextPageInv.setItem(50,nextPage);
        ItemStack prevPage = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta prevPageMeta = prevPage.getItemMeta();
        prevPageMeta.displayName(Component.text("Previous Page"));
        prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        prevPage.setItemMeta(prevPageMeta);
        nextPageInv.setItem(48,prevPage);
        ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta pageNumMeta = pageNum.getItemMeta();
        pageNumMeta.displayName(Component.text("Page " + (currPage+1)));
        pageNum.setItemMeta(pageNumMeta);
        nextPageInv.setItem(45,pageNum);
        player.updateInventory();
    }

    public void prevPage(Player player, int currPage) {
        currPage-=2;
        Inventory prevPageInv = player.getOpenInventory().getTopInventory();
        MarketplaceBookHolder holder = (MarketplaceBookHolder) prevPageInv.getHolder();
        List<Map<String,String>> shops = holder.getShops();
        int type = holder.getType();
        prevPageInv.clear();
        for(int i=0;i < 45;i++) {
            ItemStack shopItem;
            try {
                shopItem = new ItemStack(Material.getMaterial(shops.get(i+currPage*45).get("displayItem")));
            }
            catch (Exception e) {
                shopItem = new ItemStack(Material.WRITTEN_BOOK);
            }
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.displayName(Component.text(shops.get(i+currPage*45).get("name").contains("&") ? ChatColor.translateAlternateColorCodes('&',shops.get(i+currPage*45).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("name") +  shops.get(i+currPage*45).get("name"))));
            List<String> l = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(shops.get(i+currPage*45).get("desc").contains("&") ? ChatColor.translateAlternateColorCodes('&',shops.get(i+currPage*45).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("desc") +  shops.get(i+currPage*45).get("desc")),30)));
            List<Component> lore = new ArrayList<>();
            l.forEach(s -> lore.add(Component.text(s)));
            lore.add(Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("loc") +  shops.get(i+currPage*45).get("loc"))));
            lore.add(0, Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("owner") +  shops.get(i+currPage*45).get("owners"))));
            if(type == 1) {
                lore.add(Component.text(ChatColor.GREEN + "Right click to approve"));
                lore.add(Component.text(ChatColor.RED + "Left click to reject"));
            }
            else if (type == 2) {
                lore.add(Component.text(ChatColor.RED + "Right click to delete"));
            }
            else if(type == 3) {
                lore.add(Component.text(ChatColor.AQUA + "Right click to recover"));
            }
            shopMeta.lore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            prevPageInv.setItem(i,shopItem);
        }
        ItemStack nextPage = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.displayName(Component.text("Next Page"));
        nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        nextPage.setItemMeta(nextPageMeta);
        prevPageInv.setItem(50,nextPage);
        ItemStack prevPage = currPage > 0 ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE) : new ItemStack(Material.BARRIER);
        ItemMeta prevPageMeta = prevPage.getItemMeta();
        prevPageMeta.displayName(Component.text("Previous Page"));
        prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        prevPage.setItemMeta(prevPageMeta);
        prevPageInv.setItem(48,prevPage);
        ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta pageNumMeta = pageNum.getItemMeta();
        pageNumMeta.displayName(Component.text("Page " + (currPage+1)));
        pageNum.setItemMeta(pageNumMeta);
        prevPageInv.setItem(45,pageNum);
        player.updateInventory();
    }

    public void openRefinedShopPageByName(Player player,String searchKey) {

        List<Map<String,String>> refinedShops = plugin.getShopRepo().getRefinedShopsByName(searchKey);
        if(refinedShops.size() == 0) {
            player.sendMessage(ChatColor.RED + "No shops with matching name found");
            return;
        }
        Inventory refinedShopDirectory = Bukkit.createInventory(new MarketplaceBookHolder(refinedShops),Math.min(9*(refinedShops.size()/9 + (refinedShops.size() % 9 == 0 ? 0 : 1)),54), Component.text("Search results"));
        for(int i=0;i< (Math.min(refinedShops.size(), 54));i++) {
            ItemStack shopItem;
            try {
                shopItem = new ItemStack(Material.getMaterial(refinedShops.get(i).get("displayItem")));
            }
            catch (Exception e) {
                shopItem = new ItemStack(Material.WRITTEN_BOOK);
            }
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.displayName(Component.text(refinedShops.get(i).get("name").contains("&") ? ChatColor.translateAlternateColorCodes('&',refinedShops.get(i).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("name") + refinedShops.get(i).get("name"))));
            List<String> l = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(refinedShops.get(i).get("desc").contains("&") ? ChatColor.translateAlternateColorCodes('&',refinedShops.get(i).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("desc") + refinedShops.get(i).get("desc")),30)));
            List<Component> lore = new ArrayList<>();
            l.forEach(s -> lore.add(Component.text(s)));
            lore.add(Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("loc") + refinedShops.get(i).get("loc"))));
            lore.add(0, Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("owner") + refinedShops.get(i).get("owners"))));
            shopMeta.lore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            refinedShopDirectory.setItem(i,shopItem);
        }
        player.openInventory(refinedShopDirectory);

    }

    public void openRefinedShopPageByPlayer(Player player,String searchKey) {

        List<Map<String,String>> refinedShops = plugin.getShopRepo().getRefinedShopsByPlayer(searchKey);
        if(refinedShops.size() == 0) {
            player.sendMessage(ChatColor.RED + "No shops with matching name found");
            return;
        }
        Inventory refinedShopDirectory = Bukkit.createInventory(new MarketplaceBookHolder(refinedShops),Math.min(9*(refinedShops.size()/9 + (refinedShops.size() % 9 == 0 ? 0 : 1)),54), Component.text("Search results"));
        for(int i=0;i< (Math.min(refinedShops.size(), 54));i++) {
            ItemStack shopItem;
            try {
                shopItem = new ItemStack(Material.getMaterial(refinedShops.get(i).get("displayItem")));
            }
            catch (Exception e) {
                shopItem = new ItemStack(Material.WRITTEN_BOOK);
            }
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.displayName(Component.text(refinedShops.get(i).get("name").contains("&") ? ChatColor.translateAlternateColorCodes('&',refinedShops.get(i).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("name") + refinedShops.get(i).get("name"))));
            List<String> l = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(refinedShops.get(i).get("desc").contains("&") ? ChatColor.translateAlternateColorCodes('&',refinedShops.get(i).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("desc") + refinedShops.get(i).get("desc")),30)));
            List<Component> lore = new ArrayList<>();
            l.forEach(s -> lore.add(Component.text(s)));
            lore.add(Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("loc") + refinedShops.get(i).get("loc"))));
            lore.add(0, Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("owner") + refinedShops.get(i).get("owners"))));
            shopMeta.lore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            refinedShopDirectory.setItem(i,shopItem);
        }
        player.openInventory(refinedShopDirectory);

    }

    public void openRefinedItemInventory(Player player, String searchKey) {
        Map<String,Object> searchResults = plugin.getShopRepo().findItem(searchKey);
        List<ItemStack> refinedItems = (List<ItemStack>) searchResults.get("items");
        List<Map<String,String>> shops = (List<Map<String,String>>) searchResults.get("shops");
        if(refinedItems.size() == 0) {
            player.sendMessage(ChatColor.RED + "No items with matching name found");
            return;
        }

        Inventory refinedItemInv = Bukkit.createInventory(new ShopInvHolder("",6,null, null).setShops(shops),Math.min(9*(refinedItems.size()/9 + ((refinedItems.size()%9) == 0 ? 0 : 1)),54), Component.text("Search results"));

        for(int i=0;i<Math.min(refinedItems.size(),54);i++) {
            refinedItemInv.setItem(i,refinedItems.get(i));
        }

        player.openInventory(refinedItemInv);
    }

    public void openShopDirectoryModerator(Player moderator,int type) {
        List<Map<String,String>> shops = type == 1 ? plugin.getShopRepo().getPendingShopDetails() : plugin.getShopRepo().getShopDetails();
        Inventory shopDirectory = Bukkit.createInventory(new MarketplaceBookHolder(shops,type), Math.min(9*(shops.size()/9 + (shops.size()%9 == 0 ? 0 : 1)),54) + (shops.size() == 0 ? 9 : 0), Component.text("Marketplace Directory"));
        for(int i=0;i<(shops.size() > 54 ? 45 : shops.size());i++) {
            ItemStack shopItem;
            try {
                shopItem = new ItemStack(Material.getMaterial(shops.get(i).get("displayItem")));
            }
            catch (Exception e) {
                shopItem = new ItemStack(Material.WRITTEN_BOOK);
            }
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(shops.get(i).get("name").contains("&") ? ChatColor.translateAlternateColorCodes('&',shops.get(i).get("name")) : ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("name") + shops.get(i).get("name")));
            List<String > l = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(shops.get(i).get("desc").contains("&") ? ChatColor.translateAlternateColorCodes('&',shops.get(i).get("desc")) : (colors.get("desc") + shops.get(i).get("desc")),30)));
            List<Component> lore = new ArrayList<>();
            l.forEach(s -> lore.add(Component.text(s)));
            lore.add(Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("loc") + shops.get(i).get("loc"))));
            lore.add(0, Component.text(ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR,colors.get("owner") + shops.get(i).get("owners"))));
            if(type == 1) {
                lore.add(Component.text(ChatColor.AQUA + "Shift click to view"));
                lore.add(Component.text(ChatColor.GREEN + "Right click to approve"));
                lore.add(Component.text(ChatColor.RED + "Left click to reject"));
            }
            else if (type == 2) {
                lore.add(Component.text(ChatColor.RED + "Right click to remove"));
            }
            else if(type == 3) {
                lore.add(Component.text(ChatColor.AQUA + "Right click to recover"));
            }
            else if(type == 4) {
                lore.add(Component.text(ChatColor.AQUA + "Right click to check activity"));
            }
            else if(type == 5) {
                lore.add(Component.text(ChatColor.AQUA + "Right click to set lookup radius"));
            }
            shopMeta.lore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            shopDirectory.setItem(i,shopItem);
        }

        if(shops.size() > 54) {
            ((MarketplaceBookHolder) shopDirectory.getHolder()).setPaged();
            ItemStack nextPage = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.displayName(Component.text("Next Page"));
            nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            nextPage.setItemMeta(nextPageMeta);
            shopDirectory.setItem(49,nextPage);
            ItemStack prevPage = new ItemStack(Material.BARRIER);
            ItemMeta prevPageMeta = prevPage.getItemMeta();
            prevPageMeta.displayName(Component.text("Previous Page"));
            prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            prevPage.setItemMeta(prevPageMeta);
            shopDirectory.setItem(48,prevPage);
            ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta pageNumMeta = pageNum.getItemMeta();
            pageNumMeta.displayName(Component.text("Page 1"));
            pageNum.setItemMeta(pageNumMeta);
            shopDirectory.setItem(45,pageNum);
        }

        if(shops.size() == 0) {
            logger.info("No pending shop");
        }

        moderator.openInventory(shopDirectory);
    }

    public void openShopEditMenu(Player player, String key) {
        String name = plugin.getShopRepo().getShopName(key);
        Inventory shopEditMenuInv = Bukkit.createInventory(new ShopInvHolder(key,4,null, null),9, Component.text(name));
        ItemStack addOwner = new ItemStack(Material.BEACON);
        ItemMeta addOwnerMeta = addOwner.getItemMeta();
        addOwnerMeta.displayName(Component.text(ChatColor.GOLD + "" + ChatColor.ITALIC + "Add owner"));
        addOwner.setItemMeta(addOwnerMeta);
        shopEditMenuInv.setItem(1,addOwner);

        ItemStack setDisplayItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta setDisplayItemMeta = setDisplayItem.getItemMeta();
        setDisplayItemMeta.displayName(Component.text(ChatColor.GOLD + "" + ChatColor.ITALIC + "Set display item"));
        setDisplayItem.setItemMeta(setDisplayItemMeta);
        shopEditMenuInv.setItem(4,setDisplayItem);

        ItemStack removeShop = new ItemStack(Material.FLINT_AND_STEEL);
        ItemMeta removeShopMeta = removeShop.getItemMeta();
        removeShopMeta.displayName(Component.text(ChatColor.RED + "" + ChatColor.ITALIC + "delete shop"));
        removeShop.setItemMeta(removeShopMeta);
        shopEditMenuInv.setItem(7,removeShop);

        player.openInventory(shopEditMenuInv);
    }

    public void openItemAddMenu(Player player, String key, List<ItemStack> matchingItems, ItemStack itemToAdd) {
        Inventory itemAddMenuInv = Bukkit.createInventory(new ShopInvHolder(key,itemToAdd.clone(),5),Math.min(54,9 + 9*(matchingItems.size()/9 + matchingItems.size()%9 == 0 ? 0 : 1)),Component.text("Adding Item..."));
        for(int i = 0;i<Math.min(matchingItems.size(),45);i++) {
            ItemStack iTA = matchingItems.get(i).clone();
            ItemMeta meta = iTA.getItemMeta();
            List<Component> lore = meta.lore();
            if(lore!=null)
                lore.add(Component.text(ChatColor.RED + "Right click to remove"));
            meta.lore(lore);
            iTA.setItemMeta(meta);
            itemAddMenuInv.setItem(i,iTA);
        }

        ItemStack addItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta addItemMeta = addItem.getItemMeta();
        addItemMeta.displayName(Component.text(ChatColor.GREEN + "" + ChatColor.ITALIC + "Add item"));
        addItem.setItemMeta(addItemMeta);
        itemAddMenuInv.setItem(itemAddMenuInv.getSize()-7,addItem);

        ItemStack removeAllItems = new ItemStack(Material.FLINT_AND_STEEL);
        ItemMeta removeAllItemsMeta = removeAllItems.getItemMeta();
        removeAllItemsMeta.displayName(Component.text(ChatColor.RED + "" + ChatColor.ITALIC + "Remove all items"));
        removeAllItems.setItemMeta(removeAllItemsMeta);
        itemAddMenuInv.setItem(itemAddMenuInv.getSize()-3,removeAllItems);

        player.openInventory(itemAddMenuInv);
    }

}
