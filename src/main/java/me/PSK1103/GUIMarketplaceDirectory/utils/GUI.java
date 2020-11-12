package me.PSK1103.GUIMarketplaceDirectory.utils;


import me.PSK1103.GUIMarketplaceDirectory.GUIMarketplaceDirectory;
import me.PSK1103.GUIMarketplaceDirectory.InvHolders.MarketplaceBookHolder;
import me.PSK1103.GUIMarketplaceDirectory.InvHolders.ShopInvHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.ChatPaginator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GUI {
    private GUIMarketplaceDirectory plugin;

    public GUI(GUIMarketplaceDirectory plugin) {
        this.plugin = plugin;
    }

    public void openShopDirectory(Player player) {
        List<Map<String,String>> shops = plugin.getShopRepo().getShopDetails();
        Inventory shopDirectory = Bukkit.createInventory(new MarketplaceBookHolder(shops), Math.min(9*(shops.size()/9 + (shops.size()%9 == 0 ? 0 : 1)),54) + (shops.size() == 0 ? 9 : 0),"Marketplace Directory");
        for(int i=0;i<(shops.size() > 54 ? 45 : shops.size());i++) {
            ItemStack shopItem = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + shops.get(i).get("name"));
            List<String> lore = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(ChatColor.translateAlternateColorCodes('&',"&1" + shops.get(i).get("desc")),30)));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e" + shops.get(i).get("loc")));
            lore.add(0,ChatColor.LIGHT_PURPLE + shops.get(i).get("owners"));
            shopMeta.setLore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            shopDirectory.setItem(i,shopItem);
        }

        if(shops.size() > 54) {
            ItemStack nextPage = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName("Next Page");
            nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            nextPage.setItemMeta(nextPageMeta);
            shopDirectory.setItem(49,nextPage);
            ItemStack prevPage = new ItemStack(Material.BARRIER);
            ItemMeta prevPageMeta = prevPage.getItemMeta();
            prevPageMeta.setDisplayName("Previous Page");
            prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            prevPage.setItemMeta(prevPageMeta);
            shopDirectory.setItem(48,prevPage);
            ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta pageNumMeta = pageNum.getItemMeta();
            pageNumMeta.setDisplayName("Page 1");
            pageNum.setItemMeta(pageNumMeta);
            shopDirectory.setItem(45,pageNum);
        }

        player.openInventory(shopDirectory);

    }

    public void openShopInventory(Player player, int slot,int page, List<Map<String,String>> shops) {

        List<ItemStack> inv = plugin.getShopRepo().getShopInv(shops.get(slot + 45*page - 45).get("key"));
        Inventory shopInventory = Bukkit.createInventory(new ShopInvHolder(shops.get(slot + 45*page - 45).get("key")),Math.min(9*((inv.size()/9) + (inv.size()%9 == 0 ? 0 : 1) + (inv.size()==0?1:0)),54),shops.get(slot).get("name"));
        for(int i=0;i<Math.min(inv.size(),54);i++) {
            shopInventory.setItem(i,inv.get(i));
        }
        if(inv.size() == 0) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "This shop is empty!");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            empty.setItemMeta(meta);
            shopInventory.setItem(4,empty);
        }
        player.openInventory(shopInventory);
    }

    public void nextPage(Player player, int currPage) {
        Inventory nextPageInv = player.getOpenInventory().getTopInventory();
        MarketplaceBookHolder holder = (MarketplaceBookHolder) nextPageInv.getHolder();
        List<Map<String,String>> shops = holder.getShops();
        int type = holder.getType();
        nextPageInv.clear();
        for(int i=0;i<Math.min(shops.size(),(currPage+1)*45)-currPage*45;i++) {
            ItemStack shopItem = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + shops.get(i+currPage*45).get("name"));
            List<String> lore = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(ChatColor.translateAlternateColorCodes('&',"&1" + shops.get(i+currPage*45).get("desc")),30)));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e" + shops.get(i+currPage*45).get("loc")));
            lore.add(0,ChatColor.LIGHT_PURPLE + shops.get(i+currPage*45).get("owners"));
            if(type == 1) {
                lore.add(ChatColor.GREEN + "Right click to approve");
                lore.add(ChatColor.RED + "Left click to reject");
            }
            else if (type == 2) {
                lore.add(ChatColor.RED + "Right click to delete");
            }
            shopMeta.setLore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            nextPageInv.setItem(i,shopItem);
        }
        ItemStack nextPage = shops.size() > (currPage+1)*45 ? new ItemStack(Material.LIME_STAINED_GLASS_PANE): new ItemStack(Material.BARRIER);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.setDisplayName("Next Page");
        nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        nextPage.setItemMeta(nextPageMeta);
        nextPageInv.setItem(49,nextPage);
        ItemStack prevPage = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta prevPageMeta = prevPage.getItemMeta();
        prevPageMeta.setDisplayName("Previous Page");
        prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        prevPage.setItemMeta(prevPageMeta);
        nextPageInv.setItem(48,prevPage);
        ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta pageNumMeta = pageNum.getItemMeta();
        pageNumMeta.setDisplayName("Page " + (currPage+1));
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
            ItemStack shopItem = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + shops.get(i+currPage*45).get("name"));
            List<String> lore = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(ChatColor.translateAlternateColorCodes('&',"&1" + shops.get(i+currPage*45).get("desc")),30)));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e" + shops.get(i+currPage*45).get("loc")));
            lore.add(0,ChatColor.LIGHT_PURPLE + shops.get(i+currPage*45).get("owners"));
            if(type == 1) {
                lore.add(ChatColor.GREEN + "Right click to approve");
                lore.add(ChatColor.RED + "Left click to reject");
            }
            else if (type == 2) {
                lore.add(ChatColor.RED + "Right click to delete");
            }
            shopMeta.setLore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            prevPageInv.setItem(i,shopItem);
        }
        ItemStack nextPage = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.setDisplayName("Next Page");
        nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        nextPage.setItemMeta(nextPageMeta);
        prevPageInv.setItem(49,nextPage);
        ItemStack prevPage = currPage > 0 ? new ItemStack(Material.ORANGE_STAINED_GLASS_PANE) : new ItemStack(Material.BARRIER);
        ItemMeta prevPageMeta = prevPage.getItemMeta();
        prevPageMeta.setDisplayName("Previous Page");
        prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        prevPage.setItemMeta(prevPageMeta);
        prevPageInv.setItem(48,prevPage);
        ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta pageNumMeta = pageNum.getItemMeta();
        pageNumMeta.setDisplayName("Page " + (currPage+1));
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
        Inventory refinedShopDirectory = Bukkit.createInventory(new MarketplaceBookHolder(refinedShops),Math.min(9*(refinedShops.size()/9 + (refinedShops.size() % 9 == 0 ? 0 : 1)),54),"Search results");
        for(int i=0;i< (Math.min(refinedShops.size(), 54));i++) {
            ItemStack shopItem = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + refinedShops.get(i).get("name"));
            List<String> lore = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(ChatColor.translateAlternateColorCodes('&',"&1" + refinedShops.get(i).get("desc")),30)));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e" + refinedShops.get(i).get("loc")));
            lore.add(0,ChatColor.LIGHT_PURPLE + refinedShops.get(i).get("owners"));
            shopMeta.setLore(lore);
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
        Inventory refinedShopDirectory = Bukkit.createInventory(new MarketplaceBookHolder(refinedShops),Math.min(9*(refinedShops.size()/9 + (refinedShops.size() % 9 == 0 ? 0 : 1)),54),"Search results");
        for(int i=0;i< (Math.min(refinedShops.size(), 54));i++) {
            ItemStack shopItem = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + refinedShops.get(i).get("name"));
            List<String> lore = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(ChatColor.translateAlternateColorCodes('&',"&1" + refinedShops.get(i).get("desc")),30)));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e" + refinedShops.get(i).get("loc")));
            lore.add(0,ChatColor.LIGHT_PURPLE + refinedShops.get(i).get("owners"));
            shopMeta.setLore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            refinedShopDirectory.setItem(i,shopItem);
        }
        player.openInventory(refinedShopDirectory);

    }

    public void openRefinedItemInventory(Player player, String searchKey) {
        List<ItemStack> refinedItems = plugin.getShopRepo().findItem(searchKey);
        if(refinedItems.size() == 0) {
            player.sendMessage(ChatColor.RED + "No items with matching name found");
            return;
        }

        Inventory refinedItemInv = Bukkit.createInventory(new ShopInvHolder(""),Math.min(9*(refinedItems.size()/9 + (refinedItems.size()%9) == 0 ? 0 : 1),54),"Search results");

        for(int i=0;i<Math.min(refinedItems.size(),54);i++) {
            refinedItemInv.setItem(i,refinedItems.get(i));
        }

        player.openInventory(refinedItemInv);
    }

    public void openShopDirectoryModerator(Player moderator,int type) {
        List<Map<String,String>> shops = type == 1 ? plugin.getShopRepo().getPendingShopDetails() : plugin.getShopRepo().getShopDetails();
        Inventory shopDirectory = Bukkit.createInventory(new MarketplaceBookHolder(shops,type), Math.min(9*(shops.size()/9 + (shops.size()%9 == 0 ? 0 : 1)),54) + (shops.size() == 0 ? 9 : 0),"Marketplace Directory");
        for(int i=0;i<(shops.size() > 54 ? 45 : shops.size());i++) {
            ItemStack shopItem = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + shops.get(i).get("name"));
            List<String> lore = new ArrayList<>(Arrays.asList(ChatPaginator.wordWrap(ChatColor.translateAlternateColorCodes('&',"&1" + shops.get(i).get("desc")),30)));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e" + shops.get(i).get("loc")));
            lore.add(0,ChatColor.LIGHT_PURPLE + shops.get(i).get("owners"));
            if(type == 1) {
                lore.add(ChatColor.GREEN + "Right click to approve");
                lore.add(ChatColor.RED + "Left click to reject");
            }
            else if (type == 2) {
                lore.add(ChatColor.RED + "Right click to remove");
            }
            shopMeta.setLore(lore);
            shopMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            shopItem.setItemMeta(shopMeta);
            shopDirectory.setItem(i,shopItem);
        }

        if(shops.size() > 54) {
            ItemStack nextPage = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName("Next Page");
            nextPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            nextPage.setItemMeta(nextPageMeta);
            shopDirectory.setItem(49,nextPage);
            ItemStack prevPage = new ItemStack(Material.BARRIER);
            ItemMeta prevPageMeta = prevPage.getItemMeta();
            prevPageMeta.setDisplayName("Previous Page");
            prevPageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            prevPage.setItemMeta(prevPageMeta);
            shopDirectory.setItem(48,prevPage);
            ItemStack pageNum = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta pageNumMeta = pageNum.getItemMeta();
            pageNumMeta.setDisplayName("Page 1");
            pageNum.setItemMeta(pageNumMeta);
            shopDirectory.setItem(45,pageNum);
        }

        if(shops.size() == 0) {
            System.out.println("No pending shop");
        }

        moderator.openInventory(shopDirectory);
    }

}
