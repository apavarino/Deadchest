package me.crylonz

import me.crylonz.deadchest.ChestData
import me.crylonz.deadchest.DeadChest
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

object DeadChestAPI {

    /**
     * Get a list of chest for the given player
     *
     * @param player
     * @return List<ChestData>
    </ChestData> */
    fun getChests(player: Player): List<ChestData> {
        val chestData: MutableList<ChestData> = ArrayList()

        DeadChest.chestData
            .filter { it.playerName.equals(player.name, ignoreCase = true) }
            .forEach { chest: ChestData ->
                chestData.add(
                    chest.copy(
                        inventory = chest.inventory,
                        chestLocation = chest.chestLocation,
                        playerName = chest.playerName,
                        playerUUID = chest.playerUUID,
                        chestDate = chest.chestDate,
                        isInfinity = chest.isInfinity,
                        isRemovedBlock = chest.isRemovedBlock,
                        holographicTimer = chest.holographicTimer,
                        holographicTimerId = chest.holographicTimerId,
                        holographicOwnerId = chest.holographicOwnerId,
                        worldName = chest.worldName,
                        xpStored = chest.xpStored
                    )
                )
            }
        return chestData
    }

    /**
     * Give back the last Deadchest of a player. The player need to be online
     *
     * @param player
     * @param chest
     * @return true if chest is returned to his owner
     */
    fun giveBackChest(player: Player, chest: ChestData): Boolean {
        if (player.isOnline) {
            chest.inventory
                .filterNotNull()
                .forEach { player.world.dropItemNaturally(player.location, it) }

            return DeadChestAPI.removeChest(chest)
        }
        return false
    }

    /**
     * Remove a player chest
     *
     * @param chest
     * @return true is the chest is correctly removed
     */
    fun removeChest(chest: ChestData): Boolean {
        val world = Bukkit.getWorld(chest.worldName)
        if (world != null && DeadChest.chestData.contains(chest)) {
            world.getBlockAt(chest.chestLocation).type = Material.AIR
            chest.removeArmorStand()
            DeadChest.chestData.remove(chest)
            return true
        }
        return false
    }
}
