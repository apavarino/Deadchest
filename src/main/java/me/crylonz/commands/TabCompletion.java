package me.crylonz.commands;

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
                    if (player.hasPermission("deadchest.admin")) {
                        list.add("reload");
                        list.add("removeinfinite");
                        list.add("removeall");
                        list.add("repair");
                    }

                    if (player.hasPermission("deadchest.admin") || player.hasPermission("deadchest.remove.own")
                            && player.hasPermission("deadchest.remove.other")) {
                        list.add("remove");
                    }

                    if (player.hasPermission("deadchest.admin") || player.hasPermission("deadchest.giveback")) {
                        list.add("giveBack");
                    }

                    if (player.hasPermission("deadchest.admin") || player.hasPermission("deadchest.list.own") ||
                            player.hasPermission("deadchest.list.other")) {
                        list.add("list");
                    }
                }

                if (args.length == 2) {
                    if (args[0].equals("remove")) {
                        if (player.hasPermission("deadchest.admin") || player.hasPermission("deadchest.remove.other")) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                list.add(p.getName());
                            }
                        }
                    }

                    if (args[0].equals("list")) {
                        if (player.hasPermission("deadchest.admin") || player.hasPermission("deadchest.list.other")) {
                            list.add("all");
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                list.add(p.getName());
                            }
                        }
                    }
                    if (args[0].equals("giveBack")) {
                        if (player.hasPermission("deadchest.admin") || player.hasPermission("deadchest.giveBack")) {
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