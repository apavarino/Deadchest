package me.crylonz.deadchest.commands

import me.crylonz.deadchest.DeadChest
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class DCCommandExecutor(plugin: DeadChest) : CommandExecutor {
    private val commandRegistration: DCCommandRegistrationService

    init {
        commandRegistration = DCCommandRegistrationService(plugin)
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        commandRegistration.register(sender, args.toList())
        commandRegistration.registerReload() // dc reload
        commandRegistration.registerRepairForce() // dc repair force
        commandRegistration.registerRepair() // dc repair
        commandRegistration.registerRemoveInfinite() // dc removeinfinate
        commandRegistration.registerRemoveAll() // dc removeall
        commandRegistration.registerRemoveOwn() // dc remove
        commandRegistration.registerRemoveOther() // dc remove <PlayerName>
        commandRegistration.registerListOwn() // dc list
        commandRegistration.registerListOther() // dc list all | <PlayerName>
        commandRegistration.registerGiveBack() // dc giveback <PlayerName>

        if (!commandRegistration.isCommandSucceed.contains(true)) {
            sender.sendMessage("${DeadChest.local.get("loc_prefix")}${RED}Unrecognized Command")
        }
        return commandRegistration.isCommandSucceed.contains(true)
    }
}
