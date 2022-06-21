package me.crylonz;

import me.crylonz.commands.DCCommandExecutor;
import me.crylonz.commands.TabCompletion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

    // Config
    public static boolean isIndestructible = true;
    public static boolean OnlyOwnerCanOpenDeadChest = true;
    public static int chestDuration = 600;
    public static int maxDeadChestPerPlayer = 5;
    public static boolean logDeadChestOnConsole = false;
    public static boolean requirePermissionToGenerate = false;
    public static boolean requirePermissionToGetChest = false;
    public static boolean permissionRequiredToListOwn = false;
    public static boolean autoCleanUpOnStart = false;
    public static boolean generateDeadChestInCreative = true;
    public static boolean displayDeadChestPositionOnDeath = true;
    public static int dropMode = 1;
    public static int dropBlock = 1;
    public static ArrayList<String> excludedWorlds = new ArrayList<>();
    public static ArrayList<String> excludedItems = new ArrayList<>();
    public static boolean itemsDroppedAfterTimeOut = false;
    public static boolean enableWorldGuardDetection = false;
    public static boolean generateOnLava = true;
    public static boolean generateOnWater = true;
    public static boolean generateOnRails = true;
    public static boolean generateInMinecart = true;
    public static boolean bstats = true;
    public static boolean isChangesNeedToBeSave = false;

    static {
        ConfigurationSerialization.registerClass(ChestData.class, "ChestData");
    }

    public DeadChest() {
        super();
    }

    protected DeadChest(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
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

    public void onEnable() {

        plugin = this;
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new DeadChestListener(), this);
        fileManager = new FileManager(this);

        // Wich block can be used as grave ?
        graveBlocks.add(Material.CHEST);
        graveBlocks.add(Material.PLAYER_HEAD);
        graveBlocks.add(Material.ENDER_CHEST);
        graveBlocks.add(Material.BARREL);
        graveBlocks.add(Material.SHULKER_BOX);

        Objects.requireNonNull(getCommand("dc")).setTabCompleter(new TabCompletion());

        if (bstats) {
            Metrics metrics = new Metrics(this, 11385);
        }

        chestData = new ArrayList<>();
        local = new Localization();

        Objects.requireNonNull(this.getCommand("dc"), "Command dc not found")
                .setExecutor(new DCCommandExecutor(this));

        initializeConfig();

        if (autoCleanUpOnStart) {
            cleanAllDeadChests();
        }
        launchRepeatingTask();
    }

    @Override
    public void onLoad() {

        // to be sure that data is loaded when call onLoad()
        enableWorldGuardDetection = (boolean) getConfig().get("EnableWorldGuardDetection");


        if (enableWorldGuardDetection) {

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

    private void initializeConfig() {

        // plugin config file
        if (!fileManager.getConfigFile().exists()) {
            saveDefaultConfig();

        } else {

            autoUpdateConfigFile();

            @SuppressWarnings("unchecked")
            ArrayList<ChestData> tmp = (ArrayList<ChestData>) fileManager.getChestDataConfig().get("chestData");

            @SuppressWarnings("unchecked")
            ArrayList<String> tmpExludedWorld = (ArrayList<String>) getConfig().get("ExcludedWorld");
            ArrayList<String> tmpExcludedItems = (ArrayList<String>) getConfig().get("ExcludedItems");

            if (tmp != null)
                chestData = tmp;

            if (tmpExludedWorld != null)
                excludedWorlds = tmpExludedWorld;

            if (tmpExcludedItems != null)
                excludedItems = tmpExcludedItems;

            isIndestructible = getConfig().getBoolean("IndestuctibleChest");
            OnlyOwnerCanOpenDeadChest = getConfig().getBoolean("OnlyOwnerCanOpenDeadChest");
            chestDuration = getConfig().getInt("DeadChestDuration");
            maxDeadChestPerPlayer = (int) getConfig().get("maxDeadChestPerPlayer");
            logDeadChestOnConsole = (boolean) getConfig().get("logDeadChestOnConsole");
            requirePermissionToGenerate = (boolean) getConfig().get("RequirePermissionToGenerate");
            requirePermissionToGetChest = (boolean) getConfig().get("RequirePermissionToGetChest");
            permissionRequiredToListOwn = (boolean) getConfig().get("RequirePermissionToListOwn");
            autoCleanUpOnStart = (boolean) getConfig().get("AutoCleanupOnStart");
            generateDeadChestInCreative = (boolean) getConfig().get("GenerateDeadChestInCreative");
            displayDeadChestPositionOnDeath = (boolean) getConfig().get("DisplayDeadChestPositionOnDeath");
            itemsDroppedAfterTimeOut = (boolean) getConfig().get("ItemsDroppedAfterTimeOut");
            enableWorldGuardDetection = (boolean) getConfig().get("EnableWorldGuardDetection");
            dropMode = (int) getConfig().get("DropMode");
            dropBlock = (int) getConfig().get("DropBlock");
            generateOnLava = (boolean) getConfig().get("GenerateOnLava");
            generateOnWater = (boolean) getConfig().get("GenerateOnWater");
            generateOnRails = (boolean) getConfig().get("GenerateOnRails");
            generateInMinecart = (boolean) getConfig().get("GenerateInMinecart");
        }

        // database (chestData.yml)
        if (!fileManager.getChestDataFile().exists()) {
            fileManager.saveChestDataConfig();
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
            // we verify if the file have all traduction
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

    private void launchRepeatingTask() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, DeadChest::handleEvent, 20, 20);
    }

    // Add missing parameters on config.yml
    public void autoUpdateConfigFile() {
        reloadConfig();
        ArrayList<String> allConfigPath = new ArrayList<>();
        allConfigPath.add("IndestuctibleChest");
        allConfigPath.add("OnlyOwnerCanOpenDeadChest");
        allConfigPath.add("DeadChestDuration");
        allConfigPath.add("maxDeadChestPerPlayer");
        allConfigPath.add("logDeadChestOnConsole");
        allConfigPath.add("RequirePermissionToGenerate");
        allConfigPath.add("RequirePermissionToGetChest");
        allConfigPath.add("RequirePermissionToListOwn");
        allConfigPath.add("AutoCleanupOnStart");
        allConfigPath.add("GenerateDeadChestInCreative");
        allConfigPath.add("DisplayDeadChestPositionOnDeath");
        allConfigPath.add("DropMode");
        allConfigPath.add("DropBlock");
        allConfigPath.add("ExcludedWorld");
        allConfigPath.add("ExcludedItems");
        allConfigPath.add("ItemsDroppedAfterTimeOut");
        allConfigPath.add("EnableWorldGuardDetection");
        allConfigPath.add("GenerateOnLava");
        allConfigPath.add("GenerateOnWater");
        allConfigPath.add("GenerateOnRails");
        allConfigPath.add("GenerateInMinecart");

        for (String path : allConfigPath) {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(fileManager.getConfigFile());
            if (configuration.get(path) == null) {
                File file = new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml");
                File oldFile = new File(getDataFolder().getAbsolutePath() + File.separator + "config.old.yml");
                file.renameTo(oldFile);
                saveDefaultConfig();
                log.warning("---------------------------------------");
                log.warning("");
                log.warning("[DeadChest] Configuration update detected !");
                log.warning("[DeadChest] Config back to default : Don't forget to update your config.yml");
                log.warning("[DeadChest] Your old config is in config.old.yml");
                log.warning("");
                log.warning("---------------------------------------");
                break;
            }
        }
    }
}