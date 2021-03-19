package me.PSK1103.GUIMarketplaceDirectory.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTListCompound;
import me.PSK1103.GUIMarketplaceDirectory.guimd.GUIMarketplaceDirectory;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class ItemList {
    ItemStack item;
    int price;
    String qty;
    String name, customName;
    String customType;
    Map<String, Object> extraInfo;

    ItemList() {
    }

    ItemList(String itemName, String qty, int price) {
        this.name = itemName;
        this.qty = qty;
        this.price = price;
        this.customName = "";
        this.customType = "";
        this.extraInfo = new HashMap<>(0);
        item = new ItemStack(Material.getMaterial(itemName));
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>(2);
        String qtyString = "";
        String[] parts = qty.split(":");
        if (Integer.parseInt(parts[0]) > 0)
            qtyString = parts[0] + " shulker";
        else if (Integer.parseInt(parts[1]) > 0)
            qtyString = parts[1] + " stack";
        else if (Integer.parseInt(parts[2]) > 0)
            qtyString = parts[2];

        else return;

        lore.add(ChatColor.translateAlternateColorCodes('&', "&6" + qtyString + " &ffor &3" + price + " diamond" + (price == 1 ? "" : "s")));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
    }

    ItemList(String itemName, ItemMeta meta) {
        this.name = itemName;
        this.customName = "";
        this.extraInfo = new HashMap<>(0);
        item = new ItemStack(Material.getMaterial(itemName));
        item.setItemMeta(meta);
        if (meta.hasDisplayName())
            this.customName = meta.getDisplayName();
        qty = "";
        price = 0;
    }

    public static ItemStack getCustomItem(ItemStack item, String customType, Map<String, Object> extraInfo) {
        switch (customType) {
            case "head":
                NBTItem head = new NBTItem(item);
                NBTCompound skull = head.addCompound("SkullOwner");
                skull.setString("Name", extraInfo.get("name").toString());
                skull.setString("Id", extraInfo.get("uuid").toString());
                NBTListCompound texture = skull.addCompound("Properties").getCompoundList("textures").addCompound();
                texture.setString("Signature", extraInfo.get("signature").toString());
                texture.setString("Value", extraInfo.get("value").toString());
                item = head.getItem();
                break;
            case "potion":
            case "tippedArrow":
                PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                PotionData base = new PotionData(PotionType.valueOf(extraInfo.get("effect").toString()), (Boolean) extraInfo.get("extended"), (Boolean) extraInfo.get("upgraded"));
                potionMeta.setBasePotionData(base);
                item.setItemMeta(potionMeta);
                break;
            case "rocket":
                NBTItem rocket = new NBTItem(item);
                NBTCompound fl = rocket.addCompound("Fireworks");
                double flno = (Double) extraInfo.get("flight");
                fl.setByte("Flight", (byte) flno);
                item = rocket.getItem();
                FireworkMeta fireworkMeta = (FireworkMeta) item.getItemMeta();
                List<Object> effects = (List<Object>) extraInfo.get("effects");
                if (effects != null && effects.size() > 0) {
                    List<FireworkEffect> fireworkEffects = new ArrayList<>();
                    effects.forEach(o -> {
                        Map<String, Object> effect = ((Map<String, Object>) o);
                        List<Color> colors = new ArrayList<>();
                        List<Color> fadeColors = new ArrayList<>();
                        ((List<Double>) effect.get("colors")).forEach(aDouble -> colors.add(Color.fromRGB(aDouble.intValue())));
                        ((List<Double>) effect.get("fadeColors")).forEach(aDouble -> fadeColors.add(Color.fromRGB(aDouble.intValue())));
                        FireworkEffect fireworkEffect = FireworkEffect.builder()
                                .flicker((Boolean) effect.get("flicker"))
                                .trail((Boolean) effect.get("trail"))
                                .with(FireworkEffect.Type.valueOf(effect.get("type").toString()))
                                .withColor(colors)
                                .withFade(fadeColors)
                                .build();
                        fireworkEffects.add(fireworkEffect);
                    });
                    fireworkMeta.addEffects(fireworkEffects);
                    item.setItemMeta(fireworkMeta);
                }
                break;
            case "banner":
                BannerMeta bannerMeta = (BannerMeta) item.getItemMeta();
                List<Object> patterns = (List<Object>) extraInfo.get("patterns");
                List<Pattern> bannerPatterns = new ArrayList<>();
                patterns.forEach(o -> {
                    Map<String, Object> pattern = (Map<String, Object>) o;
                    Pattern bannerPattern = new Pattern(DyeColor.valueOf(pattern.get("color").toString()), PatternType.valueOf(pattern.get("type").toString()));
                    bannerPatterns.add(bannerPattern);
                });
                bannerMeta.setPatterns(bannerPatterns);
                item.setItemMeta(bannerMeta);
                break;
            case "shulker":
                List<Map<String, Object>> contents = (List<Map<String, Object>>) extraInfo.get("contents");
                List<ItemStack> items = new ArrayList<>();
                contents.forEach(content -> {
                    ItemStack itemStack = new ItemStack(Material.valueOf(content.get("name").toString()), Double.valueOf(content.get("quantity").toString()).intValue());
                    if (content.containsKey("customName")) {
                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setDisplayName(content.get("customName").toString());
                    }
                    if (content.containsKey("customType")) {
                        getCustomItem(itemStack, content.get("customType").toString(), (Map<String, Object>) content.get("extraInfo"));
                    }

                    items.add(itemStack);

                });

                BlockStateMeta blockStateMeta = (BlockStateMeta) item.getItemMeta();
                ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                shulkerBox.getInventory().setContents(items.toArray(new ItemStack[0]));
                shulkerBox.update(true, false);
                blockStateMeta.setBlockState(shulkerBox);
                item.setItemMeta(blockStateMeta);

                break;
        }
        return item;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(customName);
        item.setItemMeta(meta);
    }

    public void setExtraInfo(Map<String, Object> extraInfo, String customType) {
        this.extraInfo = extraInfo;
        this.customType = customType;
        this.item = getCustomItem(item, customType, extraInfo);
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public void setPrice(int price) {
        this.price = price;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>(2);
        String qtyString = "";
        String[] parts = qty.split(":");
        if (Integer.parseInt(parts[0]) > 0)
            qtyString = parts[0] + " shulker";
        else if (Integer.parseInt(parts[1]) > 0)
            qtyString = parts[1] + " stack";
        else if (Integer.parseInt(parts[2]) > 0)
            qtyString = parts[2];

        else return;

        if(price <=0) {
            this.price = -1;
            lore.add(ChatColor.GRAY + "Price hidden or variable");
        }
        else {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&6" + qtyString + " &ffor &3" + price + " diamonds"));
        }
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
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
    private Map<String, String> owners;
    private String owner, uuid;
    private String key;
    private String displayItem;

    private List<ItemList> inv;

    public Shop() {
    }

    public Shop(String name, String desc, String owner, String uuid, String key, String loc) {
        this.name = name;
        this.desc = desc;
        this.owner = owner;
        this.owners = new HashMap<>();
        this.owners.put(uuid, owner);
        this.uuid = uuid;
        this.key = key;
        this.loc = loc;
        this.inv = new ArrayList<>();
        this.displayItem = "WRITTEN_BOOK";
    }

    public void setDisplayItem(String displayItem) {
        this.displayItem = displayItem;
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

    public String getDisplayItem() {
        return displayItem;
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
        return inv == null ? new ArrayList<>() : inv;
    }
}

public class ShopRepo {
    private final Map<String, Shop> shops;
    private final Map<String, Shop> pendingShops;
    private final Map<String, Shop> waitingShops;
    private final GUIMarketplaceDirectory plugin;
    private final HashMap<String, String> shopsUnderAdd;
    private final HashMap<String, Integer> shopsUnderEdit;
    private final HashMap<String, ItemList> itemToAdd;
    private final HashMap<String, String> shopsUnderReject;
    private final HashMap<String, String> shopsUnderRemove;

    private final Logger logger;

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
        this.logger = plugin.getSLF4JLogger();
        if (initShops()) {
            logger.info("Shops loaded");
            if (plugin.getCustomConfig().bstatsEnabled())
                addShopCountMetric();
        } else {
            logger.error("Error while loading shops, disabling GUIMD");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public void addShopAsOwner(String name, String desc, String owner, String uuid, String key, String loc, String displayItem) {
        Shop shop = new Shop(name, desc, owner, uuid, key, loc);
        Material material = Material.matchMaterial(displayItem);
        if(material != null)
            shop.setDisplayItem(displayItem);
        else shop.setDisplayItem("WRITTEN_BOOK");
        if (plugin.getCustomConfig().directoryModerationEnabled())
            pendingShops.put(key, shop);
        else
            shops.put(key, shop);

        saveShops();
    }

    public void addShop(String name, String desc, String owner, String uuid, String key, String loc, String displayItem) {
        Shop shop = new Shop(name, desc, owner, uuid, key, loc);
        Material material = Material.matchMaterial(displayItem);
        if(material != null)
            shop.setDisplayItem(displayItem);
        else shop.setDisplayItem("WRITTEN_BOOK");
        waitingShops.put(uuid, shop);
        shopsUnderEdit.put(key, 2);
        shopsUnderAdd.put(uuid, key);
    }

    public String getOwner(String key) {
        return shops.get(key).getOwner();
    }

    public boolean getIsInitOwner(String uuid) {
        return waitingShops.containsKey(uuid);
    }

    public void stopInitOwner(String uuid) {
        waitingShops.remove(uuid);
        if (shopsUnderAdd.containsKey(uuid)) {
            shopsUnderEdit.remove(shopsUnderAdd.get(uuid));
            shopsUnderAdd.remove(uuid);
        }
    }

    public int startAddingOwner(String uuid, String key) {
        if (shopsUnderAdd.containsKey(uuid) && !shopsUnderEdit.containsKey(key))
            return 0;
        if (!shops.containsKey(key) && !pendingShops.containsKey(key))
            return -1;
        shopsUnderAdd.put(uuid, key);
        shopsUnderEdit.put(key, 1);
        return 1;
    }

    public int startSettingDisplayItem(String uuid, String key) {
        if (shopsUnderAdd.containsKey(uuid) && !shopsUnderEdit.containsKey(key))
            return 0;
        if (!shops.containsKey(key) && !pendingShops.containsKey(key))
            return -1;
        shopsUnderAdd.put(uuid, key);
        shopsUnderEdit.put(key, 3);
        return 1;
    }

    public int startRemovingShop(String uuid, String key) {
        if (shopsUnderAdd.containsKey(uuid) && !shopsUnderEdit.containsKey(key))
            return 0;
        if (!shops.containsKey(key) && !pendingShops.containsKey(key))
            return -1;
        shopsUnderRemove.put(uuid, key);
        return 1;
    }

    public boolean getIsEditingShop(String uuid, String key) {
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
            if (plugin.getCustomConfig().directoryModerationEnabled())
                pendingShops.put(shop.getKey(), shop);
            else
                shops.put(shop.getKey(), shop);

            waitingShops.remove(uuid);
            shopsUnderAdd.remove(uuid);
            shopsUnderEdit.remove(shop.getKey());
            saveShops();
        } else {
            if (shopsUnderAdd.containsKey(uuid)) {
                if (pendingShops.containsKey(shopsUnderAdd.get(uuid))) {
                    pendingShops.get(shopsUnderAdd.get(uuid)).addOwner(player.getUniqueId().toString(), player.getName());
                } else if (shops.containsKey(shopsUnderAdd.get(uuid))) {
                    shops.get(shopsUnderAdd.get(uuid)).addOwner(player.getUniqueId().toString(), player.getName());
                }
                shopsUnderEdit.remove(shopsUnderAdd.get(uuid));
                shopsUnderAdd.remove(uuid);
                saveShops();
            }
        }
    }

    public void setDisplayItem(String uuid, String materialName) {
        if (shopsUnderAdd.containsKey(uuid)) {
            if (pendingShops.containsKey(shopsUnderAdd.get(uuid))) {
                pendingShops.get(shopsUnderAdd.get(uuid)).setDisplayItem(materialName);
            } else if (shops.containsKey(shopsUnderAdd.get(uuid))) {
                shops.get(shopsUnderAdd.get(uuid)).setDisplayItem(materialName);
            }
            shopsUnderEdit.remove(shopsUnderAdd.get(uuid));
            shopsUnderAdd.remove(uuid);
            saveShops();
        }
    }

    public void saveShops() {
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
                shopJSON.put("name", shop1.getName());
                shopJSON.put("desc", shop1.getDesc());
                shopJSON.put("owner", shop1.getOwner());
                shopJSON.put("owners", shop1.getOwners());
                shopJSON.put("uuid", shop1.getUuid());
                shopJSON.put("key", shop1.getKey());
                shopJSON.put("loc", shop1.getLoc());
                shopJSON.put("displayItem",shop1.getDisplayItem());

                JSONArray items = new JSONArray();

                shop1.getInv().forEach(itemList -> {
                    JSONObject item = new JSONObject();
                    item.put("name", itemList.item.getType().getKey().getKey().toUpperCase());
                    item.put("price", Integer.valueOf(itemList.price).toString());
                    item.put("qty", itemList.qty);
                    if (itemList.item.getItemMeta().hasDisplayName())
                        item.put("customName", itemList.item.getItemMeta().getDisplayName());
                    if (itemList.extraInfo != null && itemList.extraInfo.size() > 0) {
                        item.put("extraInfo", itemList.extraInfo);
                    }
                    if (itemList.customType != null && itemList.customType.length() > 0) {
                        item.put("customType", itemList.customType);
                    }
                    items.add(item);
                });

                shopJSON.put("items", items);


                shopJSONs.add(shopJSON);
            });

            pendingShops.forEach((s, shop1) -> {
                JSONObject shopJSON = new JSONObject();
                shopJSON.put("name", shop1.getName());
                shopJSON.put("desc", shop1.getDesc());
                shopJSON.put("owner", shop1.getOwner());
                shopJSON.put("owners", shop1.getOwners());
                shopJSON.put("uuid", shop1.getUuid());
                shopJSON.put("key", shop1.getKey());
                shopJSON.put("loc", shop1.getLoc());
                shopJSON.put("displayItem",shop1.getDisplayItem());

                JSONArray items = new JSONArray();

                shop1.getInv().forEach(itemList -> {
                    JSONObject item = new JSONObject();
                    item.put("name", itemList.item.getType().getKey().getKey().toUpperCase());
                    item.put("price", Integer.valueOf(itemList.price).toString());
                    item.put("qty", itemList.qty);
                    if (itemList.item.getItemMeta().hasDisplayName()) {
                        item.put("customName", itemList.item.getItemMeta().getDisplayName());
                    }
                    if (itemList.extraInfo != null && itemList.extraInfo.size() > 0) {
                        item.put("extraInfo", itemList.extraInfo);
                    }
                    if (itemList.customType != null && itemList.customType.length() > 0) {
                        item.put("customType", itemList.customType);
                    }
                    items.add(item);
                });

                shopJSON.put("items", items);


                pShopJSONs.add(shopJSON);
            });

            data.put("shops", shopJSONs);
            data.put("pendingShops", pShopJSONs);

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

            if (shopJSONs.size() > 0) {
                for (Object json : shopJSONs) {
                    try {
                        JSONObject shopJSON = ((JSONObject) json);
                        Shop shop = new Shop(shopJSON.get("name").toString(), shopJSON.get("desc").toString(), shopJSON.get("owner").toString(), shopJSON.get("uuid").toString(), shopJSON.get("key").toString(), shopJSON.get("loc").toString());
                        if (shopJSON.containsKey("owners")) {
                            Map<String, String> owners = new Gson().fromJson(shopJSON.get("owners").toString(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());

                            shop.setOwners(owners);
                        }
                        if(shopJSON.containsKey("displayItem")) {
                            shop.setDisplayItem(shopJSON.get("displayItem").toString());
                        }
                        JSONArray itemsArray = ((JSONArray) shopJSON.get("items"));
                        for (Object o : itemsArray) {
                            try {
                                JSONObject itemJSON = ((JSONObject) o);
                                ItemList item = new ItemList(itemJSON.get("name").toString(), itemJSON.get("qty").toString(), Integer.parseInt(itemJSON.get("price").toString()));
                                if (itemJSON.get("customName") != null)
                                    item.setCustomName(itemJSON.get("customName").toString());
                                if (itemJSON.containsKey("extraInfo") && itemJSON.containsKey("customType")) {
                                    JSONObject extraData = ((JSONObject) itemJSON.get("extraInfo"));
                                    HashMap<String, Object> headInfo = new Gson().fromJson(extraData.toString(), HashMap.class);
                                    item.setExtraInfo(headInfo, itemJSON.get("customType").toString());
                                }
                                shop.addToInv(item);
                            } catch (ClassCastException | NullPointerException e) {
                                if (e instanceof ClassCastException)
                                    logger.error("Malformed shops.json, cannot add item");
                                if (e instanceof NullPointerException)
                                    logger.warn("Key value(s) missing, item won't be created");
                                e.printStackTrace();
                            }
                        }
                        shops.put(shopJSON.get("key").toString(), shop);
                    } catch (ClassCastException | NullPointerException e) {
                        if (e instanceof ClassCastException)
                            logger.error("Malformed shops.json, cannot add shop");
                        if (e instanceof NullPointerException)
                            logger.warn("Key value(s) missing, shop won't be created");
                        e.printStackTrace();
                    }
                }
            } else
                logger.warn("No shops in directory");

            if (pShopJSONs.size() > 0) {
                for (Object json : pShopJSONs) {
                    try {
                        JSONObject shopJSON = ((JSONObject) json);
                        Shop shop = new Shop(shopJSON.get("name").toString(), shopJSON.get("desc").toString(), shopJSON.get("owner").toString(), shopJSON.get("uuid").toString(), shopJSON.get("key").toString(), shopJSON.get("loc").toString());
                        if (shopJSON.containsKey("owners")) {
                            Map<String, String> owners = new Gson().fromJson(shopJSON.get("owners").toString(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());

                            shop.setOwners(owners);
                        }
                        if(shopJSON.containsKey("displayItem")) {
                            shop.setDisplayItem(shopJSON.get("displayItem").toString());
                        }
                        JSONArray itemsArray = ((JSONArray) shopJSON.get("items"));
                        for (Object o : itemsArray) {
                            try {
                                JSONObject itemJSON = ((JSONObject) o);
                                ItemList item = new ItemList(itemJSON.get("name").toString(), itemJSON.get("qty").toString(), Integer.parseInt(itemJSON.get("price").toString()));
                                if (itemJSON.get("customName") != null)
                                    item.setCustomName(itemJSON.get("customName").toString());
                                if (itemJSON.containsKey("extraInfo") && itemJSON.containsKey("customType")) {
                                    JSONObject headData = ((JSONObject) itemJSON.get("extraInfo"));
                                    HashMap<String, Object> headInfo = new Gson().fromJson(headData.toString(), HashMap.class);
                                    item.setExtraInfo(headInfo, itemJSON.get("customType").toString());
                                }
                                shop.addToInv(item);
                            } catch (ClassCastException | NullPointerException e) {
                                if (e instanceof ClassCastException)
                                    logger.error("Malformed shops.json, cannot add item");
                                if (e instanceof NullPointerException)
                                    logger.warn("Key value(s) missing, item won't be created");
                                e.printStackTrace();
                            }
                        }
                        pendingShops.put(shopJSON.get("key").toString(), shop);
                    } catch (ClassCastException | NullPointerException e) {
                        if (e instanceof ClassCastException)
                            logger.warn("Malformed shops.json, cannot add shop");
                        if (e instanceof NullPointerException)
                            logger.warn("Key value(s) missing, shop won't be created");
                        e.printStackTrace();
                    }
                }
            }
            return true;


        } catch (IOException | ParseException | ClassCastException | NullPointerException e) {
            if (e instanceof ParseException || e instanceof ClassCastException)
                plugin.getLogger().severe("Malformed shops.json, cannot initiate shops");
            if (e instanceof NullPointerException)
                plugin.getLogger().warning("Key value(s) missing, shop or item won't be created");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isShopUnderEditOrAdd(String key) {
        return itemToAdd.containsKey(key) || shopsUnderEdit.containsKey(key);
    }

    public int initItemAddition(String uuid, String key, String name, ItemStack itemStack) {
        int res = 1;
        if (!shops.containsKey(key) && !pendingShops.containsKey(key))
            return -1;

        if (shopsUnderAdd.containsKey(uuid) || itemToAdd.containsKey(key)) {
            return 0;
        }

        ItemList item = new ItemList(name, itemStack.getItemMeta());

        if (name.contains("SHULKER_BOX")) {
            if (itemStack.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta im = (BlockStateMeta) itemStack.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulker = (ShulkerBox) im.getBlockState();

                    List<Map<String, Object>> contents = new ArrayList<>();

                    for (int i = 0; i < 27; i++) {
                        ItemStack itemStack1 = shulker.getSnapshotInventory().getItem(i);
                        if (itemStack1 == null || itemStack1.getType() == Material.AIR)
                            continue;

                        Map<String, Object> content = new HashMap<>();
                        content.put("name", itemStack1.getType().getKey().getKey().toUpperCase());
                        content.put("quantity", itemStack1.getAmount());

                        if (itemStack1.getType() == Material.PLAYER_HEAD) {
                            NBTItem nbtItem = new NBTItem(itemStack1);
                            NBTCompound skullOwner = nbtItem.getCompound("SkullOwner");
                            if (skullOwner != null) {
                                Map<String, Object> skullData = new HashMap<>();
                                SkullMeta skullMeta = (SkullMeta) itemStack1.getItemMeta();
                                skullData.put("name", skullMeta.getOwningPlayer().getName());
                                skullData.put("uuid", skullMeta.getOwningPlayer().getUniqueId().toString());
                                skullData.put("value", skullOwner.getCompound("Properties").getCompoundList("textures").get(0).getString("Value"));
                                skullData.put("signature", skullOwner.getCompound("Properties").getCompoundList("textures").get(0).getString("Signature"));
                                content.put("extraInfo", skullData);
                                content.put("customType", "head");
                                item.customType = "head";
                            } else res = 2;
                        } else if (name.contains("POTION")) {
                            PotionMeta potionMeta = (PotionMeta) itemStack1.getItemMeta();
                            Map<String, Object> data = new HashMap<>();
                            PotionData potionType = potionMeta.getBasePotionData();
                            data.put("effect", potionType.getType().getEffectType().getName().toUpperCase());
                            data.put("upgraded", potionType.isUpgraded());
                            data.put("extended", potionType.isExtended());
                            content.put("extraInfo", data);
                            content.put("customType", "potion");
                        } else if (name.contains("FIREWORK_ROCKET")) {
                            FireworkMeta rocketMeta = (FireworkMeta) itemStack1.getItemMeta();
                            List<Object> effects = new ArrayList<>();
                            rocketMeta.getEffects().forEach(fireworkEffect -> {
                                Map<String, Object> effect = new HashMap<>();
                                effect.put("type", fireworkEffect.getType());
                                effect.put("flicker", fireworkEffect.hasFlicker());
                                effect.put("trail", fireworkEffect.hasTrail());
                                List<Integer> colors = new ArrayList<>();
                                List<Integer> fadeColors = new ArrayList<>();
                                fireworkEffect.getColors().forEach(color -> colors.add(color.asRGB()));
                                fireworkEffect.getFadeColors().forEach(fadeColor -> fadeColors.add(fadeColor.asRGB()));
                                effect.put("colors", colors);
                                effect.put("fadeColors", fadeColors);
                                effects.add(effect);
                            });
                            NBTItem nbtItem = new NBTItem(itemStack1);
                            Map<String, Object> fireworksData = new HashMap<>();
                            fireworksData.put("flight", nbtItem.getCompound("Fireworks").getByte("Flight"));
                            fireworksData.put("effects", effects);
                            content.put("extraInfo", fireworksData);
                            content.put("customType", "rocket");
                        } else if (name.contains("TIPPED_ARROW")) {
                            PotionMeta potionMeta = (PotionMeta) itemStack1.getItemMeta();
                            Map<String, Object> data = new HashMap<>();
                            PotionData potionType = potionMeta.getBasePotionData();
                            data.put("effect", potionType.getType().getEffectType().getName().toUpperCase());
                            data.put("upgraded", potionType.isUpgraded());
                            data.put("extended", potionType.isExtended());
                            content.put("extraInfo", data);
                            content.put("customType", "tippedArrow");
                        } else if (name.contains("BANNER")) {
                            BannerMeta bannerMeta = (BannerMeta) itemStack1.getItemMeta();
                            List<Object> patterns = new ArrayList<>();
                            bannerMeta.getPatterns().forEach(pattern -> {
                                Map<String, Object> patternData = new HashMap<>();
                                patternData.put("color", pattern.getColor().name().toUpperCase());
                                patternData.put("type", pattern.getPattern().name().toUpperCase());
                                patterns.add(patternData);
                            });
                            Map<String, Object> info = new HashMap<>();
                            info.put("patterns", patterns);
                            content.put("extraInfo", info);
                            content.put("customType", "banner");
                        }

                        contents.add(content);
                    }

                    item.extraInfo = new HashMap<>();
                    item.extraInfo.put("contents", contents);
                    item.customType = "shulker";

                }
            }
        } else if (itemStack.getType() == Material.PLAYER_HEAD) {
            NBTItem nbtItem = new NBTItem(itemStack);
            NBTCompound skullOwner = nbtItem.getCompound("SkullOwner");
            if (skullOwner != null) {
                Map<String, Object> skullData = new HashMap<>();
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                skullData.put("name", skullMeta.getOwningPlayer().getName());
                skullData.put("uuid", skullMeta.getOwningPlayer().getUniqueId().toString());
                skullData.put("value", skullOwner.getCompound("Properties").getCompoundList("textures").get(0).getString("Value"));
                skullData.put("signature", skullOwner.getCompound("Properties").getCompoundList("textures").get(0).getString("Signature"));
                item.extraInfo = skullData;
                item.customType = "head";
            } else res = 2;
        } else if (name.contains("POTION")) {
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            Map<String, Object> data = new HashMap<>();
            PotionData potionType = potionMeta.getBasePotionData();
            data.put("effect", potionType.getType().getEffectType().getName().toUpperCase());
            data.put("upgraded", potionType.isUpgraded());
            data.put("extended", potionType.isExtended());
            item.extraInfo = data;
            item.customType = "potion";
        } else if (name.contains("FIREWORK_ROCKET")) {
            FireworkMeta rocketMeta = (FireworkMeta) itemStack.getItemMeta();
            List<Object> effects = new ArrayList<>();
            rocketMeta.getEffects().forEach(fireworkEffect -> {
                Map<String, Object> effect = new HashMap<>();
                effect.put("type", fireworkEffect.getType());
                effect.put("flicker", fireworkEffect.hasFlicker());
                effect.put("trail", fireworkEffect.hasTrail());
                List<Integer> colors = new ArrayList<>();
                List<Integer> fadeColors = new ArrayList<>();
                fireworkEffect.getColors().forEach(color -> colors.add(color.asRGB()));
                fireworkEffect.getFadeColors().forEach(fadeColor -> fadeColors.add(fadeColor.asRGB()));
                effect.put("colors", colors);
                effect.put("fadeColors", fadeColors);
                effects.add(effect);
            });
            NBTItem nbtItem = new NBTItem(itemStack);
            Map<String, Object> fireworksData = new HashMap<>();
            fireworksData.put("flight", nbtItem.getCompound("Fireworks").getByte("Flight"));
            fireworksData.put("effects", effects);
            item.extraInfo = fireworksData;
            item.customType = "rocket";
        } else if (name.contains("TIPPED_ARROW")) {
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            Map<String, Object> data = new HashMap<>();
            PotionData potionType = potionMeta.getBasePotionData();
            data.put("effect", potionType.getType().getEffectType().getName().toUpperCase());
            data.put("upgraded", potionType.isUpgraded());
            data.put("extended", potionType.isExtended());
            item.extraInfo = data;
            item.customType = "tippedArrow";
        } else if (name.contains("BANNER")) {
            BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();
            List<Object> patterns = new ArrayList<>();
            bannerMeta.getPatterns().forEach(pattern -> {
                Map<String, Object> patternData = new HashMap<>();
                patternData.put("color", pattern.getColor().name().toUpperCase());
                patternData.put("type", pattern.getPattern().name().toUpperCase());
                patterns.add(patternData);
            });
            item.extraInfo = new HashMap<>();
            item.extraInfo.put("patterns", patterns);
            item.customType = "banner";
        }

        shopsUnderAdd.put(uuid, key);
        itemToAdd.put(key, item);
        return res;
    }

    public void initShopOwnerAddition(String uuid) {
        shopsUnderEdit.put(shopsUnderAdd.get(uuid), 1);
    }

    public int getEditType(String uuid) {
        if (!shopsUnderAdd.containsKey(uuid))
            return -1;

        return shopsUnderEdit.getOrDefault(shopsUnderAdd.get(uuid), 0);
    }

    public void setQty(String qty, String uuid) {
        itemToAdd.get(shopsUnderAdd.get(uuid)).setQty(qty);
    }

    public void setPrice(int price, String uuid) {
        itemToAdd.get(shopsUnderAdd.get(uuid)).setPrice(price);
        if (shops.containsKey(shopsUnderAdd.get(uuid)))
            shops.get(shopsUnderAdd.get(uuid)).addToInv(itemToAdd.get(shopsUnderAdd.get(uuid)));
        else if (pendingShops.containsKey(shopsUnderAdd.get(uuid)))
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
        if (pendingShops.containsKey(key)) {
            shops.put(key, pendingShops.get(key));
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

    public List<Map<String, String>> getShopDetails() {
        List<Map<String, String>> detailsList = new ArrayList<>();
        shops.forEach((s, shop) -> {
            Map<String, String> details = new HashMap<>();
            details.put("name", shop.getName());
            details.put("desc", shop.getDesc());
            details.put("owners", String.join(", ", shop.getOwners().values()));
            details.put("loc", shop.getLoc());
            details.put("displayItem",shop.getDisplayItem());
            details.put("key", shop.getKey());
            detailsList.add(details);
        });
        return detailsList;
    }

    public List<Map<String, String>> getPendingShopDetails() {
        List<Map<String, String>> detailsList = new ArrayList<>();
        pendingShops.forEach((s, shop) -> {
            Map<String, String> details = new HashMap<>();
            details.put("name", shop.getName());
            details.put("desc", shop.getDesc());
            details.put("owners", String.join(", ", shop.getOwners().values()));
            details.put("loc", shop.getLoc());
            details.put("key", shop.getKey());
            details.put("displayItem",shop.getDisplayItem());
            detailsList.add(details);
        });
        return detailsList;
    }

    public List<ItemStack> getShopInv(String key) {
        Shop shop = null;
        if (shops.containsKey(key))
            shop = shops.get(key);
        else if (pendingShops.containsKey(key))
            shop = pendingShops.get(key);

        List<ItemStack> inv = new ArrayList<>();

        if (shop == null) {
            return inv;
        }

        shop.getInv().forEach(itemList -> {
            ItemStack item = itemList.item.clone();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
            lore.add("Right click to find a better deal");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.add(item);

        });
        return inv;
    }

    public void findBetterAlternative(Player player, String key, int pos) {
        ItemList item = shops.get(key).getInv().get(pos);
        String name = item.name;
        double value = 0;
        if(item.price<=0) {
            value = Integer.MAX_VALUE;
        }
        else {
            String[] parts1 = item.qty.split(":");
            if (Integer.parseInt(parts1[0]) > 0)
                value = Double.parseDouble(parts1[0]) * 1728;
            else if (Integer.parseInt(parts1[1]) > 0)
                value = Double.parseDouble(parts1[1]) * 64;
            else if (Integer.parseInt(parts1[2]) > 0)
                value = Double.parseDouble(parts1[2]);
            value /= item.price;
        }
        final boolean[] found = {false};
        double finalValue = value;
        shops.forEach((s, shop) ->
                shop.getInv().forEach(itemList -> {
                    if (itemList.name.equals(name)) {
                        double val = 0;
                        String[] parts = itemList.qty.split(":");
                        if (Integer.parseInt(parts[0]) > 0)
                            val = Double.parseDouble(parts[0]) * 1728;
                        else if (Integer.parseInt(parts[1]) > 0)
                            val = Double.parseDouble(parts[1]) * 64;
                        else if (Integer.parseInt(parts[2]) > 0)
                            val = Double.parseDouble(parts[2]);
                        val /= itemList.price;

                        if (val > finalValue) {
                            player.sendMessage(ChatColor.GOLD + shop.getName() + ChatColor.WHITE + " has a better deal: " + itemList.getItem().getLore().get(0));
                            found[0] = true;
                        }
                    }
                })
        );
        if (!found[0]) {
            player.sendMessage("No better alternatives found");
        }
    }

    public String getShopName(String key) {
        return shops.containsKey(key) ? shops.get(key).getName() : pendingShops.containsKey(key) ? pendingShops.get(key).getName() : "";
    }

    public List<Map<String, String>> getRefinedShopsByName(String searchKey) {
        List<Map<String, String>> detailsList = new ArrayList<>();
        shops.forEach((s, shop) -> {

            if (shop.getName().toLowerCase().trim().contains(searchKey.toLowerCase().trim())) {

                Map<String, String> details = new HashMap<>();
                details.put("name", shop.getName());
                details.put("desc", shop.getDesc());
                details.put("owners", String.join(", ", shop.getOwners().values()));
                details.put("loc", shop.getLoc());
                details.put("key", shop.getKey());
                details.put("displayItem",shop.getDisplayItem());
                detailsList.add(details);
            }
        });
        return detailsList;
    }

    public List<ItemStack> getMatchingItems(String key, String itemName) {
        Shop shop = shops.getOrDefault(key, pendingShops.get(key));
        if(shop == null)
            return null;
        List<ItemStack> items = new ArrayList<>();
        shop.getInv().forEach(itemList -> {
            if (itemList.name.equalsIgnoreCase(itemName))
                items.add(itemList.item);
        });
        return items;
    }

    public void removeMatchingItems(String key, String itemName) {
        Shop shop = shops.getOrDefault(key, pendingShops.get(key));
        shop.setInv(shop.getInv().stream().filter(itemList -> !itemList.name.equals(itemName)).collect(Collectors.toList()));
    }

    public void removeItem(String key, ItemStack item) {
        Shop shop = shops.getOrDefault(key, pendingShops.get(key));
        shop.setInv(shop.getInv().stream().filter(itemList -> itemList.getItem().getType() != item.getType() || !item.getItemMeta().getLore().get(0).equals(itemList.item.getItemMeta().getLore().get(0))).collect(Collectors.toList()));
    }

    public List<Map<String, String>> getRefinedShopsByPlayer(String searchKey) {
        List<Map<String, String>> detailsList = new ArrayList<>();
        shops.forEach((s, shop) -> {

            boolean[] contains = {false};
            shop.getOwners().values().forEach(s1 -> {
                if (s1.toLowerCase().trim().contains(searchKey.toLowerCase().trim())) {
                    contains[0] = true;
                }
            });
            if (contains[0]) {
                Map<String, String> details = new HashMap<>();
                details.put("name", shop.getName());
                details.put("desc", shop.getDesc());
                details.put("owners", String.join(", ", shop.getOwners().values()));
                details.put("loc", shop.getLoc());
                details.put("key", shop.getKey());
                details.put("displayItem",shop.getDisplayItem());
                detailsList.add(details);
            }
        });
        return detailsList;
    }

    public Map<String, Object> findItem(String searchKey) {
        List<ItemStack> items = new ArrayList<>();
        List<Map<String,String>> shopIds = new ArrayList<>();
        shops.forEach((s, shop) -> {
            List<ItemList> inv = shop.getInv();
            inv.forEach(itemList -> {
                if (itemList.name.replace('_', ' ').toLowerCase().trim().contains(searchKey.toLowerCase().trim())) {
                    ItemStack itemToAdd = itemList.item.clone();
                    ItemMeta meta = itemToAdd.getItemMeta();
                    List<String> lore = meta.getLore();
                    lore.add(ChatColor.GREEN + "From " + shop.getName());
                    lore.add(ChatColor.YELLOW + "Right-click to view this shop");
                    meta.setLore(lore);
                    itemToAdd.setItemMeta(meta);
                    items.add(itemToAdd);
                    Map<String,String> shopData = new HashMap<>();
                    shopData.put("name",shop.getName());
                    shopData.put("id",shop.getKey());
                    shopIds.add(shopData);
                } else if (itemList.customName.length() > 0 && itemList.customName.toLowerCase().trim().contains(searchKey.toLowerCase().trim())) {
                    ItemStack itemToAdd = itemList.item.clone();
                    ItemMeta meta = itemToAdd.getItemMeta();
                    List<String> lore = meta.getLore();
                    lore.add(ChatColor.GREEN + "From " + shop.getName());
                    lore.add(ChatColor.YELLOW + "Right-click to view this shop");
                    meta.setLore(lore);
                    itemToAdd.setItemMeta(meta);
                    items.add(itemToAdd);
                    Map<String,String> shopData = new HashMap<>();
                    shopData.put("name",shop.getName());
                    shopData.put("id",shop.getKey());
                    shopIds.add(shopData);
                }
            });
        });
        Map<String,Object> searchResults = new HashMap<>();
        searchResults.put("items",items);
        searchResults.put("shops",shopIds);
        return searchResults;
    }

    private void addShopCountMetric() {
        plugin.getMetrics().addCustomChart(new Metrics.SingleLineChart("shop_items", () -> shops.values().stream().mapToInt(shop -> shop.getInv().size()).sum()));
        plugin.getMetrics().addCustomChart(new Metrics.SingleLineChart("shops", shops::size));
    }

}


