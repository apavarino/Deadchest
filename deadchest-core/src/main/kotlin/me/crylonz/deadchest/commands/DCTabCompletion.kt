package me.crylonz.deadchest.commands

import me.crylonz.Permission
import me.crylonz.Permission.ADMIN
import me.crylonz.deadchest.PermissionUtils
import me.crylonz.deadchest.PermissionUtils.hasAdminOr
import me.crylonz.deadchest.PermissionUtils.hasAdminOrOneOf
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class DCTabCompletion : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<String>
    ): List<String> {

        val list: MutableList<String> = mutableListOf()
        if (cmd.name.equals("dc", ignoreCase = true)) {
            if (sender is Player) {
                if (args.size == 1) {
                    if (sender.hasPermission(ADMIN.label)) {
                        list.add("reload")
                        list.add("removeinfinite")
                        list.add("removeall")
                        list.add("repair")
                    }
                    if (hasAdminOrOneOf(sender, PermissionUtils.LIST_ALL)) {
                        list.add("remove")
                    }
                    if (hasAdminOr(sender, Permission.GIVEBACK)) {
                        list.add("giveBack")
                    }
                    if (hasAdminOrOneOf(sender, PermissionUtils.REMOVE_ALL)) {
                        list.add("list")
                    }
                }
                if (args.size == 2) {
                    if (args[0] == "remove") {
                        if (hasAdminOr(sender, Permission.REMOVE_OTHER)) {
                            for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                                list.add(onlinePlayer.name)
                            }
                        }
                    }
                    if (args[0] == "repair" && sender.hasPermission(ADMIN.label)) {
                        list.add("force")
                    }
                    if (args[0] == "list") {
                        if (hasAdminOr(sender, Permission.LIST_OTHER)) {
                            list.add("all")
                            for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                                list.add(onlinePlayer.name)
                            }
                        }
                    }
                    if (args[0] == "giveback") {
                        if (hasAdminOr(sender, Permission.GIVEBACK)) {
                            for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                                list.add(onlinePlayer.name)
                            }
                        }
                    }
                }
            }
        }
        return list
    }
}