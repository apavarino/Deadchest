package me.crylonz.deadchest.commands;

import me.crylonz.deadchest.Permission;
import me.crylonz.deadchest.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DCTabCompletion implements TabCompleter {

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

                    if (PermissionUtils.hasAdminOrOneOf(player, PermissionUtils.LIST_ALL)) {
                        list.add("remove");
                    }

                    if (PermissionUtils.hasAdminOr(player, Permission.GIVEBACK)) {
                        list.add("giveBack");
                    }

                    if (PermissionUtils.hasAdminOrOneOf(player, PermissionUtils.REMOVE_ALL)) {
                        list.add("list");
                    }
                }

                if (args.length == 2) {
                    if (args[0].equals("remove")) {
                        if (PermissionUtils.hasAdminOr(player, Permission.REMOVE_OTHER)) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                list.add(p.getName());
                            }
                        }
                    }
                    if (args[0].equals("repair") && player.hasPermission(Permission.ADMIN.label)) {
                        list.add("force");
                    }
                    if (args[0].equals("list")) {
                        if (PermissionUtils.hasAdminOr(player, Permission.LIST_OTHER)) {
                            list.add("all");
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                list.add(p.getName());
                            }
                        }
                    }
                    if (args[0].equals("giveback")) {
                        if (PermissionUtils.hasAdminOr(player, Permission.GIVEBACK)) {
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