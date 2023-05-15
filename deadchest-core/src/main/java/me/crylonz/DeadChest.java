package me.crylonz;

import me.crylonz.commands.DCCommandExecutor;
import me.crylonz.commands.DCTabCompletion;
import me.crylonz.deadchest.ChestData;
import me.crylonz.utils.ConfigKey;
import me.crylonz.utils.DeadChestConfig;
import me.crylonz.utils.DeadChestUpdater;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static me.crylonz.DeadChestManager.*;
import static me.crylonz.Utils.generateLog;

public class DeadChest extends JavaPlugin {

    public final static Logger log = Logger.getLogger("Minecraft");
    public static FileManager fileManager;
    public static List<ChestData> chestData;
    public static WorldGuardSoftDependenciesChecker wgsdc = null;
    public static ArrayList<Material> graveBlocks = new ArrayList<>();
    public static Localization local;
    public static Plugin plugin;

    public static boolean bstats = true;
    public static boolean isChangesNeedToBeSave = false;

    public static DeadChestConfig config;

    public DeadChest() {
        super();
    }

    protected DeadChest(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    public void onEnable() {

        ConfigurationSerialization.registerClass(ChestData.class, "ChestData");

        config = new DeadChestConfig(this);
        plugin = this;
        fileManager = new FileManager(this);

        chestData = new ArrayList<>();
        local = new Localization();


        registerConfig();
        initializeConfig();

        if (config.getBoolean(ConfigKey.AUTO_UPDATE)) {
            DeadChestUpdater updater = new DeadChestUpdater(this, 322882, this.getFile(), DeadChestUpdater.UpdateType.DEFAULT, true);
        }

        if (config.getBoolean(ConfigKey.AUTO_CLEANUP_ON_START)) {
            cleanAllDeadChests();
        }

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new DeadChestListener(this), this);

        // Wich block can be used as grave ?
        graveBlocks.add(Material.CHEST);
        graveBlocks.add(Material.PLAYER_HEAD);
        graveBlocks.add(Material.ENDER_CHEST);
        graveBlocks.add(Material.BARREL);
        graveBlocks.add(Material.SHULKER_BOX);


        Objects.requireNonNull(this.getCommand("dc"), "Command dc not found")
                .setExecutor(new DCCommandExecutor(this));

        Objects.requireNonNull(getCommand("dc")).setTabCompleter(new DCTabCompletion());

        if (bstats) {
            Metrics metrics = new Metrics(this, 11385);
        }

        launchRepeatingTask();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.getConfig().getBoolean(ConfigKey.WORLD_GUARD_DETECTION.toString())) {
            try {
                wgsdc = new WorldGuardSoftDependenciesChecker();
                wgsdc.load();
                log.info("[DeadChest] Worldguard detected : Support is enabled");

            } catch (NoClassDefFoundError e) {
                log.info("[DeadChest] Worldguard not detected : Support is disabled");
            }
        } else {
            log.info("[DeadChest] Worldguard support disabled by user");
        }
    }

    public void onDisable() {

        // chest data
        if (fileManager.getChestDataFile().exists()) {
            fileManager.saveChestDataConfig();
        }
    }

    public void registerConfig() {
        config.register(ConfigKey.AUTO_UPDATE.toString(), true);
        config.register(ConfigKey.INDESTRUCTIBLE_CHEST.toString(), true);
        config.register(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST.toString(), true);
        config.register(ConfigKey.DEADCHEST_DURATION.toString(), 300);
        config.register(ConfigKey.MAX_DEAD_CHEST_PER_PLAYER.toString(), 15);
        config.register(ConfigKey.LOG_DEADCHEST_ON_CONSOLE.toString(), false);
        config.register(ConfigKey.REQUIRE_PERMISSION_TO_GENERATE.toString(), false);
        config.register(ConfigKey.REQUIRE_PERMISSION_TO_GET_CHEST.toString(), false);
        config.register(ConfigKey.REQUIRE_PERMISSION_TO_LIST_OWN.toString(), false);
        config.register(ConfigKey.AUTO_CLEANUP_ON_START.toString(), false);
        config.register(ConfigKey.GENERATE_DEADCHEST_IN_CREATIVE.toString(), true);
        config.register(ConfigKey.DISPLAY_POSITION_ON_DEATH.toString(), true);
        config.register(ConfigKey.ITEMS_DROPPED_AFTER_TIMEOUT.toString(), false);
        config.register(ConfigKey.WORLD_GUARD_DETECTION.toString(), false);
        config.register(ConfigKey.DROP_MODE.toString(), 1);
        config.register(ConfigKey.DROP_BLOCK.toString(), 1);
        config.register(ConfigKey.GENERATE_ON_LAVA.toString(), true);
        config.register(ConfigKey.GENERATE_ON_WATER.toString(), true);
        config.register(ConfigKey.GENERATE_ON_RAILS.toString(), true);
        config.register(ConfigKey.GENERATE_IN_MINECART.toString(), true);
        config.register(ConfigKey.EXCLUDED_WORLDS.toString(), Collections.emptyList());
        config.register(ConfigKey.EXCLUDED_ITEMS.toString(), Collections.emptyList());
        config.register(ConfigKey.STORE_XP.toString(), false);
    }

    private void initializeConfig() {

        // plugin config file
        if (!fileManager.getConfigFile().exists()) {
            saveDefaultConfig();
        } else {
            config.updateConfig();
        }

        // database (chestData.yml)
        if (!fileManager.getChestDataFile().exists()) {
            fileManager.saveChestDataConfig();
        } else {

            ArrayList<ChestData> tmp = (ArrayList<ChestData>) fileManager.getChestDataConfig().getList("chestData");

            if (tmp != null) {
                chestData = tmp;
            }
        }

        // locale file for translation
        if (!fileManager.getLocalizationConfigFile().exists()) {
            fileManager.saveLocalizationConfig();
            fileManager.getLocalizationConfig().options().header(
                    "+--------------------------------------------------------------+\n" +
                            "PLEASE REMOVE ALL EXISTING DEADCHESTS BEFORE EDITING THIS FILE\n" +
                            "+--------------------------------------------------------------+\n" +
                            "You can add colors on texts :\n" +
                            "Example '§cHello' will print Hello in red\n" +
                            "§4 : DARK_RED\n" +
                            "§c : RED\n" +
                            "§6 : GOLD\n" +
                            "§e : YELLOW\n" +
                            "§2 : DARK_GREEN\n" +
                            "§a : GREEN\n" +
                            "§b : AQUA\n" +
                            "§3 : DARK_AQUA\n" +
                            "§1 : DARK_BLUE\n" +
                            "§9 : BLUE\n" +
                            "§d : LIGHT_PURPLE\n" +
                            "§5 : DARK_PURPLE\n" +
                            "§f : WHITE\n" +
                            "§7 : GRAY\n" +
                            "§8 : DARK_GRAY\n" +
                            "§0 : BLACK\n" +
                            "+---------------------------------------------------------------+\n" +
                            "You can also add some styling options :\n" +
                            "§l : Text in bold\n" +
                            "§o : Text in italic\n" +
                            "§n : Underline text\n" +
                            "§m : Strike text\n" +
                            "§k : Magic \n" +
                            "+---------------------------------------------------------------+\n" +
                            "Need help ? Join the discord support :\n" +
                            "https://discord.com/invite/jCsvJxS\n" +
                            "+---------------------------------------------------------------+\n"
            );
        } else {
            // if file exist
            // we verify if the file have all translation
            // and add missing if needed

            Map<String, Object> localTmp =
                    Objects.requireNonNull(fileManager.getLocalizationConfig().
                            getConfigurationSection("localisation")).getValues(true);

            for (Map.Entry<String, Object> entry : local.get().entrySet()) {
                localTmp.computeIfAbsent(entry.getKey(), k -> entry.getValue());
            }
            local.set(localTmp);
        }

        fileManager.getLocalizationConfig().createSection("localisation", local.get());
        fileManager.saveLocalizationConfig();
    }

    public static void handleEvent() {
        if (chestData != null && !chestData.isEmpty()) {

            Date now = new Date();
            Iterator<ChestData> chestDataIt = chestData.iterator();

            while (chestDataIt.hasNext()) {
                ChestData chestData = chestDataIt.next();
                World world = chestData.getChestLocation().getWorld();

                if (world != null) {
                    updateTimer(chestData, now);

                    if (handleExpirateDeadChest(chestData, chestDataIt, now)) {
                        isChangesNeedToBeSave = true;
                        generateLog("Deadchest of [" + chestData.getPlayerName() + "] has expired in " + Objects.requireNonNull(chestData.getChestLocation().getWorld()).getName());
                    } else {
                        if (chestData.isChunkLoaded()) {
                            isChangesNeedToBeSave = replaceDeadChestIfItDeseapears(chestData);
                        }
                    }
                }
            }
        }

        if (isChangesNeedToBeSave) {
            fileManager.saveModification();
            isChangesNeedToBeSave = false;
        }
    }

    private void launchRepeatingTask() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, DeadChest::handleEvent, 20, 20);
    }

    public DeadChestConfig getDataConfig() {
        return config;
    }
}