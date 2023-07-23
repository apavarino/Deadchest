package me.crylonz.deadchest

import me.crylonz.deadchest.DeadChest.Companion.chestData
import me.crylonz.deadchest.DeadChest.Companion.local
import me.crylonz.deadchest.Utils.generateLog
import me.crylonz.deadchest.Utils.isBoots
import me.crylonz.deadchest.Utils.isChestplate
import me.crylonz.deadchest.Utils.isHelmet
import me.crylonz.deadchest.Utils.isLeggings
import me.crylonz.deadchest.utils.ConfigKey
import me.crylonz.deadchest.utils.DeadChestConfig
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class DeadChestListener(private val plugin: DeadChest) : Listener {

    val config: DeadChestConfig
        get() = DeadChest.dcConfig

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerDeathEvent(event: PlayerDeathEvent) {
        if (event.keepInventory) {
            return
        }
        val player = event.entity.player

        if (player == null
            || DeadChest.dcConfig.getArray(ConfigKey.EXCLUDED_WORLDS).contains(player.world.name)
            || !config.getBoolean(ConfigKey.GENERATE_DEADCHEST_IN_CREATIVE)
            && player.gameMode == GameMode.CREATIVE
        ) {
            return
        }
        if (Utils.worldGuardCheck(player) && (player.hasPermission(Permission.GENERATE.label)
                    || !config.getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_GENERATE))
        ) {
            if ((DeadChestManager.playerDeadChestAmount(player) < config.getInt(ConfigKey.MAX_DEAD_CHEST_PER_PLAYER) ||
                        config.getInt(ConfigKey.MAX_DEAD_CHEST_PER_PLAYER) == 0) &&
                player.getMetadata("NPC").isEmpty()
            ) {
                val world = player.world
                var loc = player.location
                if (!config.getBoolean(ConfigKey.GENERATE_ON_LAVA) && loc.block.type == Material.LAVA) {
                    generateLog("Player dies in lava : No deadchest generated")
                    return
                }
                if (!config.getBoolean(ConfigKey.GENERATE_ON_WATER) && loc.block.type == Material.WATER) {
                    generateLog("Player dies in water : No deadchest generated")
                    return
                }
                if (!config.getBoolean(ConfigKey.GENERATE_ON_RAILS) && loc.block.type == Material.RAIL || loc.block.type == Material.ACTIVATOR_RAIL || loc.block.type == Material.DETECTOR_RAIL || loc.block.type == Material.POWERED_RAIL) {
                    generateLog("Player dies on rails : No deadchest generated")
                    return
                }
                if (!config.getBoolean(ConfigKey.GENERATE_IN_MINECART) && player.vehicle != null) {
                    if (player.vehicle!!.type == EntityType.MINECART) {
                        generateLog("Player dies in a minecart : No deadchest generated")
                        return
                    }
                }

                // Handle case bottom of the world
                val minHeight = Utils.computeMinHeight()
                if (loc.y < minHeight) {
                    loc.y = (world.getHighestBlockYAt(loc.x.toInt(), loc.z.toInt()) + 1).toDouble()

                    if (loc.y < minHeight) {
                        loc.y = minHeight.toDouble()
                    }
                } else if (loc.blockY >= world.maxHeight) {

                    var y = world.maxHeight - 1
                    loc.y = y.toDouble()

                    while (world.getBlockAt(loc).type != Material.AIR && y > 0) {
                        y--
                        loc.y = y.toDouble()
                    }

                    if (y < 1) {
                        player.sendMessage(local.get("loc_prefix") + local.get("loc_noDCG"))
                        return
                    }

                } else {
                    if (world.getBlockAt(loc).type == Material.DARK_OAK_DOOR ||
                        world.getBlockAt(loc).type == Material.ACACIA_DOOR ||
                        world.getBlockAt(loc).type == Material.BIRCH_DOOR ||
                        !Utils.isBefore1_16 && world.getBlockAt(loc).type == Material.CRIMSON_DOOR ||
                        world.getBlockAt(loc).type == Material.IRON_DOOR ||
                        world.getBlockAt(loc).type == Material.JUNGLE_DOOR ||
                        world.getBlockAt(loc).type == Material.OAK_DOOR ||
                        world.getBlockAt(loc).type == Material.SPRUCE_DOOR ||
                        !Utils.isBefore1_16 && world.getBlockAt(loc).type == Material.WARPED_DOOR ||
                        world.getBlockAt(loc).type == Material.VINE ||
                        world.getBlockAt(loc).type == Material.LADDER
                    ) {
                        val tmpLoc = Utils.getFreeBlockAroundThisPlace(world, loc)
                        if (tmpLoc != null) {
                            loc = tmpLoc
                        }
                    }
                    if (world.getBlockAt(loc).type != Material.AIR &&
                        world.getBlockAt(loc).type != Material.CAVE_AIR &&
                        world.getBlockAt(loc).type != Material.VOID_AIR &&
                        world.getBlockAt(loc).type != Material.WATER &&
                        world.getBlockAt(loc).type != Material.LAVA
                    ) {
                        while (world.getBlockAt(loc).type != Material.AIR && loc.y < world.maxHeight) {
                            loc.y = loc.y + 1
                        }
                    }
                }
                val groundLocation = loc.clone()
                groundLocation.y = groundLocation.y - 1
                if (Utils.isBefore1_17 && world.getBlockAt(groundLocation).type == Material.valueOf("GRASS_PATH") || !Utils.isBefore1_17 && world.getBlockAt(
                        groundLocation
                    ).type == Material.DIRT_PATH || world.getBlockAt(groundLocation).type == Material.FARMLAND
                ) {
                    loc.y = loc.y + 1
                }
                val b = world.getBlockAt(loc)
                if (!Utils.isInventoryEmpty(player.inventory)) {
                    Utils.computeChestType(b, player)
                    val firstLine =
                        local.replacePlayer(local.get("holo_owner"), event.entity.displayName)
                    val holoName = DeadChestManager.generateHologram(b.location, firstLine, 0.5f, -0.95f, 0.5f, false)
                    val secondLine = local.get("holo_loading")
                    val holoTime = DeadChestManager.generateHologram(b.location, secondLine, 0.5f, -1.2f, 0.5f, true)

                    // Remove items with curse of vanishing
                    for (itemStack: ItemStack? in player.inventory.contents) {
                        if (itemStack != null && itemStack.enchantments.containsKey(Enchantment.VANISHING_CURSE)) {
                            player.inventory.remove(itemStack)
                        }
                    }
                    if (player.inventory.helmet != null
                        && player.inventory.helmet!!.enchantments.containsKey(Enchantment.VANISHING_CURSE)
                    ) {
                        player.inventory.helmet = null
                    }
                    if (player.inventory.chestplate != null
                        && player.inventory.chestplate!!.enchantments.containsKey(Enchantment.VANISHING_CURSE)
                    ) {
                        player.inventory.chestplate = null
                    }
                    if (player.inventory.leggings != null
                        && player.inventory.leggings!!.enchantments.containsKey(Enchantment.VANISHING_CURSE)
                    ) {
                        player.inventory.leggings = null
                    }
                    if (player.inventory.boots != null
                        && player.inventory.boots!!.enchantments.containsKey(Enchantment.VANISHING_CURSE)
                    ) {
                        player.inventory.boots = null
                    }
                    if (player.inventory.itemInOffHand.enchantments.containsKey(Enchantment.VANISHING_CURSE)) {
                        player.inventory.setItemInOffHand(null)
                    }
                    for (item: String? in DeadChest.dcConfig.getArray(ConfigKey.EXCLUDED_ITEMS)) {
                        if (item != null && Material.getMaterial(item.uppercase(Locale.getDefault())) != null) {
                            player.inventory.remove(Material.getMaterial(item.uppercase(Locale.getDefault()))!!)
                        }
                    }
                    if (DeadChest.dcConfig.getBoolean(ConfigKey.STORE_XP)) {
                        event.droppedExp = 0
                    }
                    chestData.add(
                        ChestData(
                            player.inventory,
                            b.location,
                            player,
                            player.hasPermission(Permission.INFINITY_CHEST.label),
                            holoTime,
                            holoName,
                            Utils.computeXpToStore(player)
                        )
                    )
                    val backupInv = player.inventory.contents
                    event.drops.clear()
                    player.inventory.clear()
                    if (config.getBoolean(ConfigKey.DISPLAY_POSITION_ON_DEATH)) {
                        player.sendMessage(
                            local.get("loc_prefix") + local.get("loc_chestPos") + " X: " +
                                    ChatColor.WHITE + b.x + ChatColor.GOLD + " Y: " +
                                    ChatColor.WHITE + b.y + ChatColor.GOLD + " Z: " +
                                    ChatColor.WHITE + b.z
                        )
                    }
                    DeadChest.fileManager.saveModification()
                    generateLog("New deadchest for [" + player.name + "] in " + b.world.name + " at X:" + b.x + " Y:" + b.y + " Z:" + b.z)
                    generateLog("Chest content : " + Arrays.asList(*backupInv))
                    if (config.getBoolean(ConfigKey.LOG_DEADCHEST_ON_CONSOLE)) plugin.logger.info("New deadchest for [" + player.name + "] at X:" + b.x + " Y:" + b.y + " Z:" + b.z)
                } else {
                    generateLog("Player [" + player.name + "] died without inventory : No Deadchest generated")
                }
            }
        }
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        if ((event.action == Action.RIGHT_CLICK_BLOCK)) {
            chestData
                .filter {
                    it.chestLocation.world === event.player.world &&
                            it.chestLocation.distance(event.clickedBlock!!.location) <= 1
                }.forEach { _ ->
                    event.isCancelled = true
                    return@forEach
                }
        } else if (event.action == Action.LEFT_CLICK_BLOCK &&
            event.clickedBlock != null &&
            Utils.isGraveBlock(event.clickedBlock!!.type)
        ) {
            val block = event.clickedBlock!!
            val player = event.player
            val playerUUID = player.uniqueId.toString()
            val playerHasPermission = player.hasPermission(Permission.CHESTPASS.label)
            val playerWorld = player.world

            chestData
                .filter { it.chestLocation == block.location }
                .firstOrNull {

                    if (!config.getBoolean(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST) ||
                        playerUUID == it.playerUUID ||
                        playerHasPermission
                    ) {
                        if (!player.hasPermission(Permission.GET.label) &&
                            config.getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_GET_CHEST)
                        ) {
                            generateLog("Player ${player.name} need to have deadchest.get permission to generate")
                            player.sendMessage(local.get("loc_prefix") + local.get("loc_noPermsToGet"))
                            event.isCancelled = true
                            return@onClick
                        }

                        val deadChestPickUpEvent = DeadChestPickUpEvent()
                        Bukkit.getServer().pluginManager.callEvent(DeadChestPickUpEvent())
                        if (!deadChestPickUpEvent.isCancelled) {
                            generateLog("Deadchest of [${it.playerName}] was taken by [${player.name}] in ${playerWorld.name}")

                            // put all item on the inventory
                            if (config.getInt(ConfigKey.DROP_MODE) == 1) {
                                val playerInventory = player.inventory
                                player.giveExp(it.xpStored)

                                it.inventory
                                    .filterNotNull()
                                    .forEach { item ->
                                        when {
                                            isHelmet(item) && playerInventory.helmet == null ->
                                                playerInventory.helmet = item

                                            isBoots(item) && playerInventory.boots == null ->
                                                playerInventory.boots = item

                                            isChestplate(item) && playerInventory.chestplate == null ->
                                                playerInventory.chestplate = item

                                            isLeggings(item) && playerInventory.leggings == null ->
                                                playerInventory.leggings = item

                                            playerInventory.firstEmpty() != -1 -> playerInventory.addItem(item)
                                            else -> playerWorld.dropItemNaturally(block.location, item)
                                        }
                                    }
                            } else {
                                // pushed item on the ground
                                it.inventory
                                    .filterNotNull()
                                    .forEach { item ->
                                        playerWorld.dropItemNaturally(block.location, item)

                                        if (it.xpStored != 0) {
                                            playerWorld.spawn(block.location, ExperienceOrb::class.java).run {
                                                experience = it.xpStored
                                            }
                                        }
                                    }
                            }

                            block.type = Material.AIR
                            chestData.remove(it)
                            DeadChest.fileManager.saveModification()
                            block.world.playEffect(block.location, Effect.MOBSPAWNER_FLAMES, 10)
                            player.playSound(block.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10f, 1f)
                            it.removeArmorStand()
                            return@onClick
                        } else {
                            event.isCancelled = true
                        }
                    } else {
                        event.isCancelled = true
                        player.sendMessage(local.get("loc_prefix") + local.get("loc_not_owner"))
                    }
                    return
                }
        }
    }

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        if (Utils.isGraveBlock(event.block.type)) {
            if (config.getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)) {
                for (cd: ChestData in chestData) {
                    if (cd.chestLocation === event.block.location) {
                        event.isCancelled = true
                        event.player.sendMessage(local.get("loc_prefix") + local.get("loc_not_owner"))
                        break
                    }
                }
            }
        }
    }

    /**
     * Disable water destruction of Deadchest (for head)
     */
    @EventHandler
    fun onBlockFromToEvent(event: BlockFromToEvent) {
        if (Utils.isGraveBlock(event.toBlock.type)) {
            for (cd: ChestData in chestData) {
                if ((cd.chestLocation == event.toBlock.location)) {
                    event.isCancelled = true
                    break
                }
            }
        }
    }

    @EventHandler
    fun onEntityExplodeEvent(event: EntityExplodeEvent?) {
        chestExplosionHandler(event)
    }

    @EventHandler
    fun onBlockExplodeEvent(event: BlockExplodeEvent?) {
        chestExplosionHandler(event)
    }

    private fun chestExplosionHandler(event: Event?) {

        var blocklist: MutableList<Block> = ArrayList()
        if (event is EntityExplodeEvent) {
            blocklist = event.blockList()
        } else if (event is BlockExplodeEvent) {
            blocklist = event.blockList()
        }
        if (blocklist.size > 0) {

            blocklist.forEach { block ->
                chestData
                    .filter { Utils.isGraveBlock(block.type) && it.chestLocation == block.location }
                    .forEach {
                        if (config.getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)) {
                            blocklist.remove(block)
                            generateLog("Deadchest of [" + it.playerName + "] was protected from explosion in " + it.chestLocation.world!!.name)
                        } else {
                            it.removeArmorStand()
                            chestData.remove(it)
                            generateLog(
                                "Deadchest of [" + it.playerName + "] was blown up in " + it.chestLocation.world!!.name
                            )
                        }
                    }
            }
        }

        @EventHandler
        fun onPlayerArmorStandManipulateEvent(event: PlayerArmorStandManipulateEvent) {
            if (!event.rightClicked.isVisible && event.rightClicked.getMetadata("deadchest").size != 0) {
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onBlockPlaceEvent(event: BlockPlaceEvent) {
            // Disable double chest for grave chest
            if (event.block.type == Material.CHEST) {

                BlockFace.values()
                    .map { event.block.getRelative(it) }
                    .filter { it.type == Material.CHEST }
                    .forEach { block ->
                        chestData
                            .filter { it.chestLocation == block.location }
                            .forEach { _ ->
                                event.isCancelled = true
                                event.player.sendMessage(local.get("loc_prefix") + local.get("loc_doubleDC"))
                                return@onBlockPlaceEvent
                            }
                    }
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        fun onBlockPistonExtendEvent(event: BlockPistonExtendEvent) {

            event.blocks
                .filterNotNull()
                .filter { it.type == Material.PLAYER_HEAD || it.type == Material.PLAYER_WALL_HEAD }
                .forEach { block ->
                    chestData
                        .filter { it.chestLocation == block.location }
                        .forEach { _ ->
                            event.isCancelled = true
                            return@onBlockPistonExtendEvent
                        }
                }
        }
    }
}
