package me.crylonz;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

import static me.crylonz.DeadChest.chestData;

public class FileManager {

    private File configFile;

    private static FileConfiguration config2 = null;
    private static File config2File = null;
    private static String config2FileName = "chestData.yml";

    private static FileConfiguration config3 = null;
    private static File config3File = null;
    private static String config3FileName = "locale.yml";


    private Plugin p;

    // Constructor
    public FileManager(Plugin p) {
        this.p = p;
        configFile = new File(p.getDataFolder(), "config.yml");
        config2File = new File(p.getDataFolder(), config2FileName);
        config3File = new File(p.getDataFolder(), config3FileName);
    }

    // Default config
    public File getConfigFile() {
        return configFile;
    }

    // Config 2
    public File getConfig2File() {
        return config2File;
    }

    public void reloadConfig2() {
        if (config2File == null) {
            config2File = new File(p.getDataFolder(), config2FileName);
        }
        config2 = YamlConfiguration.loadConfiguration(config2File);
    }

    public FileConfiguration getConfig2() {
        if (config2 == null) {
            reloadConfig2();
        }
        return config2;
    }

    public void saveConfig2() {
        if (config2 == null || config2File == null) {
            return;
        }
        try {
            getConfig2().save(config2File);
        } catch (IOException ex) {
            DeadChest.log.severe("Could not save config to " + config2File + ex);
        }
    }

    // Config 3
    public File getConfig3File() {
        return config3File;
    }

    public void reloadConfig3() {
        if (config3File == null) {
            config3File = new File(p.getDataFolder(), config3FileName);
        }
        config3 = YamlConfiguration.loadConfiguration(config3File);
    }

    public FileConfiguration getConfig3() {
        if (config3 == null) {
            reloadConfig3();
        }
        return config3;
    }

    public void saveConfig3() {
        if (config3 == null || config3File == null) {
            return;
        }
        try {
            getConfig3().save(config3File);
        } catch (IOException ex) {
            DeadChest.log.severe("Could not save config to " + config3File + ex);
        }
    }

    // custom func
    public void saveModification() {
        getConfig2().set("chestData", chestData);
        saveConfig2();
    }

}
