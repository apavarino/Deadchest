package me.crylonz.deadchest.commands

import me.crylonz.deadchest.ChestData
import me.crylonz.deadchest.DeadChest
import me.crylonz.deadchest.DeadChest.Companion.chestData
import me.crylonz.deadchest.DeadChest.Companion.dcConfig
import me.crylonz.deadchest.DeadChest.Companion.fileManager
import me.crylonz.deadchest.DeadChest.Companion.local
import me.crylonz.deadchest.DeadChestManager
import me.crylonz.deadchest.Permission
import me.crylonz.deadchest.Permission.*
import me.crylonz.deadchest.utils.ConfigKey.DEADCHEST_DURATION
import me.crylonz.deadchest.utils.ConfigKey.REQUIRE_PERMISSION_TO_LIST_OWN
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs


class DCCommandRegistrationService(plugin: DeadChest) : DCCommandRegistration(plugin) {
    fun registerReload() {
        registerCommand("dc reload", ADMIN.label) {
            fileManager.reloadChestDataConfig()
            fileManager.reloadLocalizationConfig()

            plugin.reloadConfig()
            plugin.registerConfig()

            fileManager.chestDataConfig.getList("chestData")
                ?.let { chestData = it as MutableList<ChestData> }

            local.set(
                fileManager.localizationConfig.getConfigurationSection("localisation")!!
                    .getValues(true)
            )
            sender.sendMessage(GREEN.toString() + local.get("loc_prefix") + " Plugin reload successfully")
        }
    }

    fun registerRepairForce() {
        registerCommand("dc repair force", ADMIN.label) { repair(true) }
    }

    fun registerRepair() {
        registerCommand("dc repair", ADMIN.label) { repair(false) }
    }

    private fun repair(forced: Boolean) {

        player?.let { player ->
            var holoRemoved = 0
            player.world.getNearbyEntities(player.location, 100.0, 25.0, 100.0)
                .filter { it.type == EntityType.ARMOR_STAND }
                .map { it as ArmorStand }
                .filter { it.hasMetadata("deadchest") || forced }
                .forEach {
                    holoRemoved++
                    it.remove()
                }

            sender.sendMessage("${local.get("loc_prefix")}$GOLD Operation complete. [$holoRemoved] hologram(s) removed")

        } ?: sender.sendMessage("${local.get("loc_prefix")}$RED Command must be called by a player")
    }

    fun registerRemoveInfinite() {
        registerCommand("dc removeinfinite", ADMIN.label) {

            var cpt = 0
            if (chestData.isNotEmpty()) {

                chestData
                    .filter { it.chestLocation.world != null }
                    .forEach {
                        if (it.isInfinity || dcConfig.getInt(DEADCHEST_DURATION) == 0) {
                            it.chestLocation.world!!.getBlockAt(it.chestLocation).type = Material.AIR
                            it.removeArmorStand()
                            chestData.remove(it)
                            cpt++
                        }
                    }
                fileManager.saveModification()
            }

            sender.sendMessage(
                "${local.get("loc_prefix")}${GOLD}Operation complete. [$GREEN$cpt$GOLD] deadchest(s) removed"
            )
        }
    }

    fun registerRemoveAll() {
        registerCommand("dc removeall", ADMIN.label) {
            val cpt: Int = DeadChestManager.cleanAllDeadChests()
            sender.sendMessage(
                ("${local.get("loc_prefix")}${GOLD}Operation complete. [$GREEN$cpt$GOLD] deadchest(s) removed")
            )
        }
    }

    fun registerRemoveOwn() {
        registerCommand("dc remove", Permission.REMOVE_OWN.label) {
            player?.let {
                removeAllDeadChestOfPlayer(it.name)
            } ?: sender.sendMessage("${local.get("loc_prefix")}${RED}Command must be called by a player")
        }
    }

    fun registerRemoveOther() {
        registerCommand("dc remove {0}", Permission.REMOVE_OTHER.label) {
            removeAllDeadChestOfPlayer(args[1])
        }
    }

    private fun removeAllDeadChestOfPlayer(playerName: String) {
        var cpt = 0
        if (chestData.isNotEmpty()) {

            chestData
                .filter { it.chestLocation.world != null }
                .filter { it.playerName.equals(playerName, ignoreCase = true) }
                .forEach {
                    it.chestLocation.world!!.getBlockAt(it.chestLocation).type = Material.AIR
                    it.removeArmorStand()
                    chestData.remove(it)
                    cpt++
                }
            fileManager.saveModification()
        }
        sender.sendMessage(
            local.get("loc_prefix") + GOLD + "Operation complete. [" + GREEN + cpt + GOLD + "] deadchest(s) removed of player " + playerName
        )
    }

    fun registerListOwn() {
        registerCommand("dc list", null) {
            player?.let {
                if (it.hasPermission(LIST_OWN.label) || !dcConfig.getBoolean(REQUIRE_PERMISSION_TO_LIST_OWN)) {
                    if (chestData.isNotEmpty()) {
                        sender.sendMessage("${local.get("loc_prefix")}${local.get("loc_dclistown")} :")
                        chestData
                            .filter { data -> data.playerUUID.equals(it.uniqueId.toString(), ignoreCase = true) }
                            .forEach { data -> displayChestData(Date(), data) }
                    } else {
                        it.sendMessage("${local.get("loc_prefix")}${local.get("loc_nodc")}")
                    }
                }
            } ?: sender.sendMessage("${local.get("loc_prefix")}${RED}Command must be called by a player")
        }
    }

    fun registerListOther() {
        registerCommand("dc list {0}", LIST_OTHER.label) {

            val now = Date()
            if (args[1].equals("all", ignoreCase = true)) {
                if (chestData.isNotEmpty()) {
                    sender.sendMessage("${local.get("loc_prefix")}${local.get("loc_dclistall")}:")
                    chestData.forEach { data: ChestData ->
                        displayChestData(now, data)
                    }
                } else {
                    sender.sendMessage("${local.get("loc_prefix")}${local.get("loc_nodcs")}")
                }
            } else {
                if (chestData.isNotEmpty()) {
                    sender.sendMessage("${local.get("loc_prefix")}$GREEN${args[1]} deadchests :")
                    chestData
                        .filter { it.playerName.equals(args[1], ignoreCase = true) }
                        .forEach { displayChestData(now, it) }
                } else {
                    sender.sendMessage("${local.get("loc_prefix")}${local.get("loc_nodcs")}")
                }
            }
        }
    }

    private fun displayChestData(now: Date, chestData: ChestData) {
        val worldName = when {
            chestData.chestLocation.world != null -> chestData.chestLocation.world!!.name
            else -> "???"
        }

        if (chestData.isInfinity || dcConfig.getInt(DEADCHEST_DURATION) == 0) {
            sender.sendMessage(
                "-" + AQUA + " World: " + WHITE + worldName + " |"
                        + AQUA + " X: " + WHITE + chestData.chestLocation.x
                        + AQUA + " Y: " + WHITE + chestData.chestLocation.y
                        + AQUA + " Z: " + WHITE + chestData.chestLocation.z
                        + " | "
                        + "∞ " + local.get("loc_endtimer")
            )
        } else {
            val diff = now.time - (chestData.chestDate.time + dcConfig.getInt(DEADCHEST_DURATION) * 1000L)
            val diffSeconds = abs(diff / 1000 % 60)
            val diffMinutes = abs(diff / (60 * 1000) % 60)
            val diffHours = abs(diff / (60 * 60 * 1000))
            sender.sendMessage(
                "-" + AQUA + " X: " + WHITE + chestData.chestLocation.x
                        + AQUA + " Y: " + WHITE + chestData.chestLocation.y
                        + AQUA + " Z: " + WHITE + chestData.chestLocation.z
                        + " | " +
                        +diffHours + "h "
                        + diffMinutes + "m "
                        + diffSeconds + "s " + local.get("loc_endtimer")
            )
        }
    }

    fun registerGiveBack() {
        registerCommand("dc giveback {0}", GIVEBACK.label) {

            var targetPlayer: Player? = null

            chestData
                .filter { it.playerName.equals(args[1], ignoreCase = true) }
                .forEach {
                    targetPlayer = Bukkit.getPlayer(UUID.fromString(it.playerUUID))
                    targetPlayer?.let { currentPlayer ->
                        if (currentPlayer.isOnline) {
                            it.inventory
                                .filterNotNull()
                                .forEach { itemStack ->
                                    currentPlayer.world.dropItemNaturally(currentPlayer.location, itemStack)
                                }
                            currentPlayer.world.getBlockAt(it.chestLocation).type = Material.AIR
                            it.removeArmorStand()
                            chestData.remove(it)
                        }
                    }

                }

            targetPlayer?.let {
                sender.sendMessage("${local.get("loc_prefix")}${local.get("loc_dcgbsuccess")}")
                it.sendMessage("${local.get("loc_prefix")}${local.get("loc_gbplayer")}")
            } ?: sender.sendMessage("${local.get("loc_prefix")}${local.get("loc_givebackInfo")}")
        }
    }
}

