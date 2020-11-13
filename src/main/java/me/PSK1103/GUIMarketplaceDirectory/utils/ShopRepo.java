package me.PSK1103.GUIMarketplaceDirectory.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import me.PSK1103.GUIMarketplaceDirectory.GUIMarketplaceDirectory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

class ItemList {
    ItemStack item;
    int price;
    String qty;
    String name,customName;
    ItemList() {}

    ItemList(String itemName, String qty, int price) {
        this.name = itemName;
        this.qty = qty;
        this.price = price;
        this.customName = "";
        item = new ItemStack(Material.getMaterial(itemName));
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>(2);
        String qtyString = "";
        String [] parts = qty.split(":");
        if(Integer.parseInt(parts[0]) > 0)
            qtyString = parts[0] + " shulker";
        else if(Integer.parseInt(parts[1]) > 0)
            qtyString = parts[1] + " stack";
        else if (Integer.parseInt(parts[2]) > 0)
            qtyString = parts[2];

        else return;

        lore.add(ChatColor.translateAlternateColorCodes('&', "&6" + qtyString + " &ffor &3" + price + " diamonds"));
        lore.add("Right click to find a better deal");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
    }

    ItemList(String itemName) {
        this.name = itemName;
        this.customName = "";
        item = new ItemStack(Material.getMaterial(itemName));
        qty = "";
        price = 0;
    }
    ItemList(String itemName,String customName) {
        this.name = itemName;
        item = new ItemStack(Material.getMaterial(itemName));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(customName);
        item.setItemMeta(meta);
        qty = "";
        price = 0;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(customName);
        item.setItemMeta(meta);
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public void setPrice(int price) {
        this.price = price;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>(2);
        String qtyString = "";
        String [] parts = qty.split(":");
        if(Integer.parseInt(parts[0]) > 0)
            qtyString = parts[0] + " shulker";
        else if(Integer.parseInt(parts[1]) > 0)
            qtyString = parts[1] + " stack";
        else if (Integer.parseInt(parts[2]) > 0)
            qtyString = parts[2];

        else return;

        lore.add(ChatColor.translateAlternateColorCodes('&', "&6" + qtyString + " &ffor &3" + price + " diamonds"));
        lore.add("Right click to find a better deal");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
    }

    public ItemStack getItem() {
        return item;
    }
}

class Shop {
    private String name;
    private String loc;
    private String desc;
    private Map<String,String> owners;
    private String owner,uuid;
    private String key;

    private List<ItemList> inv;

    public Shop() {}

    public Shop(String name,String desc, String owner, String uuid, String key,String loc) {
        this.name = name;
        this.desc = desc;
        this.owner = owner;
        this.owners = new HashMap<>();
        this.owners.put(uuid,owner);
        this.uuid = uuid;
        this.key = key;
        this.loc = loc;
        this.inv = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setOwners(Map<String, String> owners) {
        this.owners = owners;
    }
    
    public void addOwner(String uuid, String owner) {
        this.owners.put(uuid, owner);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setInv(List<ItemList> inv) {
        this.inv = inv;
    }

    public void addToInv(ItemList item) {
        inv.add(item);
    }

    public String getName() {
        return name;
    }

    public String getLoc() {
        return loc;
    }

    public String getDesc() {
        return desc;
    }

    public String getOwner() {
        return owner;
    }

    public Map<String, String> getOwners() {
        return owners;
    }

    public String getUuid() {
        return uuid;
    }

    public String getKey() {
        return key;
    }

    public List<ItemList> getInv() {
        return inv;
    }
}

public class ShopRepo {
    private Map<String,Shop> shops;
    private Map<String,Shop> pendingShops;
    private Map<String,Shop> waitingShops;
    private GUIMarketplaceDirectory plugin;
    private HashMap<String,String> shopsUnderAdd;
    private HashMap<String,Integer> shopsUnderEdit;
    private HashMap<String,ItemList> itemToAdd;
    private HashMap<String,String> shopsUnderReject,shopsUnderRemove;

    public ShopRepo(GUIMarketplaceDirectory plugin) {
        this.shops = new HashMap<>();
        this.pendingShops = new HashMap<>();
        shopsUnderAdd = new HashMap<>();
        shopsUnderReject = new HashMap<>();
        shopsUnderRemove = new HashMap<>();
        shopsUnderEdit = new HashMap<>();
        waitingShops = new HashMap<>();
        itemToAdd = new HashMap<>();
        this.plugin = plugin;
        initShops();
    }

    public void addShopAsOwner(String name, String desc, String owner, String uuid, String key, String loc) {
        Shop shop = new Shop(name,desc,owner,uuid,key,loc);
        pendingShops.put(key,shop);

        saveShops();
    }
    
    public void addShop(String name, String desc, String owner, String uuid, String key, String loc) {
        Shop shop = new Shop(name,desc,owner,uuid,key,loc);
        waitingShops.put(uuid,shop);
        shopsUnderEdit.put(key,2);
        shopsUnderAdd.put(uuid, key);
    }

    public boolean getIsInitOwner(String  uuid) {
        return waitingShops.containsKey(uuid);
    }

    public void stopInitOwner(String uuid) {
        waitingShops.remove(uuid);
        if(shopsUnderAdd.containsKey(uuid)) {
            shopsUnderEdit.remove(shopsUnderAdd.get(uuid));
            shopsUnderAdd.remove(uuid);
        }
    }

    public int startAddingOwner(String uuid, String key) {
        if(shopsUnderAdd.containsKey(uuid) && !shopsUnderEdit.containsKey(key))
            return 0;
        if(!shops.containsKey(key) && !pendingShops.containsKey(key))
            return -1;
        shopsUnderAdd.put(uuid, key);
        shopsUnderEdit.put(key,1);
        return 1;
    }

    public boolean getIsEditingShop(String uuid,String key) {
        return shopsUnderAdd.containsKey(uuid) || shopsUnderAdd.containsValue(key);
    }

    public boolean getIsAddingOwner(String key) {
        return shopsUnderAdd.containsValue(key) && shopsUnderEdit.containsKey(key);
    }



    public boolean getIsUserAddingOwner(String uuid) {
        return shopsUnderAdd.containsKey(uuid) && shopsUnderEdit.containsKey(shopsUnderAdd.get(uuid)) || waitingShops.containsKey(uuid);
    }
    
    public void addOwner(String uuid, Player player) {
        if (waitingShops.containsKey(uuid)) {
            Shop shop = waitingShops.get(uuid);
            shop.setOwner(player.getName());
            shop.setUuid(player.getUniqueId().toString());
            shop.addOwner(player.getUniqueId().toString(), player.getName());
            pendingShops.put(shop.getKey(), shop);
            waitingShops.remove(uuid);
            shopsUnderAdd.remove(uuid);
            shopsUnderEdit.remove(shop.getKey());
            saveShops();
        }
        else {
            if(shopsUnderAdd.containsKey(uuid)) {
                if (pendingShops.containsKey(shopsUnderAdd.get(uuid))) {
                    pendingShops.get(shopsUnderAdd.get(uuid)).addOwner(player.getUniqueId().toString(),player.getName());
                }
                else if(shops.containsKey(shopsUnderAdd.get(uuid))) {
                    shops.get(shopsUnderAdd.get(uuid)).addOwner(player.getUniqueId().toString(),player.getName());
                }
                shopsUnderEdit.remove(shopsUnderAdd.get(uuid));
                shopsUnderAdd.remove(uuid);
            }
        }
    }
    
    private void saveShops() {
        JSONParser parser = new JSONParser();
        try {
            File shopFile = plugin.getShops();
            assert shopFile != null;
            parser.parse(new FileReader(shopFile));
            JSONObject data = new JSONObject();
            JSONArray shopJSONs = new JSONArray();
            JSONArray pShopJSONs = new JSONArray();
            shops.forEach((s, shop1) -> {
                JSONObject shopJSON = new JSONObject();
                shopJSON.put("name",shop1.getName());
                shopJSON.put("desc", shop1.getDesc());
                shopJSON.put("owner", shop1.getOwner());
                shopJSON.put("owners",shop1.getOwners());
                shopJSON.put("uuid", shop1.getUuid());
                shopJSON.put("key", shop1.getKey());
                shopJSON.put("loc",shop1.getLoc());

                JSONArray items = new JSONArray();

                shop1.getInv().forEach(itemList -> {
                    JSONObject item = new JSONObject();
                    item.put("name",itemList.item.getType().getKey().getKey().toUpperCase());
                    item.put("price",Integer.valueOf(itemList.price).toString());
                    item.put("qty",itemList.qty);
                    if(itemList.item.getItemMeta().hasDisplayName())
                        item.put("customName",itemList.item.getItemMeta().getDisplayName());

                    items.add(item);
                });

                shopJSON.put("items", items);


                shopJSONs.add(shopJSON);
            });

            pendingShops.forEach((s, shop1) -> {
                JSONObject shopJSON = new JSONObject();
                shopJSON.put("name",shop1.getName());
                shopJSON.put("desc", shop1.getDesc());
                shopJSON.put("owner", shop1.getOwner());
                shopJSON.put("owners",shop1.getOwners());
                shopJSON.put("uuid", shop1.getUuid());
                shopJSON.put("key", shop1.getKey());
                shopJSON.put("loc",shop1.getLoc());

                JSONArray items = new JSONArray();

                shop1.getInv().forEach(itemList -> {
                    JSONObject item = new JSONObject();
                    item.put("name",itemList.item.getType().getKey().getKey().toUpperCase());
                    item.put("price",Integer.valueOf(itemList.price).toString());
                    item.put("qty",itemList.qty);
                    if(itemList.item.getItemMeta().hasDisplayName())
                        item.put("customName",itemList.item.getItemMeta().getDisplayName());

                    items.add(item);
                });

                shopJSON.put("items", items);


                pShopJSONs.add(shopJSON);
            });

            data.put("shops",shopJSONs);
            data.put("pendingShops",pShopJSONs);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(data.toJSONString());
            String prettyJsonString = gson.toJson(je);

            FileWriter fw = new FileWriter(shopFile);
            fw.write(prettyJsonString);
            fw.flush();
            fw.close();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean initShops() {
        File shopFile = plugin.getShops();

        try {

            JSONParser parser = new JSONParser();
            assert shopFile != null;
            JSONObject data = (JSONObject) parser.parse(new FileReader(shopFile));
            JSONArray shopJSONs = ((JSONArray) data.get("shops"));
            JSONArray pShopJSONs = ((JSONArray) data.get("pendingShops"));

            if(shopJSONs.size() > 0) {
                for (Object json : shopJSONs) {
                    try {
                        JSONObject shopJSON = ((JSONObject) json);
                        Shop shop = new Shop(shopJSON.get("name").toString(), shopJSON.get("desc").toString(), shopJSON.get("owner").toString(), shopJSON.get("uuid").toString(), shopJSON.get("key").toString(), shopJSON.get("loc").toString());
                        if(shopJSON.containsKey("owners")) {
                            Map<String, String> owners = new Gson().fromJson(shopJSON.get("owners").toString(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());

                            shop.setOwners(owners);
                        }
                        JSONArray itemsArray = ((JSONArray) shopJSON.get("items"));
                        for (Object o : itemsArray) {
                            try {
                                JSONObject itemJSON = ((JSONObject) o);
                                ItemList item = new ItemList(itemJSON.get("name").toString(), itemJSON.get("qty").toString(), Integer.parseInt(itemJSON.get("price").toString()));
                                if (itemJSON.get("customName") != null)
                                    item.setCustomName(itemJSON.get("customName").toString());
                                shop.addToInv(item);
                            } catch (ClassCastException | NullPointerException e) {
                                if (e instanceof ClassCastException)
                                    plugin.getLogger().severe("Malformed shops.json, cannot add item");
                                if (e instanceof NullPointerException)
                                    plugin.getLogger().warning("Key value(s) missing, item won't be created");
                                e.printStackTrace();
                            }
                        }
                        shops.put(shopJSON.get("key").toString(), shop);
                    } catch (ClassCastException | NullPointerException e) {
                        if (e instanceof ClassCastException)
                            plugin.getLogger().severe("Malformed shops.json, cannot add shop");
                        if (e instanceof NullPointerException)
                            plugin.getLogger().warning("Key value(s) missing, shop won't be created");
                        e.printStackTrace();
                    }
                }
            }
            else
                plugin.getLogger().warning("No shops in directory");

            if(pShopJSONs.size() > 0) {
                for (Object json : pShopJSONs) {
                    try {
                        JSONObject shopJSON = ((JSONObject) json);
                        Shop shop = new Shop(shopJSON.get("name").toString(), shopJSON.get("desc").toString(), shopJSON.get("owner").toString(), shopJSON.get("uuid").toString(), shopJSON.get("key").toString(), shopJSON.get("loc").toString());
                        if(shopJSON.containsKey("owners")) {
                            Map<String, String> owners = new Gson().fromJson(shopJSON.get("owners").toString(),
                                    new TypeToken<HashMap<String, String>>() {
                                        }.getType());

                            shop.setOwners(owners);
                        }
                        JSONArray itemsArray = ((JSONArray) shopJSON.get("items"));
                        for (Object o : itemsArray) {
                            try {
                                JSONObject itemJSON = ((JSONObject) o);
                                ItemList item = new ItemList(itemJSON.get("name").toString(), itemJSON.get("qty").toString(), Integer.parseInt(itemJSON.get("price").toString()));
                                if (itemJSON.get("customName") != null)
                                    item.setCustomName(itemJSON.get("customName").toString());
                                shop.addToInv(item);
                            } catch (ClassCastException | NullPointerException e) {
                                if (e instanceof ClassCastException)
                                    plugin.getLogger().severe("Malformed shops.json, cannot add item");
                                if (e instanceof NullPointerException)
                                    plugin.getLogger().warning("Key value(s) missing, item won't be created");
                                e.printStackTrace();
                            }
                        }
                        pendingShops.put(shopJSON.get("key").toString(), shop);
                    } catch (ClassCastException | NullPointerException e) {
                        if (e instanceof ClassCastException)
                            plugin.getLogger().severe("Malformed shops.json, cannot add shop");
                        if (e instanceof NullPointerException)
                            plugin.getLogger().warning("Key value(s) missing, shop won't be created");
                        e.printStackTrace();
                    }
                }
            }
            return true;


        } catch (IOException | ParseException | ClassCastException | NullPointerException e) {
            if(e instanceof ParseException || e instanceof ClassCastException)
                plugin.getLogger().severe("Malformed shops.json, cannot initiate shops");
            if(e instanceof NullPointerException)
                plugin.getLogger().warning("Key value(s) missing, shop or item won't be created");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isShopUnderEditOrAdd(String key) {
        return itemToAdd.containsKey(key) || shopsUnderEdit.containsKey(key);
    }

    public int initItemAddition(String uuid, String key, String name) {
        if(!shops.containsKey(key) && !pendingShops.containsKey(key))
            return -1;

        if(shopsUnderAdd.containsKey(uuid)) {
            shopsUnderAdd.put(uuid,key);
            ItemList item = new ItemList(name);
            itemToAdd.put(key,item);
            return 0;
        }

        shopsUnderAdd.put(uuid,key);
        ItemList item = new ItemList(name);
        itemToAdd.put(key,item);
        return 1;
    }
    public int initItemAddition(String uuid, String key, String name, String customName) {
        if(!shops.containsKey(key) && !pendingShops.containsKey(key))
            return -1;

        if(shopsUnderAdd.containsKey(uuid) || itemToAdd.containsKey(key)) {
            return 0;
        }

        shopsUnderAdd.put(uuid,key);
        ItemList item = new ItemList(name,customName);
        itemToAdd.put(key,item);
        return 1;
    }

    public void initShopOwnerAddition(String uuid) {
        shopsUnderEdit.put(shopsUnderAdd.get(uuid),1);
    }

    public int getEditType(String uuid) {
        if(!shopsUnderAdd.containsKey(uuid))
            return -1;

        return shopsUnderEdit.getOrDefault(shopsUnderAdd.get(uuid), 0);
    }
    public void setQty(String qty,String uuid) {
        itemToAdd.get(shopsUnderAdd.get(uuid)).setQty(qty);
    }

    public void setPrice(int price, String uuid) {
        itemToAdd.get(shopsUnderAdd.get(uuid)).setPrice(price);
        if(shops.containsKey(shopsUnderAdd.get(uuid)))
            shops.get(shopsUnderAdd.get(uuid)).addToInv(itemToAdd.get(shopsUnderAdd.get(uuid)));
        else if(pendingShops.containsKey(shopsUnderAdd.get(uuid)))
            pendingShops.get(shopsUnderAdd.get(uuid)).addToInv(itemToAdd.get(shopsUnderAdd.get(uuid)));

        itemToAdd.remove(shopsUnderAdd.get(uuid));
        shopsUnderAdd.remove(uuid);
        
        saveShops();
    }

    public boolean isAddingItem(String uuid) {
        return shopsUnderAdd.containsKey(uuid) && !waitingShops.containsKey(uuid) && !shopsUnderEdit.containsKey(shopsUnderAdd.get(uuid));
    }

    public void stopEditing(String uuid) {
        itemToAdd.remove(shopsUnderAdd.get(uuid));
        shopsUnderAdd.remove(uuid);
    }

    public boolean isShopOwner(String uuid, String key) {
        return (shops.containsKey(key) && (shops.get(key).getUuid().equals(uuid) || shops.get(key).getOwners().containsKey(uuid))) || (pendingShops.containsKey(key) && (pendingShops.get(key).getUuid().equals(uuid) || pendingShops.get(key).getOwners().containsKey(uuid)));
    }

    public void approveShop(String key) {
        if(pendingShops.containsKey(key)) {
            shops.put(key,pendingShops.get(key));
            pendingShops.remove(key);
            saveShops();
        }
    }

    public void rejectShop(String uuid) {
        pendingShops.remove(shopsUnderReject.get(uuid));
        shopsUnderReject.remove(uuid);
        saveShops();
    }

    public void cancelRejectShop(String uuid) {
        shopsUnderReject.remove(uuid);
    }

    public boolean isShopRejecting(String key) {
        return shopsUnderReject.containsValue(key);
    }

    public boolean isUserRejectingShop(String uuid) {
        return shopsUnderReject.containsKey(uuid);
    }

    public void addShopToRejectQueue(String uuid, String key) {
        shopsUnderReject.put(uuid, key);
    }

    public void removeShop(String uuid) {
        shops.remove(shopsUnderRemove.get(uuid));
        shopsUnderRemove.remove(uuid);
        saveShops();
    }

    public void cancelRemoveShop(String uuid) {
        shopsUnderRemove.remove(uuid);
    }

    public boolean isShopRemoving(String key) {
        return shopsUnderRemove.containsValue(key);
    }

    public boolean isUserRemovingShop(String uuid) {
        return shopsUnderRemove.containsKey(uuid);
    }

    public void addShopToRemoveQueue(String uuid, String key) {
        shopsUnderRemove.put(uuid, key);
    }

    public List<Map<String,String>> getShopDetails() {
        List<Map<String,String>> detailsList = new ArrayList<>();
        shops.forEach((s, shop) -> {
            Map<String,String> details = new HashMap<>();
            details.put("name",shop.getName());
            details.put("desc",shop.getDesc());
            details.put("owners", String.join(", ",shop.getOwners().values()));
            details.put("loc",shop.getLoc());
            details.put("key",shop.getKey());
            detailsList.add(details);
        });
        return detailsList;
    }

    public List<Map<String,String>> getPendingShopDetails() {
        List<Map<String,String>> detailsList = new ArrayList<>();
        pendingShops.forEach((s, shop) -> {
            Map<String,String> details = new HashMap<>();
            details.put("name",shop.getName());
            details.put("desc",shop.getDesc());
            details.put("owners", String.join(", ",shop.getOwners().values()));
            details.put("loc",shop.getLoc());
            details.put("key",shop.getKey());
            detailsList.add(details);
        });
        return detailsList;
    }

    public List<ItemStack> getShopInv(String key) {
        Shop shop = null;
        if(shops.containsKey(key))
            shop = shops.get(key);
        else if(pendingShops.containsKey(key))
            shop = pendingShops.get(key);

        List<ItemStack> inv = new ArrayList<>();

        if(shop== null) {
            return inv;
        }

        shop.getInv().forEach(itemList -> inv.add(itemList.getItem()));
        return inv;
    }

    public void findBetterAlternative(Player player,String key, int pos) {
        ItemList item = shops.get(key).getInv().get(pos);
        String name = item.name;
        double value = 0;
        String [] parts1 =item.qty.split(":");
        if(Integer.parseInt(parts1[0]) > 0)
            value = Double.parseDouble(parts1[0])*1728;
        else if(Integer.parseInt(parts1[1]) > 0)
            value = Double.parseDouble(parts1[1])*64;
        else if (Integer.parseInt(parts1[2]) > 0)
            value = Double.parseDouble(parts1[2]);
        value/= item.price;
        final boolean[] found = {false};
        double finalValue = value;
        shops.forEach((s, shop) ->
            shop.getInv().forEach(itemList -> {
                if(itemList.name.equals(name)) {
                    double val = 0;
                    String [] parts =itemList.qty.split(":");
                    if(Integer.parseInt(parts[0]) > 0)
                        val = Double.parseDouble(parts[0])*1728;
                    else if(Integer.parseInt(parts[1]) > 0)
                        val = Double.parseDouble(parts[1])*64;
                    else if (Integer.parseInt(parts[2]) > 0)
                        val = Double.parseDouble(parts[2]);
                    val/= itemList.price;

                    if(val > finalValue) {
                        player.sendMessage(ChatColor.GOLD + shop.getName() + ChatColor.WHITE + " has a better deal: " + ChatColor.DARK_AQUA + String.format("%.2f",val) + " items per dia");
                        found[0] = true;
                    }
                }
            })
        );
        if(!found[0]) {
            player.sendMessage("No better alternatives found");
        }
    }

    public String getShopName(String key) {
        return shops.containsKey(key) ? shops.get(key).getName() : pendingShops.containsKey(key) ? pendingShops.get(key).getName() : "";
    }

    public List<Map<String,String>> getRefinedShopsByName(String searchKey) {
        List<Map<String,String>> detailsList = new ArrayList<>();
        shops.forEach((s, shop) -> {

            if(shop.getName().toLowerCase().trim().contains(searchKey.toLowerCase().trim())) {

                Map<String, String> details = new HashMap<>();
                details.put("name", shop.getName());
                details.put("desc", shop.getDesc());
                details.put("owners", String.join(", ",shop.getOwners().values()));
                details.put("loc", shop.getLoc());
                details.put("key", shop.getKey());
                detailsList.add(details);
            }
        });
        return detailsList;
    }

    public List<Map<String,String>> getRefinedShopsByPlayer(String searchKey) {
        List<Map<String,String>> detailsList = new ArrayList<>();
        shops.forEach((s, shop) -> {

            boolean [] contains = {false};
            shop.getOwners().values().forEach(s1 -> {
                if(s1.toLowerCase().trim().contains(searchKey.toLowerCase().trim())) {
                    contains[0] = true;
                }
            });
            if(contains[0]) {
                Map<String, String> details = new HashMap<>();
                details.put("name", shop.getName());
                details.put("desc", shop.getDesc());
                details.put("owners", String.join(", ",shop.getOwners().values()));
                details.put("loc", shop.getLoc());
                details.put("key", shop.getKey());
                detailsList.add(details);
            }
        });
        return detailsList;
    }

    public List<ItemStack> findItem(String searchKey) {
        List<ItemStack> searchResults = new ArrayList<>();
        shops.forEach((s, shop) -> {
            List<ItemList> inv = shop.getInv();
            inv.forEach(itemList -> {
                if(itemList.name.replace('_',' ').toLowerCase().trim().contains(searchKey.toLowerCase().trim())) {
                    ItemStack itemToAdd = itemList.item.clone();
                    ItemMeta meta = itemToAdd.getItemMeta();
                    List<String> lore = meta.getLore();
                    lore.add(ChatColor.GREEN + "From " + shop.getName());
                    meta.setLore(lore);
                    itemToAdd.setItemMeta(meta);
                    searchResults.add(itemToAdd);
                }
                else if(itemList.customName.length()>0 && itemList.customName.toLowerCase().trim().contains(searchKey.toLowerCase().trim())) {
                    ItemStack itemToAdd = itemList.item.clone();
                    ItemMeta meta = itemToAdd.getItemMeta();
                    List<String> lore = meta.getLore();
                    lore.remove(1);
                    lore.add(ChatColor.GREEN + "From " + shop.getName());
                    meta.setLore(lore);
                    itemToAdd.setItemMeta(meta);
                    searchResults.add(itemToAdd);
                }
            });
        });
        return searchResults;
    }

}


