package me.crylonz.deadchest

import me.crylonz.deadchest.Indexes.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

@SerializableAs("ChestData")
data class ChestData(

    var inventory: List<ItemStack>,
    var chestLocation: Location,
    var playerName: String,
    var playerUUID: String,
    var chestDate: Date,
    var isInfinity: Boolean,
    var isRemovedBlock: Boolean,
    var holographicTimer: Location,
    var holographicTimerId: UUID,
    var holographicOwnerId: UUID,
    var worldName: String,
    var xpStored: Int,
) : ConfigurationSerializable {

    constructor(
        inv: Inventory,
        chestLocation: Location,
        player: Player,
        isInfinity: Boolean,
        armorStandTimer: ArmorStand,
        owner: ArmorStand,
        xpToStore: Int,
    ) : this(
        inventory = listOf<ItemStack>(*inv.contents),
        chestLocation = chestLocation.clone(),
        playerName = player.name,
        playerUUID = player.uniqueId.toString(),
        chestDate = Date(),
        isInfinity = isInfinity,
        isRemovedBlock = false,
        holographicTimer = armorStandTimer.location.clone(),
        holographicTimerId = armorStandTimer.uniqueId,
        holographicOwnerId = owner.uniqueId,
        worldName = player.world.name,
        xpStored = xpToStore
    )

    override fun serialize(): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        map["inventory"] = inventory
        map["chestLocation"] = "$worldName;${chestLocation.x};${chestLocation.y};${chestLocation.z}"
        map["playerName"] = playerName
        map["playerUUID"] = playerUUID
        map["chestDate"] = chestDate
        map["isRemovedBlock"] = isRemovedBlock
        map["isInfinity"] = isInfinity
        map["holographicTimer"] = "$worldName;${holographicTimer.x};${holographicTimer.y};${holographicTimer.z}"
        map["worldName"] = worldName
        map["as_timer_id"] = holographicTimerId.toString()
        map["as_owner_id"] = holographicOwnerId.toString()
        map["xpStored"] = xpStored
        return map
    }

    fun isChunkLoaded(): Boolean {
        return chestLocation.world == null ||
                chestLocation.world!!.isChunkLoaded(chestLocation.blockX shr 4, chestLocation.blockZ shr 4)
    }

    fun isChunkForceLoaded(): Boolean {
        return chestLocation.world == null ||
                chestLocation.world!!.isChunkForceLoaded(chestLocation.blockX shr 4, chestLocation.blockZ shr 4)
    }

    fun removeArmorStand(): Boolean {
        val radius = 1
        val armorStandShiftY = 1

        if (chestLocation.world != null) {
            chestLocation.chunk.isForceLoaded = true

            val entities = chestLocation.world!!.getNearbyEntities(
                Location(chestLocation.world, chestLocation.x, chestLocation.y + armorStandShiftY, chestLocation.z),
                radius.toDouble(), radius.toDouble(), radius.toDouble()
            )

            entities
                .filter { it.uniqueId == holographicOwnerId || it.uniqueId == holographicTimerId }
                .forEach { it.remove() }

            if (isChunkForceLoaded()) {
                chestLocation.world!!.unloadChunk(chestLocation.blockX shr 4, chestLocation.blockZ shr 4)
                chestLocation.chunk.isForceLoaded = false
            }

            return entities.isNotEmpty()
        }
        return false
    }

    fun cleanInventory() {
        inventory = emptyList()
    }

    companion object {
        @JvmStatic
        fun deserialize(map: Map<String, Any>): ChestData {
            val chestLocationSerialized = map["chestLocation"].toString().split(";")
            val holoLocationSerialized = map["holographicTimer"].toString().split(";")

            val chestLocation = Location(
                Bukkit.getWorld(chestLocationSerialized[WORLD_NAME.ordinal]),
                chestLocationSerialized[LOC_X.ordinal].toDouble(),
                chestLocationSerialized[LOC_Y.ordinal].toDouble(),
                chestLocationSerialized[LOC_Z.ordinal].toDouble()
            )

            val holoLocation = Location(
                Bukkit.getWorld(holoLocationSerialized[WORLD_NAME.ordinal]),
                holoLocationSerialized[LOC_X.ordinal].toDouble(),
                holoLocationSerialized[LOC_Y.ordinal].toDouble(),
                holoLocationSerialized[LOC_Z.ordinal].toDouble()
            )
            return ChestData(
                inventory = map["inventory"] as MutableList<ItemStack>,
                chestLocation = chestLocation,
                playerName = map["playerName"] as String,
                playerUUID = map["playerUUID"] as String,
                chestDate = map["chestDate"] as Date,
                isInfinity = map["isInfinity"] as Boolean,
                isRemovedBlock = map["isRemovedBlock"] != null && map["isRemovedBlock"] as Boolean, // compatibility under 4.14
                holographicTimer = holoLocation,
                holographicTimerId = UUID.fromString(map["as_timer_id"] as String),
                holographicOwnerId = UUID.fromString(map["as_owner_id"] as String),
                worldName = map["worldName"] as String,
                xpStored = if (map["xpStored"] != null) map["xpStored"] as Int else 0  // compatibility under 4.15
            )
        }
    }
}

internal enum class Indexes {
    WORLD_NAME,
    LOC_X,
    LOC_Y,
    LOC_Z
}
