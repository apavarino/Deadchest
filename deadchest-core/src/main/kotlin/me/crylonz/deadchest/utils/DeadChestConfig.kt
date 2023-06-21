package me.crylonz.deadchest.utils

import org.bukkit.plugin.Plugin
import java.io.File

class DeadChestConfig(private val plugin: Plugin) {

    fun register(key: String, defaultValue: Any) {
        configData[key] = getFromConfig(key, defaultValue)
    }

    fun getBoolean(key: ConfigKey): Boolean {
        return configData[key.toString()] as Boolean
    }

    fun getDouble(key: ConfigKey): Double {
        return configData[key.toString()] as Double
    }

    fun getInt(key: ConfigKey): Int {
        return configData[key.toString()] as Int
    }

    fun getArray(key: ConfigKey): ArrayList<String> {
        return configData[key.toString()] as ArrayList<String>
    }

    private fun getFromConfig(paramName: String, defaultValue: Any): Any {
        val param = plugin.config[paramName]
        return param ?: defaultValue
    }

    private fun detectMissingConfigs(): Boolean {
        plugin.reloadConfig()
        return configData.keys
            .stream()
            .anyMatch { key: String? ->
                !plugin.config.getKeys(true).contains(key)
            }
    }

    fun updateConfig() {
        if (detectMissingConfigs()) {
            plugin.logger.warning("Missing configuration found")
            plugin.logger.warning("Updating config.yml with missing parameters")
            val file = File(plugin.dataFolder.absolutePath + File.separator + "config.yml")
            file.delete()
            plugin.saveDefaultConfig()
            configData.entries
                .stream()
                .filter { (key): Map.Entry<String, Any> ->
                    plugin.config[key] != null
                }
                .forEach { (key, value): Map.Entry<String, Any> ->
                    plugin.config[key] = value
                }
            plugin.saveConfig()
        }
    }

    companion object {
        private val configData = HashMap<String, Any>()
    }
}
