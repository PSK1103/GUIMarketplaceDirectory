package me.PSK1103.GUIMarketplaceDirectory.utils;

import me.PSK1103.GUIMarketplaceDirectory.database.DBConfig;
import me.PSK1103.GUIMarketplaceDirectory.GUIMarketplaceDirectory;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class Config {

    private final GUIMarketplaceDirectory plugin;
    private final Logger logger;

    private String defaultShopNameColor;
    private String defaultShopDescColor;
    private String defaultShopOwnerColor;
    private String defaultShopLocColor;

    private boolean moderateDirectory;

    private int shopDetailsLengthLimit;

    private boolean multiOwner;
    private boolean allowAddingOfflinePLayer;

    private boolean filterAlternatives;

    private boolean use_db;

    private String db;

    private String DB_TYPE;
    private String DB_HOST;
    private String DB_PORT;
    private String DB_DATABASE;
    private String DB_USERNAME;
    private String DB_PASSWORD;
    private String DB_PREFIX;

    private boolean useCoreProtect;
    private int defaultLookupRadius;
    private String lookupTime;

    private boolean enableCustomApprovalMessage;
    private String customApprovalMessage;

    private boolean enableBstats;

    public Config(@NotNull GUIMarketplaceDirectory plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger();
        try {
            matchConfigParams();
        }
        catch (IOException | InvalidConfigurationException e) {
            logger.severe(e.getMessage());
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

            shopDetailsLengthLimit = configFile.getInt("shop-details-length-limit",defaultConfig.getInt("shop-details-length-limit",-1));

            multiOwner = configFile.getBoolean("multi-owner",defaultConfig.getBoolean("multi-owner"));
            allowAddingOfflinePLayer = configFile.getBoolean("allow-add-offline-players",defaultConfig.getBoolean("allow-add-offline-players"));

            filterAlternatives = configFile.getBoolean("filter-alternatives-list",defaultConfig.getBoolean("filter-alternatives-list"));

            use_db = configFile.getBoolean("use-db", defaultConfig.getBoolean("use-db"));
            db = configFile.getString("db", defaultConfig.getString("db"));

            DB_HOST = configFile.getString("mysql-host", defaultConfig.getString("mysql-host"));
            DB_PORT = configFile.getString("mysql-port", defaultConfig.getString("mysql-port"));
            DB_DATABASE = configFile.getString("mysql-database", defaultConfig.getString("mysql-database"));
            DB_USERNAME = configFile.getString("mysql-username", defaultConfig.getString("mysql-username"));
            DB_PASSWORD = configFile.getString("mysql-password", defaultConfig.getString("mysql-password"));
            DB_PREFIX = configFile.getString("table-prefix", defaultConfig.getString("table-prefix"));

            useCoreProtect = configFile.getBoolean("use-coreprotect",defaultConfig.getBoolean("use-coreprotect", false));
            defaultLookupRadius = configFile.getInt("default-lookup-radius",defaultConfig.getInt("default-lookup-radius", 20));
            lookupTime = configFile.getString("lookup-time", defaultConfig.getString("lookup-time","7d"));

            enableCustomApprovalMessage = configFile.getBoolean("enable-custom-approval-message",defaultConfig.getBoolean("enable-custom-approval-message"));
            customApprovalMessage = configFile.getString("custom-approval-message",defaultConfig.getString("custom-approval-message"));

            enableBstats = configFile.getBoolean("enable-bstats", defaultConfig.getBoolean("enable-bstats"));

        } catch (IOException | InvalidConfigurationException e) {
            logger.severe("Failed to parse custom config");
            logger.warning("Reverting to default config");
            loadDefaultConfig();
        }
    }

    private void loadDefaultConfig() {
        logger.info("Loading default config");
        final FileConfiguration defaultConfig = new YamlConfiguration();

        try {
            defaultConfig.load(new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("config.yml"))));

            defaultShopNameColor = defaultConfig.getString("default-shop-name-color");
            defaultShopDescColor = defaultConfig.getString("default-shop-desc-color");
            defaultShopOwnerColor = defaultConfig.getString("default-shop-owner-color");
            defaultShopLocColor = defaultConfig.getString("default-shop-loc-color");

            moderateDirectory = defaultConfig.getBoolean("moderate-directory");

            filterAlternatives = defaultConfig.getBoolean("filter-alternatives-list");

            shopDetailsLengthLimit = defaultConfig.getInt("shop-details-length-limit",-1);

            multiOwner = defaultConfig.getBoolean("multi-owner");
            allowAddingOfflinePLayer = defaultConfig.getBoolean("allow-add-offline-players");

            filterAlternatives = defaultConfig.getBoolean("filter-alternatives-list");

            use_db = defaultConfig.getBoolean("use-db");
            db = defaultConfig.getString("db");

            DB_HOST = defaultConfig.getString("mysql-host");
            DB_PORT = defaultConfig.getString("mysql-port");
            DB_DATABASE = defaultConfig.getString("mysql-database");
            DB_USERNAME = defaultConfig.getString("mysql-username");
            DB_PASSWORD = defaultConfig.getString("mysql-password");
            DB_PREFIX = defaultConfig.getString("table-prefix", "guimd");

            useCoreProtect = defaultConfig.getBoolean("use-coreprotect", false);
            defaultLookupRadius = defaultConfig.getInt("default-lookup-radius", 20);
            lookupTime = defaultConfig.getString("lookup-time","7d");

            enableCustomApprovalMessage = defaultConfig.getBoolean("enable-custom-approval-message");
            customApprovalMessage = defaultConfig.getString("custom-approval-message");

            enableBstats = defaultConfig.getBoolean("enable-bstats");

        } catch (IOException | InvalidConfigurationException e) {
            logger.severe("Failed to parse custom config");
            logger.warning("Reverting to default config");
            loadDefaultConfig();
        }

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
            logger.warning("Custom config is missing some parameters\nTrying to reconstruct config.yml keeping current config values");
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
            logger.severe(e.getMessage());
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

    public int getShopDetailsLengthLimit() {
        return shopDetailsLengthLimit;
    }

    public boolean multiOwnerEnabled() {
        return multiOwner;
    }

    public boolean addingOfflinePlayerAllowed() {
        return allowAddingOfflinePLayer;
    }

    public boolean customApprovalMessageEnabled() {
        return enableCustomApprovalMessage;
    }

    public String getCustomApprovalMessage() {
        return customApprovalMessage;
    }

    public boolean filterAlternatives() {
        return filterAlternatives;
    }

    public boolean usingDB() {
        return use_db;
    }

    public void initDB() {
        DBConfig.setDB(DB_TYPE, DB_PREFIX, DB_HOST, DB_PORT, DB_DATABASE, DB_USERNAME, DB_PASSWORD);
    }

    public Map<String,String> getMySQLDetails() {
        Map<String,String> details = new HashMap<>();
        details.put("mysql-host",DB_HOST);
        details.put("mysql-port",DB_PORT);
        details.put("mysql-database",DB_DATABASE);
        details.put("mysql-username",DB_USERNAME);
        details.put("mysql-password",DB_PASSWORD);
        details.put("table-prefix",DB_PREFIX);
        details.put("db", db);
        return  details;
    }

    public boolean useCoreProtect() {
        return useCoreProtect;
    }

    public int getDefaultLookupRadius() {
        return defaultLookupRadius;
    }

    public String getLookupTime() {
        return lookupTime;
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
