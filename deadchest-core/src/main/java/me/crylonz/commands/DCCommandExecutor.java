package me.crylonz.commands;

import me.crylonz.DeadChest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static me.crylonz.DeadChest.local;

public class DCCommandExecutor implements CommandExecutor {

    private final DeadChest plugin;
    private final DCCommandRegistrationService commandRegistration;

    public DCCommandExecutor(DeadChest p) {
        this.plugin = p;
        this.commandRegistration = new DCCommandRegistrationService(plugin);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        commandRegistration.register(sender, args);

        commandRegistration.registerReload();           // dc reload
        commandRegistration.registerRepairForce();      // dc repair force
        commandRegistration.registerRepair();           // dc repair
        commandRegistration.registerRemoveInfinite();   // dc removeinfinate
        commandRegistration.registerRemoveAll();        // dc removeall
        commandRegistration.registerRemoveOwn();        // dc remove
        commandRegistration.registerRemoveOther();      // dc remove <PlayerName>
        commandRegistration.registerListOwn();          // dc list
        commandRegistration.registerListOther();        // dc list all | <PlayerName>
        commandRegistration.registerGiveBack();         // dc giveback <PlayerName>

        if (!commandRegistration.isCommandSucceed()) {
            sender.sendMessage(local.get("loc_prefix") + ChatColor.RED + "Unrecognized Command");
        }
        return commandRegistration.isCommandSucceed();
    }
}
