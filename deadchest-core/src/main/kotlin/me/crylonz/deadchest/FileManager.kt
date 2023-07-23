package me.crylonz.deadchest

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException

class FileManager(plugin: Plugin) {

    val configFile: File = File(plugin.dataFolder, "config.yml")
    val localizationConfigFile: File = File(plugin.dataFolder, localizationFileName)
    val chestDataConfigFile: File = File(plugin.dataFolder, chestDataFileName)

    fun reloadChestDataConfig() {
        Companion.chestDataConfig = YamlConfiguration.loadConfiguration(chestDataConfigFile)
    }

    val chestDataConfig: FileConfiguration
        get() {
            if (Companion.chestDataConfig == null) {
                reloadChestDataConfig()
            }
            return Companion.chestDataConfig!!
        }

    fun saveChestDataConfig() {
        if (Companion.chestDataConfig == null) {
            return
        }
        try {
            this.chestDataConfig.save(chestDataConfigFile)
        } catch (ex: IOException) {
            DeadChest.plugin.logger.severe("Could not save config to $chestDataConfigFile$ex")
        }
    }

    fun reloadLocalizationConfig() {
        Companion.localizationConfig = YamlConfiguration.loadConfiguration(localizationConfigFile)
    }

    val localizationConfig: FileConfiguration
        get() {
            if (Companion.localizationConfig == null) {
                reloadLocalizationConfig()
            }
            return Companion.localizationConfig!!
        }

    fun saveLocalizationConfig() {
        if (Companion.localizationConfig == null) {
            return
        }
        try {
            this.localizationConfig.save(localizationConfigFile)
        } catch (ex: IOException) {
            DeadChest.plugin.logger.severe("Could not save config to $localizationConfigFile$ex")
        }
    }

    // custom func
    fun saveModification() {
        this.chestDataConfig["chestData"] = DeadChest.chestData
        saveChestDataConfig()
    }

    companion object {
        private var chestDataConfig: FileConfiguration? = null
        private const val chestDataFileName = "chestData.yml"

        private var localizationConfig: FileConfiguration? = null
        private const val localizationFileName = "locale.yml"

    }
}

