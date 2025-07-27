package me.crylonz.deadchest.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DeadChestConfig {

    private final Plugin plugin;
    private static final HashMap<String, Object> configData = new HashMap<>();
    private final FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File("config.yml"));

    public DeadChestConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    public void register(String key, Object defaultValue) {
        configData.put(key, getFromConfig(key, defaultValue));

    }

    public Boolean getBoolean(ConfigKey key) {
        return (Boolean) configData.get(key.toString());
    }

    public double getDouble(ConfigKey key) {
        return (double) configData.get(key.toString());
    }

    public int getInt(ConfigKey key) {
        return (int) configData.get(key.toString());
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> getArray(ConfigKey key) {
        return (ArrayList<String>) configData.get(key.toString());
    }

    private Object getFromConfig(String paramName, Object defaultValue) {
        Object param = plugin.getConfig().get(paramName);
        if (param != null) {
            return param;
        } else {
            return defaultValue;
        }
    }

    private boolean detectMissingConfigs() {
        plugin.reloadConfig();

        return configData.keySet()
                .stream()
                .anyMatch(key -> !plugin.getConfig().getKeys(true).contains(key));
    }

    public void updateConfig() {
        if (detectMissingConfigs()) {
            plugin.getLogger().warning("Missing configuration found");
            plugin.getLogger().warning("Updating config.yml with missing parameters");

            File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
            file.delete();
            plugin.saveDefaultConfig();

            configData.entrySet()
                    .stream()
                    .filter(config -> plugin.getConfig().get(config.getKey()) != null)
                    .forEach(config -> {
                        plugin.getConfig().set(config.getKey(), config.getValue());
                    });

            plugin.saveConfig();
        }
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }
}