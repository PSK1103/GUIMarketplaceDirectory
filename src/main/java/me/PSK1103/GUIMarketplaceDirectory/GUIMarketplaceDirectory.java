package me.PSK1103.GUIMarketplaceDirectory;

import me.PSK1103.GUIMarketplaceDirectory.EventHandler.ItemEvents;
import me.PSK1103.GUIMarketplaceDirectory.EventHandler.ShopEvents;
import me.PSK1103.GUIMarketplaceDirectory.utils.GUI;
import me.PSK1103.GUIMarketplaceDirectory.utils.GUIMarketplaceCommands;
import me.PSK1103.GUIMarketplaceDirectory.utils.ShopRepo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.io.*;
import java.util.EventListener;

public class GUIMarketplaceDirectory extends JavaPlugin {

    File shops = null;
    private ShopRepo shopRepo;
    public GUI gui;
    private File customConfigFile;
    private FileConfiguration customConfig;

    @Override
    public void onEnable() {
        customConfig = null;
        saveDefaultConfig();
        this.shopRepo = new ShopRepo(this);
        this.gui = new GUI(this);
        getServer().getPluginManager().registerEvents(new ShopEvents(this),this);
        getServer().getPluginManager().registerEvents(new ItemEvents(this),this);
        getCommand("GUIMD").setExecutor(new GUIMarketplaceCommands(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Nullable
    public File getShops() {

        if(shops!=null)
            return shops;

        shops = new File(getDataFolder(),"shops.json");

        if(!shops.exists()) {
            try {
                getDataFolder().mkdir();
                shops.createNewFile();
                JSONObject init = new JSONObject();
                init.put("shops",new JSONArray());
                init.put("pendingShops",new JSONArray());
                FileWriter writer = new FileWriter(shops);
                writer.write(init.toJSONString());
                writer.close();

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return shops;
    }

    public ShopRepo getShopRepo() {
        return shopRepo;
    }

    public FileConfiguration getCustomConfig() {

        if(customConfig!=null)
            return customConfig;

        customConfigFile = new File(getDataFolder(), "config.yml");
        if (!customConfigFile.exists()) {
            return getConfig();
        }
        customConfig= new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return customConfig;
    }
}
