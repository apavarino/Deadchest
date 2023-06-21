package me.crylonz.deadchest

import me.crylonz.DeadChestListener
import me.crylonz.DeadChestManager.*
import me.crylonz.FileManager
import me.crylonz.Utils.generateLog
import me.crylonz.deadchest.DeadChestUpdater.UpdateType
import me.crylonz.deadchest.commands.DCCommandExecutor
import me.crylonz.deadchest.commands.DCTabCompletion
import me.crylonz.deadchest.utils.ConfigKey.*
import me.crylonz.deadchest.utils.DeadChestConfig
import org.bstats.bukkit.Metrics
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

open class DeadChest : JavaPlugin() {

    companion object {
        var bstats = true
        var isChangesNeedToBeSave = false

        lateinit var chestData: MutableList<ChestData>
        lateinit var plugin: Plugin
        lateinit var local: Localization
        lateinit var dcConfig: DeadChestConfig
        lateinit var fileManager: FileManager
        lateinit var worldGuardDependenciesChecker: WorldGuardSoftDependenciesChecker
        lateinit var graveBlocks: MutableList<Material>
    }

    override fun onEnable() {
        ConfigurationSerialization.registerClass(ChestData::class.java, "ChestData")

        chestData = mutableListOf();
        local = Localization()
        plugin = this
        dcConfig = DeadChestConfig(this)
        fileManager = FileManager(this)

        registerConfig()
        initializeConfig()

        if (dcConfig.getBoolean(AUTO_UPDATE)) {
            DeadChestUpdater(this, 322882, file, UpdateType.DEFAULT, true)
        }

        if (dcConfig.getBoolean(AUTO_CLEANUP_ON_START)) {
            cleanAllDeadChests()
        }

        val pm = server.pluginManager
        pm.registerEvents(DeadChestListener(this), this)

        getCommand("dc")?.setExecutor(DCCommandExecutor(this))

        getCommand("dc")?.tabCompleter = DCTabCompletion()

        if (bstats) {
            Metrics(this, 11385)
        }

        // Wich block can be used as grave ?
        graveBlocks = mutableListOf(
            Material.CHEST,
            Material.PLAYER_HEAD,
            Material.ENDER_CHEST,
            Material.BARREL,
            Material.SHULKER_BOX
        )

        launchRepeatingTask()
    }

    override fun onLoad() {
        super.onLoad()
        if (this.config.getBoolean(WORLD_GUARD_DETECTION.toString())) {
            try {
                worldGuardDependenciesChecker = WorldGuardSoftDependenciesChecker()
                worldGuardDependenciesChecker.load()
                logger.info("[DeadChest] Worldguard detected : Support is enabled")
            } catch (e: NoClassDefFoundError) {
                logger.info("[DeadChest] Worldguard not detected : Support is disabled")
            }
        } else {
            logger.info("[DeadChest] Worldguard support disabled by user")
        }
    }

    override fun onDisable() {

        // chest data
        if (fileManager.chestDataFile.exists()) {
            fileManager.saveChestDataConfig()
        }
    }

    fun registerConfig() {
        dcConfig.register(AUTO_UPDATE.toString(), true)
        dcConfig.register(INDESTRUCTIBLE_CHEST.toString(), true)
        dcConfig.register(ONLY_OWNER_CAN_OPEN_CHEST.toString(), true)
        dcConfig.register(DEADCHEST_DURATION.toString(), 300)
        dcConfig.register(MAX_DEAD_CHEST_PER_PLAYER.toString(), 15)
        dcConfig.register(LOG_DEADCHEST_ON_CONSOLE.toString(), false)
        dcConfig.register(REQUIRE_PERMISSION_TO_GENERATE.toString(), false)
        dcConfig.register(REQUIRE_PERMISSION_TO_GET_CHEST.toString(), false)
        dcConfig.register(REQUIRE_PERMISSION_TO_LIST_OWN.toString(), false)
        dcConfig.register(AUTO_CLEANUP_ON_START.toString(), false)
        dcConfig.register(GENERATE_DEADCHEST_IN_CREATIVE.toString(), true)
        dcConfig.register(DISPLAY_POSITION_ON_DEATH.toString(), true)
        dcConfig.register(ITEMS_DROPPED_AFTER_TIMEOUT.toString(), false)
        dcConfig.register(WORLD_GUARD_DETECTION.toString(), false)
        dcConfig.register(DROP_MODE.toString(), 1)
        dcConfig.register(DROP_BLOCK.toString(), 1)
        dcConfig.register(GENERATE_ON_LAVA.toString(), true)
        dcConfig.register(GENERATE_ON_WATER.toString(), true)
        dcConfig.register(GENERATE_ON_RAILS.toString(), true)
        dcConfig.register(GENERATE_IN_MINECART.toString(), true)
        dcConfig.register(EXCLUDED_WORLDS.toString(), emptyList<Any>())
        dcConfig.register(EXCLUDED_ITEMS.toString(), emptyList<Any>())
        dcConfig.register(STORE_XP.toString(), false)
    }

    private fun initializeConfig() {

        // plugin config file
        if (!fileManager.configFile.exists()) {
            saveDefaultConfig()
        } else {
            dcConfig.updateConfig()
        }

        // database (chestData.yml)
        if (!fileManager.chestDataFile.exists()) {
            fileManager.saveChestDataConfig()
        } else {
            fileManager.chestDataConfig.getList("chestData")
                ?.let { chestData = it as MutableList<ChestData> }
        }

        // locale file for translation
        if (!fileManager.localizationConfigFile.exists()) {
            fileManager.saveLocalizationConfig()
            fileManager.localizationConfig.options().header(
                """
                +--------------------------------------------------------------+
                PLEASE REMOVE ALL EXISTING DEADCHESTS BEFORE EDITING THIS FILE
                +--------------------------------------------------------------+
                You can add colors on texts :
                Example '§cHello' will print Hello in red
                §4 : DARK_RED
                §c : RED
                §6 : GOLD
                §e : YELLOW
                §2 : DARK_GREEN
                §a : GREEN
                §b : AQUA
                §3 : DARK_AQUA
                §1 : DARK_BLUE
                §9 : BLUE
                §d : LIGHT_PURPLE
                §5 : DARK_PURPLE
                §f : WHITE
                §7 : GRAY
                §8 : DARK_GRAY
                §0 : BLACK
                +---------------------------------------------------------------+
                You can also add some styling options :
                §l : Text in bold
                §o : Text in italic
                §n : Underline text
                §m : Strike text
                §k : Magic
                +---------------------------------------------------------------+
                Need help ? Join the discord support :
                https://discord.com/invite/jCsvJxS
                +---------------------------------------------------------------+
            """.trimIndent()
            )
        } else {

            // if file exist
            // we verify if the file have all translation
            // and add missing if needed

            fileManager.localizationConfig.getConfigurationSection("localisation")
                ?.getValues(true)
                ?.let { localFromFile ->
                    local.get().map { localFromFile.computeIfAbsent(it.key) { _: String -> it.value } }
                    local.set(localFromFile)
                }

        }
        fileManager.localizationConfig.createSection("localisation", local.get())
        fileManager.saveLocalizationConfig()
    }

    fun handleEvent() {
        if (chestData.isNotEmpty()) {

            val now = Date()

            chestData.forEach {
                it.chestLocation.world?.let { _ ->
                    updateTimer(it, now)

                    if (handleExpirateDeadChest(it, now)) {
                        isChangesNeedToBeSave = true
                        generateLog("Deadchest of  ${it.playerName}] has expired in ${it.chestLocation.world!!.name}")
                    } else if (it.isChunkLoaded()) {
                        isChangesNeedToBeSave = replaceDeadChestIfItDeseapears(it)
                    }
                }
            }
        }
        if (isChangesNeedToBeSave) {
            fileManager.saveModification()
            isChangesNeedToBeSave = false
        }
    }

    private fun launchRepeatingTask() {
        server.scheduler.scheduleSyncRepeatingTask(this, { handleEvent() }, 20, 20)
    }
}