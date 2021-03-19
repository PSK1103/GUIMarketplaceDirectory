package me.PSK1103.GUIMarketplaceDirectory.guimd;

import me.PSK1103.GUIMarketplaceDirectory.eventhandlers.ItemEvents;
import me.PSK1103.GUIMarketplaceDirectory.eventhandlers.ShopEvents;
import me.PSK1103.GUIMarketplaceDirectory.utils.Config;
import me.PSK1103.GUIMarketplaceDirectory.utils.GUI;
import me.PSK1103.GUIMarketplaceDirectory.guimd.GUIMarketplaceCommands;
import me.PSK1103.GUIMarketplaceDirectory.utils.Metrics;
import me.PSK1103.GUIMarketplaceDirectory.utils.ShopRepo;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;

public class GUIMarketplaceDirectory extends JavaPlugin {

    File shops = null;
    private ShopRepo shopRepo;
    private Config config;
    public GUI gui;
    private File customConfigFile;
    private FileConfiguration customConfig;
    private Metrics metrics;
    private Logger logger;

    private static final int pluginId = 9879;

    @Override
    public void onEnable() {
        logger = getSLF4JLogger();
        customConfig = null;
        saveDefaultConfig();
        config = new Config(this);
        if(config.bstatsEnabled())
            metrics = new Metrics(this, pluginId);
        this.shopRepo = new ShopRepo(this);
        this.gui = new GUI(this);
        getServer().getPluginManager().registerEvents(new ShopEvents(this),this);
        getServer().getPluginManager().registerEvents(new ItemEvents(this),this);
        getCommand("GUIMD").setExecutor(new GUIMarketplaceCommands(this));
    }

    @Override
    public void onDisable() {
        shopRepo.saveShops();
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
                logger.error("Unable to initialise shops", e);
                e.printStackTrace();
            }
        }
        return shops;
    }

    public ShopRepo getShopRepo() {
        return shopRepo;
    }

    public Metrics getMetrics(){
        return metrics;
    }

    public Config getCustomConfig() {
        return config;
    }

}
