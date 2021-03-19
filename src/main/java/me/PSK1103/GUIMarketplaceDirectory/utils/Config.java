package me.PSK1103.GUIMarketplaceDirectory.utils;

import me.PSK1103.GUIMarketplaceDirectory.guimd.GUIMarketplaceDirectory;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;

public class Config {

    private final GUIMarketplaceDirectory plugin;
    private final Logger logger;

    private String defaultShopNameColor;
    private String defaultShopDescColor;
    private String defaultShopOwnerColor;
    private String defaultShopLocColor;

    private boolean moderateDirectory;

    private boolean multiOwner;

    private boolean enableCustomApprovalMessage;
    private String customApprovalMessage;

    private boolean enableBstats;

    public Config(@NotNull GUIMarketplaceDirectory plugin) {
        this.plugin = plugin;
        logger = plugin.getSLF4JLogger();
        try {
            matchConfigParams();
        }
        catch (IOException | InvalidConfigurationException e) {
            logger.error(e.getMessage());
        }
        if (new File(plugin.getDataFolder(), "config.yml").exists()) loadCustomConfig();
        else loadDefaultConfig();
    }

    private void loadCustomConfig() {
        logger.info("Loading custom config");
        final FileConfiguration configFile = new YamlConfiguration();
        final FileConfiguration defaultConfig = new YamlConfiguration();
        try {
            defaultConfig.load(new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("config.yml"))));
            configFile.load(new File(plugin.getDataFolder(), "config.yml"));

            defaultShopNameColor = configFile.getString("default-shop-name-color",defaultConfig.getString("default-shop-name-color"));
            defaultShopDescColor = configFile.getString("default-shop-desc-color",defaultConfig.getString("default-shop-desc-color"));
            defaultShopOwnerColor = configFile.getString("default-shop-owner-color",defaultConfig.getString("default-shop-owner-color"));
            defaultShopLocColor = configFile.getString("default-shop-loc-color",defaultConfig.getString("default-shop-loc-color"));

            moderateDirectory = configFile.getBoolean("moderate-directory",defaultConfig.getBoolean("moderate-directory"));

            multiOwner = configFile.getBoolean("multi-owner",defaultConfig.getBoolean("multi-owner"));

            enableCustomApprovalMessage = configFile.getBoolean("enable-custom-approval-message",defaultConfig.getBoolean("enable-custom-approval-message"));
            customApprovalMessage = configFile.getString("custom-approval-message",defaultConfig.getString("custom-approval-message"));

            enableBstats = configFile.getBoolean("enable-bstats", defaultConfig.getBoolean("enable-bstats"));

        } catch (IOException | InvalidConfigurationException e) {
            logger.error("Failed to parse custom config");
            logger.warn("Reverting to default config");
            loadDefaultConfig();
        }
    }

    private void loadDefaultConfig() {
        logger.info("Loading default config");
        final FileConfiguration defaultConfig = plugin.getConfig();

        defaultShopNameColor = defaultConfig.getString("default-shop-name-color");
        defaultShopDescColor = defaultConfig.getString("default-shop-desc-color");
        defaultShopOwnerColor = defaultConfig.getString("default-shop-owner-color");
        defaultShopLocColor = defaultConfig.getString("default-shop-loc-color");

        moderateDirectory = defaultConfig.getBoolean("moderate-directory");

        multiOwner = defaultConfig.getBoolean("multi-owner");

        enableCustomApprovalMessage = defaultConfig.getBoolean("enable-custom-approval-message");
        customApprovalMessage = defaultConfig.getString("custom-approval-message");

        enableBstats = defaultConfig.getBoolean("enable-bstats");

    }

    private void matchConfigParams() throws IOException, InvalidConfigurationException {
        File configFile = new File(plugin.getDataFolder(),"config.yml");
        if(!configFile.exists()) {
            plugin.saveDefaultConfig();
            return;
        }

        final FileConfiguration customConfig = new YamlConfiguration();
        customConfig.load(configFile);
        final FileConfiguration defaultConfig = new YamlConfiguration();
        defaultConfig.load(new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("config.yml"))));

        Map<String,Object> customValues = customConfig.getValues(true);

        List<String> customParams = new ArrayList<>(customValues.keySet());
        List<String> defaultParams = new ArrayList<>(defaultConfig.getValues(true).keySet());


        Collections.sort(customParams);
        Collections.sort(defaultParams);

        if(customParams.equals(defaultParams)) {
            logger.info("Custom config is up-to-date");
        }
        else {
            logger.warn("Custom config is missing some parameters\nTrying to reconstruct config.yml keeping current config values");
            InputStreamReader isr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("config.yml"));
            BufferedReader br = new BufferedReader(isr);
            List<String> lines = new ArrayList<>();
            br.lines().forEach(line -> {
                boolean found = false;
                for(String k : customParams) {
                    if(line.matches("\\s*" + k + ".*")) {
                        found = true;
                        lines.add(line.substring(0,line.indexOf(k) + k.length() + 1) + " " + customValues.get(k).toString());
                        break;
                    }
                }
                if(!found)
                    lines.add(line);

            });
            isr.close();
            br.close();

            FileWriter fw = new FileWriter(configFile);
            BufferedWriter bw = new BufferedWriter(fw);
            for(String line : lines) {
                bw.write(line);
                bw.write("\n");
            }
            bw.flush();
            bw.close();
        }
    }

    public void reloadConfig() {
        try {
            matchConfigParams();
        }
        catch (IOException | InvalidConfigurationException e) {
            logger.error(e.getMessage());
        }

        loadCustomConfig();
    }

    public String getDefaultShopNameColor() {
        return defaultShopNameColor;
    }

    public String getDefaultShopDescColor() {
        return defaultShopDescColor;
    }

    public String getDefaultShopOwnerColor() {
        return defaultShopOwnerColor;
    }

    public String getDefaultShopLocColor() {
        return defaultShopLocColor;
    }

    public boolean directoryModerationEnabled() {
        return moderateDirectory;
    }

    public boolean multiOwnerEnabled() {
        return multiOwner;
    }

    public boolean customApprovalMessageEnabled() {
        return enableCustomApprovalMessage;
    }

    public String getCustomApprovalMessage() {
        return customApprovalMessage;
    }

    public boolean bstatsEnabled() {
        return enableBstats;
    }

    @Override
    public String toString() {
        return "Config{" +
                "default-shop-name-color='" + defaultShopNameColor + "'\n" +
                ", default-shop-desc-color='" + defaultShopDescColor + "'\n" +
                ", default-shop-owner-color='" + defaultShopOwnerColor + "'\n" +
                ", default-shop-loc-color='" + defaultShopLocColor + "'\n" +
                ", moderate-directory=" + moderateDirectory + "'\n" +
                ", multi-owner=" + multiOwner + "\n" +
                ", enable-custom-approval-message=" + enableCustomApprovalMessage + "\n" +
                ", custom-approval-message='" + customApprovalMessage + "'\n" +
                ", enable-bstats=" + enableBstats + "\n" +
                '}';
    }
}
