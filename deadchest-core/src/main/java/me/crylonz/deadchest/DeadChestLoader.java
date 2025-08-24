package me.crylonz.deadchest;

import me.crylonz.deadchest.commands.DCCommandExecutor;
import me.crylonz.deadchest.commands.DCTabCompletion;
import me.crylonz.deadchest.db.ChestDataRepository;
import me.crylonz.deadchest.db.IgnoreItemListRepository;
import me.crylonz.deadchest.db.SQLExecutor;
import me.crylonz.deadchest.db.SQLite;
import me.crylonz.deadchest.deps.worldguard.WorldGuardSoftDependenciesChecker;
import me.crylonz.deadchest.listener.*;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.DeadChestConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

import static me.crylonz.deadchest.DeadChestManager.*;
import static me.crylonz.deadchest.db.IgnoreItemListRepository.loadIgnoreIntoInventory;
import static me.crylonz.deadchest.legacy.OldChestData.migrateOldChestData;
import static me.crylonz.deadchest.utils.Utils.generateLog;

public class DeadChestLoader {

    public static Logger log = Logger.getLogger("Minecraft");
    public static FileManager fileManager;
    public static List<ChestData> chestDataList;
    public static WorldGuardSoftDependenciesChecker wgsdc = null;
    public static ArrayList<Material> graveBlocks = new ArrayList<>();
    public static Localization local;
    public static JavaPlugin javaPlugin;
    public static Plugin plugin;

    public static Inventory ignoreList;

    public static boolean bstats = true;
    public static boolean isChangesNeedToBeSave = false;

    public static DeadChestConfig config;

    public static SQLite db;
    public static SQLExecutor sqlExecutor = new SQLExecutor();

//    static {
//        ConfigurationSerialization.registerClass(ChestData.class, "ChestData");
//    }

    public DeadChestLoader(Plugin dcPlugin, JavaPlugin dcjavaPlugin) {
        super();
        javaPlugin = dcjavaPlugin;
        plugin = dcPlugin;
    }

    public void enable() {

        // db parts
        db = new SQLite(plugin);
        db.init();
        IgnoreItemListRepository.initTable();
        ChestDataRepository.initTable();

        ignoreList = Bukkit.createInventory(new IgnoreInventoryHolder(), 36, "Ignore list");
        config = new DeadChestConfig(plugin);
        fileManager = new FileManager(plugin);

        chestDataList = new ArrayList<>();
        local = new Localization();

        registerConfig();
        initializeConfig();

        if (config.getBoolean(ConfigKey.AUTO_CLEANUP_ON_START)) {
            cleanAllDeadChests();
        }

        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new ArmorstandListener(), plugin);
        pm.registerEvents(new BlockBreakListener(), plugin);
        pm.registerEvents(new BlockFromToListener(), plugin);
        pm.registerEvents(new BlockPlaceEventListener(), plugin);
        pm.registerEvents(new ClickListener(), plugin);
        pm.registerEvents(new ExplosionListener(), plugin);
        pm.registerEvents(new InventoryClickListener(), plugin);
        pm.registerEvents(new PistonListener(), plugin);
        pm.registerEvents(new PlayerDeathListener(), plugin);

        // Which block can be used as grave ?
        graveBlocks.add(Material.CHEST);
        graveBlocks.add(Material.PLAYER_HEAD);
        graveBlocks.add(Material.ENDER_CHEST);
        graveBlocks.add(Material.BARREL);
        graveBlocks.add(Material.SHULKER_BOX);


        Objects.requireNonNull(javaPlugin.getCommand("dc"), "Command dc not found")
                .setExecutor(new DCCommandExecutor(this));

        Objects.requireNonNull(javaPlugin.getCommand("dc")).setTabCompleter(new DCTabCompletion());

        launchRepeatingTask();
    }


    public void load() {
        if (javaPlugin.getConfig().getBoolean(ConfigKey.WORLD_GUARD_DETECTION.toString())) {
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

    public void disable() {

        ChestDataRepository.saveAllAsync(chestDataList);
        sqlExecutor.shutdown();
        db.close();
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
        config.register(ConfigKey.ITEM_DURABILITY_LOSS_ON_DEATH.toString(), 0);
        config.register(ConfigKey.GENERATE_ON_LAVA.toString(), true);
        config.register(ConfigKey.GENERATE_ON_WATER.toString(), true);
        config.register(ConfigKey.GENERATE_ON_RAILS.toString(), true);
        config.register(ConfigKey.GENERATE_IN_MINECART.toString(), true);
        config.register(ConfigKey.GENERATE_IN_THE_END.toString(), true);
        config.register(ConfigKey.EXCLUDED_WORLDS.toString(), Collections.emptyList());
        config.register(ConfigKey.EXCLUDED_ITEMS.toString(), Collections.emptyList());
        config.register(ConfigKey.IGNORED_ITEMS.toString(), Collections.emptyList());
        config.register(ConfigKey.STORE_XP.toString(), false);
        config.register(ConfigKey.STORE_XP_PERCENTAGE.toString(), 100);
        config.register(ConfigKey.KEEP_INVENTORY_ON_PVP_DEATH.toString(), false);
    }

    private void initializeConfig() {

        // plugin config file
        if (!fileManager.getConfigFile().exists()) {
            plugin.saveDefaultConfig();
        } else {
            config.updateConfig();
        }

        // database (chestData)
        chestDataList = ChestDataRepository.findAll();

        // migrate old chestData.yml config
        migrateOldChestData();

        // ignore list
        loadIgnoreIntoInventory(ignoreList);

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
        if (chestDataList != null && !chestDataList.isEmpty()) {

            Date now = new Date();
            Iterator<ChestData> chestDataIt = chestDataList.iterator();

            while (chestDataIt.hasNext()) {
                ChestData chestData = chestDataIt.next();
                if (chestData == null) {
                    generateLog("Deadchest of [null] has no invalid data set. Get removed.");
                    chestDataIt.remove();
                    continue;
                }
                World world = chestData.getChestLocation().getWorld();

                if (world != null) {
                    updateTimer(chestData, now);

                    if (handleExpirateDeadChest(chestData, chestDataIt, now)) {
                        isChangesNeedToBeSave = true;
                        generateLog("Deadchest of [" + chestData.getPlayerName() + "] has expired in " + Objects.requireNonNull(chestData.getChestLocation().getWorld()).getName());
                    } else {
                        if (chestData.isChunkLoaded()) {
                            isChangesNeedToBeSave = replaceDeadChestIfItDisappears(chestData);
                        }
                    }
                }
            }
        }

        if (isChangesNeedToBeSave) {
            ChestDataRepository.saveAllAsync(chestDataList);
            isChangesNeedToBeSave = false;
        }
    }

    private void launchRepeatingTask() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, DeadChestLoader::handleEvent, 20, 20);
    }

    public DeadChestConfig getDataConfig() {
        return config;
    }
}