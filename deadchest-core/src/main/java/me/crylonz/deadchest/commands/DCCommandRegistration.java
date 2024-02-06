package me.crylonz.deadchest.commands;

import me.crylonz.deadchest.DeadChest;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.crylonz.deadchest.DeadChest.local;

abstract class DCCommandRegistration {
    protected final DeadChest plugin;

    // CommandSender params
    protected CommandSender sender = null;
    protected String[] args = null;

    protected Player player = null;
    protected boolean commandSucceed = false;

    public DCCommandRegistration(DeadChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Need to be call at then beginning of onCommand to set up the context
     *
     * @param sender sender from onCommand
     * @param args   args from onCommand
     */
    public void register(CommandSender sender, String[] args) {
        this.sender = sender;
        this.args = args;
        this.commandSucceed = false;

        if (sender instanceof Player) {
            player = (Player) sender;
        }
    }

    /**
     * commandSucceed will be true if any command is call successfully
     *
     * @param succeed command call success status
     */
    protected void setCommandSucceed(boolean succeed) {
        this.commandSucceed = this.commandSucceed || succeed;
    }

    public boolean isCommandSucceed() {
        return commandSucceed;
    }

    /**
     * Check if the given command from onCommand is matching the command given in parameters and run the lambda if
     * matching.
     * <p>
     * the command must have syntax /<pluginPrefix> <commandName> [params...]
     * 0 to 5 params is possible
     *
     * @param command         command with params {x}
     * @param permission      permission needed to do the command (can be null)
     * @param commandRunnable function to call to apply the command
     * @return true if the command succeed else false
     */
    protected boolean checkCommand(String command,
                                   String permission,
                                   Runnable commandRunnable) {
        if (args.length == 0 || !args[0].equalsIgnoreCase(command.split(" ")[1])) {
            return false;
        }

        for (int i = 0; i < 5; ++i) {
            if (command.contains("{" + i + "}")) {
                if (args.length > i + 1) {
                    command = command.replace("{" + i + "}", args[i + 1]);
                } else {
                    if (!commandSucceed) {
                        sender.sendMessage(local.get("loc_prefix") + ChatColor.RED + "Bad argument(s) for /dc " + args[0]);
                        return true;
                    }
                }
            }
        }

        if (player == null || (permission == null || player.hasPermission(permission))) {
            String[] commandsPart = command.split(" ");
            if (args.length == commandsPart.length - 1) {
                for (int i = 0; i < args.length; ++i) {
                    if (!args[i].equalsIgnoreCase(commandsPart[i + 1])) {
                        return false;
                    }
                }
                commandRunnable.run();
                return true;
            }
        }
        return false;
    }

    public void registerCommand(String command, String permission, Runnable commandRunnable) {
        setCommandSucceed(checkCommand(command, permission, commandRunnable));
    }
}