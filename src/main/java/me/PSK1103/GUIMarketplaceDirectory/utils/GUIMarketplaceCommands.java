package me.PSK1103.GUIMarketplaceDirectory.utils;

import me.PSK1103.GUIMarketplaceDirectory.GUIMarketplaceDirectory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GUIMarketplaceCommands implements TabExecutor {

    final GUIMarketplaceDirectory plugin;

    public GUIMarketplaceCommands(GUIMarketplaceDirectory plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(label.equals("guimarketplacedirectory") || label.equals("gmd") || label.equals("guimd")) {
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
                                plugin.gui.openShopDirectoryModerator(((Player) commandSender),2);
                                return true;

                            case "recover":
                                plugin.gui.openShopDirectoryModerator((Player) commandSender,3);
                                return true;
                        }
                        break;
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(alias.equals("gmd") || alias.equals("guimd") || alias.equals("guimarketplacedirectory")) {
            List<String> hints = new ArrayList<>();

            if(args.length == 1) {
                if(args[0].length() == 0) {
                    if(commandSender.hasPermission("GUIMD.moderate"))
                        hints.add("moderate");
                    hints.add("search");
                }
                else {
                    if ("moderate".contains(args[0])) {
                        if (args[0].equals("moderate")) {
                            hints.add("pending");
                            hints.add("recover");
                            hints.add("review");
                        } else
                            hints.add("moderate");
                    } else if ("search".contains(args[0])) {
                        if (args[0].equals("search")) {
                            hints.add("item");
                            hints.add("player");
                            hints.add("shop");
                        } else
                            hints.add("search");
                    }
                }
            }

            if(args.length == 2) {
                if(args[0].equals("moderate")) {
                    if(args[1].length() == 0) {
                        hints.add("pending");
                        hints.add("recover");
                        hints.add("review");
                    }
                    else {
                        if("pending".contains(args[1])) {
                            if(!args[1].equals("pending")) {
                                hints.add("pending");
                            }
                        }
                        if("recover".contains(args[1])) {
                            if(!args[1].equals("recover")) {
                                hints.add("recover");
                            }
                        }
                        if("review".contains(args[1])) {
                            if(!args[1].equals("review")) {
                                hints.add("review");
                            }
                        }
                    }
                }

                if(args[0].equals("search")) {
                    if(args[1].length() == 0) {
                        hints.add("item");
                        hints.add("player");
                        hints.add("shop");
                    }

                    else {
                        if("item".contains(args[1])) {
                            if(!args[1].equals("item")) {
                                hints.add("item");
                            }
                        }
                        if("player".contains(args[1])) {
                            if(!args[1].equals("player")) {
                                hints.add("player");
                            }
                        }
                        if("shop".contains(args[1])) {
                            if(!args[1].equals("shop")) {
                                hints.add("shop");
                            }
                        }
                    }
                }
            }

            if(args.length == 0) {
                if(commandSender.hasPermission("GUIMD.moderate"))
                    hints.add("moderate");
                hints.add("search");
            }

            return hints;

        }
        return null;
    }
}
