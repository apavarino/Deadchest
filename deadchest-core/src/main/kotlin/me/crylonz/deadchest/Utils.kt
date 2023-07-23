package me.crylonz.deadchest

import com.sk89q.worldguard.protection.flags.BooleanFlag
import me.crylonz.deadchest.utils.ConfigKey
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import java.util.*


object Utils {
    var DEADCHEST_GUEST_FLAG: BooleanFlag? = null
    var DEADCHEST_OWNER_FLAG: BooleanFlag? = null
    var DEADCHEST_MEMBER_FLAG: BooleanFlag? = null
    fun isInventoryEmpty(inv: Inventory): Boolean {
        for (it in inv.contents) {
            if (it != null) return false
        }
        return true
    }

    fun generateLog(message: String) {
        val log = File("plugins/DeadChest/deadchest.log")
        try {
            log.createNewFile()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = Date(System.currentTimeMillis())
            val finalMsg = "[" + formatter.format(date) + "] " + message + "\n"
            Files.write(Paths.get("plugins/DeadChest/deadchest.log"), finalMsg.toByteArray(), StandardOpenOption.APPEND)
        } catch (e: IOException) {
            DeadChest.plugin.logger.warning("Can't write log for Deadchest : $e")
        }
    }

    fun getFreeBlockAroundThisPlace(world: World, loc: Location): Location? {
        val newLoc = loc.clone()
        if (world.getBlockAt(loc).isEmpty) {
            return newLoc
        }
        if (world.getBlockAt(newLoc.clone().add(1.0, 0.0, 0.0)).isEmpty) {
            return newLoc.add(1.0, 0.0, 0.0)
        }
        if (world.getBlockAt(newLoc.clone().add(-1.0, 0.0, 0.0)).isEmpty) {
            return newLoc.add(-1.0, 0.0, 0.0)
        }
        if (world.getBlockAt(newLoc.clone().add(0.0, 1.0, 0.0)).isEmpty) {
            return newLoc.add(0.0, 1.0, 0.0)
        }
        return if (world.getBlockAt(newLoc.clone().add(0.0, -1.0, 0.0)).isEmpty) {
            newLoc.add(0.0, -1.0, 0.0)
        } else null
    }

    fun worldGuardCheck(player: Player?): Boolean =
         DeadChest.worldGuardDependenciesChecker?.worldGuardChecker(player!!) ?: true

    val isBefore1_18: Boolean
        get() = Bukkit.getVersion().contains("1.17") || isBefore1_17
    val isBefore1_17: Boolean
        get() = Bukkit.getVersion().contains("1.16") || isBefore1_16
    val isBefore1_16: Boolean
        get() = (Bukkit.getVersion().contains("1.15")
                || Bukkit.getVersion().contains("1.14")
                || Bukkit.getVersion().contains("1.13")
                || Bukkit.getVersion().contains("1.12"))

    fun isGraveBlock(material: Material?): Boolean {
        return DeadChest.graveBlocks.contains(material)
    }

    fun isHelmet(item: ItemStack): Boolean {
        val helmetList = arrayOf(
            Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.LEATHER_HELMET,
            Material.DIAMOND_HELMET, Material.CHAINMAIL_HELMET, Material.TURTLE_HELMET
        )
        return isGear(item, helmetList, Material.NETHERITE_HELMET)
    }

    fun isLeggings(item: ItemStack): Boolean {
        val leggingList = arrayOf(
            Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.LEATHER_LEGGINGS,
            Material.DIAMOND_LEGGINGS, Material.CHAINMAIL_LEGGINGS
        )
        return isGear(item, leggingList, Material.NETHERITE_LEGGINGS)
    }

    fun isChestplate(item: ItemStack): Boolean {
        val chestplateList = arrayOf(
            Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.LEATHER_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.ELYTRA
        )
        return isGear(item, chestplateList, Material.NETHERITE_CHESTPLATE)
    }

    fun isBoots(item: ItemStack): Boolean {
        val bootList = arrayOf(
            Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.LEATHER_BOOTS,
            Material.DIAMOND_BOOTS, Material.CHAINMAIL_BOOTS
        )
        return isGear(item, bootList, Material.NETHERITE_BOOTS)
    }

    fun isGear(item: ItemStack, gearList: Array<Material>, netheriteGear: Material): Boolean {
        val isStandardGear = Arrays.asList(*gearList).contains(item.type)
        val isNetheriteGear = isNetherite(item, netheriteGear)
        val hasCurseOfBinding = item.enchantments.containsKey(Enchantment.BINDING_CURSE)
        return (isStandardGear || isNetheriteGear) && !hasCurseOfBinding
    }

    fun isNetherite(item: ItemStack, netheriteGear: Material): Boolean {
        return !isBefore1_16 && item.type == netheriteGear
    }

    fun computeMinHeight(): Int {
        return if (isBefore1_18) {
            1
        } else {
            -64
        }
    }

    fun computeChestType(b: Block, p: Player?) {
        when (DeadChest.dcConfig.getInt(ConfigKey.DROP_BLOCK)) {
            2 -> {
                b.type = Material.PLAYER_HEAD
                b.setMetadata("deadchest", FixedMetadataValue(DeadChest.plugin, true))
                val state = b.state
                val skull = state as Skull
                if (p != null) {
                    skull.setOwningPlayer(p)
                }
                skull.update()
            }

            3 -> b.type = Material.BARREL
            4 -> b.type = Material.SHULKER_BOX
            5 -> b.type = Material.ENDER_CHEST
            else -> b.type = Material.CHEST
        }
    }

    fun computeXpToStore(player: Player): Int {
        return if (DeadChest.dcConfig.getBoolean(ConfigKey.STORE_XP)) {
            player.totalExperience
        } else 0
    }
}
