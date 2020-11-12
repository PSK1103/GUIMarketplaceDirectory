package me.PSK1103.GUIMarketplaceDirectory.utils;

import me.PSK1103.GUIMarketplaceDirectory.GUIMarketplaceDirectory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GUIMarketplaceCommands implements CommandExecutor {

    GUIMarketplaceDirectory plugin;

    public GUIMarketplaceCommands(GUIMarketplaceDirectory plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(label.equals("guimarketplacedirectory") || label.equals("gmd")) {
            if(args.length >= 3) {
                if(args[0].equals("search")) {
                    switch (args[1]) {
                        case "shop":
                        case "s":
                            StringBuilder key = new StringBuilder();
                            for(int i = 2;i<args.length-1;i++) {
                                key.append(args[i]);
                                key.append(' ');
                            }
                            key.append(args[args.length-1]);
                            plugin.gui.openRefinedShopPageByName(((Player) commandSender), key.toString());
                            return true;

                        case "item":
                        case "i":
                            StringBuilder key1 = new StringBuilder();
                            for(int i = 2;i<args.length-1;i++) {
                                key1.append(args[i]);
                                key1.append(' ');
                            }
                            key1.append(args[args.length-1]);
                            plugin.gui.openRefinedItemInventory(((Player) commandSender), key1.toString());
                            return true;

                        case "player":
                        case "p":
                            plugin.gui.openRefinedShopPageByPlayer(((Player) commandSender), args[2]);
                            return true;
                    }
                }
            }
            if(args.length == 2) {
                switch (args[0]) {
                    case "moderate":
                    case "m":
                        if(!commandSender.hasPermission("GUIMD.moderate")) {
                            commandSender.sendMessage(ChatColor.RED + "You do not have permissions to moderate the marketplace directory");
                            return true;
                        }
                        if(!plugin.getCustomConfig().getBoolean("moderate-directory",false)) {
                            commandSender.sendMessage(ChatColor.RED + "This feature is not enabled! Enable it from the config");
                            return true;
                        }
                        switch (args[1]) {
                            case "pending":
                            case "p":
                                plugin.gui.openShopDirectoryModerator(((Player) commandSender),1);
                                return true;

                            case "review":
                            case "r":
                                plugin.gui.openShopDirectoryModerator(((Player) commandSender),2);
                                return true;
                        }
                        break;
                }
            }
        }

        return false;
    }
}
