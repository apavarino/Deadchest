package me.crylonz.deadchest;

import me.crylonz.deadchest.commands.DCCommandExecutor;
import me.crylonz.deadchest.commands.DCTabCompletion;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.DeadChestConfig;
//import me.crylonz.deadchest.utils.DeadChestUpdater;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
// You don't strictly need this one for the sounds, but it's good practice
import org.bukkit.Location;
import org.bukkit.ChatColor;
import java.util.Comparator;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static me.crylonz.deadchest.DeadChestManager.*;
import static me.crylonz.deadchest.Utils.generateLog;

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

    public List<Warning> timedWarnings;

    static {
        ConfigurationSerialization.registerClass(ChestData.class, "ChestData");
    }

    public DeadChest() {
        super();
    }

    protected DeadChest(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    public void onEnable() {

        config = new DeadChestConfig(this);
        plugin = this;
        fileManager = new FileManager(this);

        chestData = new ArrayList<>();
        local = new Localization();

        registerConfig();
        initializeConfig();
        loadWarnings();

        //if (config.getBoolean(ConfigKey.AUTO_UPDATE)) {
        //    DeadChestUpdater updater = new DeadChestUpdater(this, 322882, this.getFile(), DeadChestUpdater.UpdateType.DEFAULT, true);
        //}

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

        //if (bstats) {
        //    Metrics metrics = new Metrics(this, 11385);
        //}

        launchRepeatingTask();
    }

    private void loadWarnings() {
        timedWarnings = new ArrayList<>();
        List<Map<?, ?>> warningMaps = getConfig().getMapList("warnings.timed");
        if (warningMaps == null) return;

        for (Map<?, ?> map : warningMaps) {
            if (map.containsKey("time") && map.containsKey("sound") && map.containsKey("message")) {
                int time = (int) map.get("time");
                String sound = (String) map.get("sound");
                String message = (String) map.get("message");
                timedWarnings.add(new Warning(time, sound, message));
            }
        }
        // Sort warnings from highest time to lowest
        timedWarnings.sort(Comparator.comparingInt(Warning::time).reversed());
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
        config.register(ConfigKey.ATTEMPT_RE_EQUIP.toString(), true);
        config.register(ConfigKey.CLICK_RETRIEVAL_MODE.toString(), 1);
        config.register(ConfigKey.CLICK_OVERFLOW_DROP_LOCATION.toString(), 1);
        config.register(ConfigKey.AUTO_UPDATE.toString(), false);
        config.register(ConfigKey.INDESTRUCTIBLE_CHEST.toString(), true);
        config.register(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST.toString(), true);
        config.register(ConfigKey.DEADCHEST_DURATION.toString(), 1800);
        config.register(ConfigKey.MAX_DEAD_CHEST_PER_PLAYER.toString(), 0);
        config.register(ConfigKey.LOG_DEADCHEST_ON_CONSOLE.toString(), false);
        config.register(ConfigKey.REQUIRE_PERMISSION_TO_GENERATE.toString(), false);
        config.register(ConfigKey.REQUIRE_PERMISSION_TO_GET_CHEST.toString(), false);
        config.register(ConfigKey.REQUIRE_PERMISSION_TO_LIST_OWN.toString(), false);
        config.register(ConfigKey.AUTO_CLEANUP_ON_START.toString(), false);
        config.register(ConfigKey.GENERATE_DEADCHEST_IN_CREATIVE.toString(), true);
        config.register(ConfigKey.DISPLAY_POSITION_ON_DEATH.toString(), true);
        config.register(ConfigKey.ITEMS_DROPPED_AFTER_TIMEOUT.toString(), false);
        config.register(ConfigKey.WORLD_GUARD_DETECTION.toString(), false);
        config.register(ConfigKey.DROP_BLOCK.toString(), 2);
        config.register(ConfigKey.ITEM_DURABILITY_LOSS_ON_DEATH.toString(), 0);
        config.register(ConfigKey.GENERATE_ON_LAVA.toString(), true);
        config.register(ConfigKey.GENERATE_ON_WATER.toString(), true);
        config.register(ConfigKey.GENERATE_ON_RAILS.toString(), true);
        config.register(ConfigKey.GENERATE_IN_MINECART.toString(), true);
        config.register(ConfigKey.GENERATE_IN_THE_END.toString(), true);
        config.register(ConfigKey.EXCLUDED_WORLDS.toString(), Collections.emptyList());
        config.register(ConfigKey.EXCLUDED_ITEMS.toString(), Collections.emptyList());
        config.register(ConfigKey.IGNORED_ITEMS.toString(), Collections.emptyList());
        config.register(ConfigKey.STORE_XP.toString(), 2);
        config.register(ConfigKey.STORE_XP_PERCENTAGE.toString(), 10);
        config.register(ConfigKey.KEEP_INVENTORY_ON_PVP_DEATH.toString(), false);
        config.register(ConfigKey.SUSPEND_COUNTDOWNS_WHEN_PLAYER_IS_OFFLINE.toString(), true);
        config.register(ConfigKey.EXPIRE_ACTION.toString(), 3);
    }

    private void initializeConfig() {
        if (!fileManager.getConfigFile().exists()) {
            saveDefaultConfig();
        } else {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        if (!fileManager.getChestDataFile().exists()) {
            fileManager.saveChestDataConfig();
        } else {
            @SuppressWarnings("unchecked")
            ArrayList<ChestData> tmp = (ArrayList<ChestData>) fileManager.getChestDataConfig().get("chestData");

            if (tmp != null) {
                chestData = tmp;
            }
        }
    }

    public void handleEvent() {
        if (chestData == null || chestData.isEmpty() || timedWarnings == null) {
            return;
        }

        Date now = new Date();
        Iterator<ChestData> chestDataIt = chestData.iterator();

        while (chestDataIt.hasNext()) {
            ChestData chest = chestDataIt.next();
            if (chest.getChestLocation().getWorld() == null) continue;

            Player player = Bukkit.getPlayer(UUID.fromString(chest.getPlayerUUID()));
            boolean isPlayerOnline = (player != null && player.isOnline());
            boolean suspend = config.getBoolean(ConfigKey.SUSPEND_COUNTDOWNS_WHEN_PLAYER_IS_OFFLINE);

            if (suspend && isPlayerOnline) {
                chest.setTimeRemaining(chest.getTimeRemaining() - 1);
            } else if (!suspend) {
                long timeSinceCreation = (now.getTime() - chest.getChestDate().getTime()) / 1000;
                chest.setTimeRemaining(config.getInt(ConfigKey.DEADCHEST_DURATION) - timeSinceCreation);
            }

            updateTimer(chest, now);

            if (isPlayerOnline) {
                for (Warning warning : timedWarnings) {
                    if (chest.getTimeRemaining() <= warning.time() && !chest.getTriggeredWarnings().contains(warning.time())) {
                        try {
                            player.playSound(player.getLocation(), Sound.valueOf(warning.sound().toUpperCase()), 1.0f, 1.0f);
                            if (!warning.message().isEmpty()) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', warning.message()));
                            }
                        } catch (IllegalArgumentException ex) {
                            getLogger().warning("Invalid sound name in config for warning at " + warning.time() + "s: " + warning.sound());
                        }
                        chest.getTriggeredWarnings().add(warning.time());
                        isChangesNeedToBeSave = true;
                    }
                }
            }

            if (handleExpirateDeadChest(chest, chestDataIt)) {
                isChangesNeedToBeSave = true;
                generateLog("Deadchest of [" + chest.getPlayerName() + "] has expired in " + Objects.requireNonNull(chest.getChestLocation().getWorld()).getName());
            } else {
                if (chest.isChunkLoaded()) {
                    isChangesNeedToBeSave = replaceDeadChestIfItDeseapears(chest);
                }
            }
        }

        if (isChangesNeedToBeSave) {
            fileManager.saveModification();
            isChangesNeedToBeSave = false;
        }
    }

    private void launchRepeatingTask() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::handleEvent, 20, 20);
    }

    public DeadChestConfig getDataConfig() {
        return config;
    }
}