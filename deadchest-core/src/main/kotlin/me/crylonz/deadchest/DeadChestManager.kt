package me.crylonz.deadchest

import me.crylonz.deadchest.DeadChest.Companion.chestData
import me.crylonz.deadchest.DeadChest.Companion.dcConfig
import me.crylonz.deadchest.DeadChest.Companion.plugin
import me.crylonz.deadchest.utils.ConfigKey
import me.crylonz.deadchest.utils.ConfigKey.DEADCHEST_DURATION
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType.ARMOR_STAND
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import kotlin.math.abs

object DeadChestManager {

    /**
     * Remove all active deadchests
     *
     * @return number of deadchests removed
     */
    fun cleanAllDeadChests(): Int {
        var chestDataRemoved = 0
        if (chestData.isNotEmpty()) {
            chestData
                .filter { it.chestLocation.world != null }
                .forEach {
                    it.chestLocation.world!!.getBlockAt(it.chestLocation).type = Material.AIR
                    it.removeArmorStand()
                    chestDataRemoved++
                }
            DeadChest.fileManager.saveModification()
        }
        return chestDataRemoved
    }

    /**
     * Generate a hologram at the given position
     *
     * @param location position to place
     * @param text     text to display
     * @param shiftX   x shifting
     * @param shiftY   y shifting
     * @param shiftZ   z shifting
     * @return the generated armorstand
     */
    fun generateHologram(
        location: Location?,
        text: String?,
        shiftX: Float,
        shiftY: Float,
        shiftZ: Float,
        isTimer: Boolean
    ): ArmorStand {
        if (location != null && location.world != null) {
            val holoLoc = Location(
                location.world,
                location.x + shiftX,
                location.y + shiftY + 2,
                location.z + shiftZ
            )
            val armorStand = location.world!!.spawnEntity(holoLoc, ARMOR_STAND) as ArmorStand
            armorStand.isInvulnerable = true
            armorStand.isSmall = true
            armorStand.setGravity(false)
            armorStand.canPickupItems = false
            armorStand.isVisible = false
            armorStand.isCollidable = false
            armorStand.setMetadata("deadchest", FixedMetadataValue(plugin, isTimer))
            armorStand.customName = text
            armorStand.isSilent = true
            armorStand.isMarker = true
            armorStand.isCustomNameVisible = true
            return armorStand
        }
        throw IllegalStateException("Unable to create hologram on world ${location?.world}")
    }

    /**
     * get the number of deadchest for a player
     *
     * @param p player
     * @return number of deadchests
     */
    fun playerDeadChestAmount(p: Player?): Int {
        var count = 0
        if (p != null) {
            for ((_, _, _, playerUUID) in chestData) {
                if (p.uniqueId.toString() == playerUUID) count++
            }
        }
        return count
    }

    /**
     * Regeneration of metaData for holos
     */
    private fun reloadMetaData() {
        chestData
            .filter { it.chestLocation.world != null }
            .forEach { chestData ->
                chestData.chestLocation.world!!.getNearbyEntities(chestData.holographicTimer, 1.0, 1.0, 1.0)
                    .forEach {
                        if (it.uniqueId == chestData.holographicOwnerId) {
                            it.setMetadata("deadchest", FixedMetadataValue(plugin, false))
                        } else if (it.uniqueId == chestData.holographicTimerId) {
                            it.setMetadata("deadchest", FixedMetadataValue(plugin, true))
                        }
                    }
            }
    }

    fun replaceDeadChestIfItDeseapears(chestData: ChestData): Boolean {
        val world = chestData.chestLocation.world
        if (world != null && !Utils.isGraveBlock(world.getBlockAt(chestData.chestLocation).type)) {
            val block = world.getBlockAt(chestData.chestLocation)
            Utils.computeChestType(block, Bukkit.getPlayer(chestData.playerUUID))
            return true
        }
        return false
    }

    fun handleExpirateDeadChest(chestData: ChestData, date: Date): Boolean {
        if (chestData.chestDate.time + dcConfig.getInt(DEADCHEST_DURATION) * 1000L < date.time && !chestData.isInfinity && dcConfig.getInt(
                DEADCHEST_DURATION
            ) != 0
        ) {
            val loc = chestData.chestLocation
            if (loc.world != null) {
                if (!chestData.isRemovedBlock) {
                    chestData.isRemovedBlock = true
                    loc.world!!.getBlockAt(loc).type = Material.AIR
                }
                if (dcConfig.getBoolean(ConfigKey.ITEMS_DROPPED_AFTER_TIMEOUT)) {
                    for (itemStack in chestData.inventory) {
                        if (itemStack != null) {
                            loc.world!!.dropItemNaturally(loc, itemStack)
                        }
                    }
                    chestData.cleanInventory()
                }
            }
            if (chestData.removeArmorStand()) {
                DeadChest.chestData.remove(chestData)
            }
            return true
        }
        return false
    }

    fun updateTimer(chestData: ChestData, date: Date) {
        val chestTimer = chestData.holographicTimer
        if (chestTimer.world != null && chestData.isChunkLoaded()) {
            val entityList = chestTimer.world!!
                .getNearbyEntities(chestTimer, 1.0, 1.0, 1.0) as ArrayList<Entity>

            for (entity in entityList) {
                if (entity.type == ARMOR_STAND) {
                    if (!entity.hasMetadata("deadchest")) {
                        reloadMetaData()
                    }
                    if (entity.getMetadata("deadchest").size > 0 && entity.getMetadata("deadchest")[0].asBoolean()) {
                        val diff = date.time - (chestData.chestDate.time + dcConfig.getInt(DEADCHEST_DURATION) * 1000L)
                        val diffSeconds = abs(diff / 1000 % 60)
                        val diffMinutes = abs(diff / (60 * 1000) % 60)
                        val diffHours = abs(diff / (60 * 60 * 1000))
                        if (!chestData.isInfinity && dcConfig.getInt(DEADCHEST_DURATION) != 0) {
                            entity.customName = DeadChest.local.replaceTimer(
                                DeadChest.local.get("holo_timer"),
                                diffHours,
                                diffMinutes,
                                diffSeconds
                            )
                        } else {
                            entity.customName = DeadChest.local.get("loc_infinityChest")
                        }
                    }
                }
            }
        }
    }
}