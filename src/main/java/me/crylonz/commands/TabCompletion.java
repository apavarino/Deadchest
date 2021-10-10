package me.crylonz.commands;

import me.crylonz.Permission;
import me.crylonz.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabCompletion implements TabCompleter {

    private final List<String> list = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        list.clear();
        if (cmd.getName().equalsIgnoreCase("dc")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length == 1) {
                    if (player.hasPermission(Permission.ADMIN.label)) {
                        list.add("reload");
                        list.add("removeinfinite");
                        list.add("removeall");
                        list.add("repair");
                    }

                    if (PermissionUtils.hasAdminOrOneOf(player, new Permission[]{Permission.REMOVE_OTHER, Permission.REMOVE_OWN})) {
                        list.add("remove");
                    }

                    if (player.hasPermission(Permission.ADMIN.label) || player.hasPermission(Permission.GIVEBACK.label)) {
                        list.add("giveBack");
                    }

                    if (PermissionUtils.hasAdminOrOneOf(player, new Permission[]{Permission.LIST_OWN, Permission.LIST_OTHER})) {
                        list.add("list");
                    }
                }

                if (args.length == 2) {
                    if (args[0].equals("remove")) {
                        if (player.hasPermission(Permission.ADMIN.label) || player.hasPermission(Permission.REMOVE_OTHER.label)) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                list.add(p.getName());
                            }
                        }
                    }

                    if (args[0].equals("list")) {
                        if (player.hasPermission(Permission.ADMIN.label) || player.hasPermission(Permission.LIST_OTHER.label)) {
                            list.add("all");
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                list.add(p.getName());
                            }
                        }
                    }
                    if (args[0].equals("giveBack")) {
                        if (player.hasPermission(Permission.ADMIN.label) || player.hasPermission(Permission.GIVEBACK.label)) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                list.add(p.getName());
                            }
                        }
                    }

                }
            }
        }
        return list;
    }
}