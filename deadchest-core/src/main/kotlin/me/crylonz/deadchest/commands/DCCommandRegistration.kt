package me.crylonz.deadchest.commands

import me.crylonz.deadchest.DeadChest
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


abstract class DCCommandRegistration(protected val plugin: DeadChest) {

    // CommandSender params
    protected lateinit var sender: CommandSender
    protected var args: List<String> = emptyList()
    protected var player: Player? = null

    var isCommandSucceed: MutableList<Boolean> = mutableListOf()

    /**
     * Need to be call at then beginning of onCommand to set up the context
     *
     * @param sender sender from onCommand
     * @param args   args from onCommand
     */
    fun register(sender: CommandSender, args: List<String>) {
        this.sender = sender
        this.args = args
        isCommandSucceed = mutableListOf()

        if (sender is Player) {
            player = sender
        }
    }


    /**
     * Check if the given command from onCommand is matching the command given in parameters and run the lambda if
     * matching.
     *
     *
     * the command must have syntax /<pluginPrefix> <commandName> [params]
     * 0 to 5 params is possible
     *
     * @param command         command with params {x}
     * @param permission      permission needed to do the command (can be null)
     * @param commandRunnable function to call to apply the command
     * @return true if the command succeed else false
    </commandName></pluginPrefix> */
    protected fun checkCommand(
        command: String,
        permission: String?,
        commandRunnable: Runnable
    ): Boolean {
        var cmd = command
        if (args.isEmpty() || !args[0].equals(cmd.split(" ")[1], ignoreCase = true)) {
            return false
        }

        for (i in 0..4) {
            if (cmd.contains("{$i}")) {
                if (args.size > i + 1) {
                    cmd = cmd.replace("{$i}", args[i + 1])
                } else {
                    if (!isCommandSucceed.contains(true)) {
                        sender.sendMessage(DeadChest.local.get("loc_prefix") + ChatColor.RED + "Bad argument(s) for /dc " + args[0])
                        return true
                    }
                }
            }
        }
        if (player == null || permission == null || player!!.hasPermission(permission)) {
            val commandsPart = cmd.split(" ")
            if (args.size == commandsPart.size - 1) {
                for (i in args.indices) {
                    if (!args[i].equals(commandsPart[i + 1], ignoreCase = true)) {
                        return false
                    }
                }

                commandRunnable.run()
                return true
            }
        }
        return false
    }

    fun registerCommand(command: String, permission: String?, commandRunnable: Runnable) {
        isCommandSucceed.add(checkCommand(command, permission, commandRunnable))
    }
}