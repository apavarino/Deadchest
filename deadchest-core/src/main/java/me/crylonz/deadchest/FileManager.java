package me.crylonz.deadchest;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class FileManager {

    private final File configFile;

    private static FileConfiguration chestDataConfig = null;
    private static File chestDataFile = null;

    @Deprecated
    private static final String chestDataFileName = "chestData.yml";

    private static FileConfiguration localizationConfig = null;
    private static File localizationFile = null;
    private static final String localizationFileName = "locale.yml";

    private final Plugin p;

    // Constructor
    public FileManager(Plugin p) {
        this.p = p;
        configFile = new File(p.getDataFolder(), "config.yml");
        chestDataFile = new File(p.getDataFolder(), chestDataFileName);
        localizationFile = new File(p.getDataFolder(), localizationFileName);
    }

    // Default config
    public File getConfigFile() {
        return configFile;
    }

    // Config 2
    @Deprecated
    public File getChestDataFile() {
        return chestDataFile;
    }

    @Deprecated
    public void reloadChestDataConfig() {
        if (chestDataFile == null) {
            chestDataFile = new File(p.getDataFolder(), chestDataFileName);
        }
        chestDataConfig = YamlConfiguration.loadConfiguration(chestDataFile);
    }

    @Deprecated
    public FileConfiguration getChestDataConfig() {
        if (chestDataConfig == null) {
            reloadChestDataConfig();
        }
        return chestDataConfig;
    }

    @Deprecated
    public void saveChestDataConfig() {
        if (chestDataConfig == null || chestDataFile == null) {
            return;
        }
        try {
            getChestDataConfig().save(chestDataFile);
        } catch (IOException ex) {
            DeadChestLoader.log.severe("Could not save config to " + chestDataFile + ex);
        }
    }

    // Config 3
    public File getLocalizationConfigFile() {
        return localizationFile;
    }

    public void reloadLocalizationConfig() {
        if (localizationFile == null) {
            localizationFile = new File(p.getDataFolder(), localizationFileName);
        }
        localizationConfig = YamlConfiguration.loadConfiguration(localizationFile);
    }

    public FileConfiguration getLocalizationConfig() {
        if (localizationConfig == null) {
            reloadLocalizationConfig();
        }
        return localizationConfig;
    }

    public void saveLocalizationConfig() {
        if (localizationConfig == null || localizationFile == null) {
            return;
        }
        try {
            getLocalizationConfig().save(localizationFile);
        } catch (IOException ex) {
            DeadChestLoader.log.severe("Could not save config to " + localizationFile + ex);
        }
    }
}
