package me.crylonz.deadchest;

import me.crylonz.deadchest.commands.DCCommandExecutor;
import me.crylonz.deadchest.commands.DCTabCompletion;
import me.crylonz.deadchest.db.*;
import me.crylonz.deadchest.deps.worldguard.WorldGuardSoftDependenciesChecker;
import me.crylonz.deadchest.legacy.OldChestData;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.DeadChestConfig;
import me.crylonz.deadchest.utils.EffectAnimationStyle;
import me.crylonz.deadchest.utils.ExpiredActionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Logger;

import static me.crylonz.deadchest.DeadChestManager.*;
import static me.crylonz.deadchest.db.IgnoreItemListRepository.loadIgnoreIntoInventory;
import static me.crylonz.deadchest.utils.Utils.generateLog;

public class DeadChestLoader {

    public static Logger log = Logger.getLogger("Minecraft");
    public static FileManager fileManager;
    private static final InMemoryChestStore chestData = new InMemoryChestStore();

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

        local = new Localization(plugin);
        ignoreList = Bukkit.createInventory(new IgnoreInventoryHolder(), 36, local.get("gui.ignore-list.title"));
        config = new DeadChestConfig(plugin);
        fileManager = new FileManager(plugin);

        ChestDataRepository.initTable(/* migrate old chestData.yml config */ OldChestData::migrateOldChestData);

        ChestDataRepository.findAllAsync(chestData::setChestData, plugin);

        registerConfig();
        initializeConfig();

        if (config.getBoolean(ConfigKey.AUTO_CLEANUP_ON_START)) {
            cleanAllDeadChests();
        }


        // Which block can be used as grave ?
        graveBlocks.add(Material.CHEST);
        graveBlocks.add(Material.ENDER_CHEST);

        Material head = Material.getMaterial("PLAYER_HEAD");
        if (head == null)
            head = Material.getMaterial("SKULL");
        if (head != null)
            graveBlocks.add(head);

        final Material barrel = Material.getMaterial("BARREL");
        if (barrel != null)
            graveBlocks.add(barrel);
        final Material shulkerBox = Material.getMaterial("SHULKER_BOX");
        if (shulkerBox != null)
            graveBlocks.add(shulkerBox);

        Objects.requireNonNull(javaPlugin.getCommand("dc"), "Command dc not found")
                .setExecutor(new DCCommandExecutor(this));

        Objects.requireNonNull(javaPlugin.getCommand("dc")).setTabCompleter(new DCTabCompletion());

        launchRepeatingTask();
    }


    public void load() {
        boolean worldGuardEnabled = javaPlugin.getConfig().getBoolean(ConfigKey.WORLD_GUARD_DETECTION.toString());
        if (!worldGuardEnabled) {
            for (String legacyKey : ConfigKey.WORLD_GUARD_DETECTION.aliases()) {
                if (javaPlugin.getConfig().contains(legacyKey)) {
                    worldGuardEnabled = javaPlugin.getConfig().getBoolean(legacyKey);
                    break;
                }
            }
        }

        if (worldGuardEnabled) {
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

        ChestDataRepository.saveAllAsync(getChestDataCache().getAllChestData().values());
        sqlExecutor.shutdown();
        db.close();
    }

    public static InMemoryChestStore getChestDataCache() {
        return chestData;
    }

    @Nullable
    public static ChestData getChestData(@Nonnull Location location) {
        return chestData.getChestData(location);
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
        config.register(ConfigKey.WORLD_GUARD_FLAG_DEFAULT.toString(), false);
        config.register(ConfigKey.DROP_MODE.toString(), "inventory-then-ground");
        config.register(ConfigKey.DROP_BLOCK.toString(), "chest");
        config.register(ConfigKey.ITEM_DURABILITY_LOSS_ON_DEATH.toString(), 0);
        config.register(ConfigKey.EFFECT_ANIMATION_ENABLED.toString(), true);
        config.register(ConfigKey.EFFECT_ANIMATION_STYLE.toString(), EffectAnimationStyle.SOUL.id());
        config.register(ConfigKey.EFFECT_ANIMATION_RADIUS.toString(), 0.8D);
        config.register(ConfigKey.EFFECT_ANIMATION_SPEED.toString(), 1.1D);
        config.register(ConfigKey.PICKUP_ANIMATION_ENABLED.toString(), true);
        config.register(ConfigKey.PICKUP_ANIMATION_PARTICLE.toString(), "TOTEM");
        config.register(ConfigKey.PICKUP_ANIMATION_COUNT.toString(), 22);
        config.register(ConfigKey.PICKUP_ANIMATION_OFFSET_X.toString(), 0.45D);
        config.register(ConfigKey.PICKUP_ANIMATION_OFFSET_Y.toString(), 0.5D);
        config.register(ConfigKey.PICKUP_ANIMATION_OFFSET_Z.toString(), 0.45D);
        config.register(ConfigKey.PICKUP_ANIMATION_SPEED.toString(), 0.08D);
        config.register(ConfigKey.PICKUP_ANIMATION_Y_SHIFT.toString(), 0.55D);
        config.register(ConfigKey.PICKUP_SOUND_ENABLED.toString(), true);
        config.register(ConfigKey.PICKUP_SOUND_NAME.toString(), "ENTITY_EXPERIENCE_ORB_PICKUP");
        config.register(ConfigKey.PICKUP_SOUND_VOLUME.toString(), 1.2D);
        config.register(ConfigKey.PICKUP_SOUND_PITCH.toString(), 1.0D);
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
        config.register(ConfigKey.LOCALIZATION_LANGUAGE.toString(), "en");
    }

    private void initializeConfig() {

        // plugin config file
        if (!fileManager.getConfigFile().exists()) {
            plugin.saveDefaultConfig();
        } else {
            config.updateConfig();
        }
        plugin.reloadConfig();

        // localization jsons
        local.reloadLanguage(config.getString(ConfigKey.LOCALIZATION_LANGUAGE));

        // ignore list inventory title now follows selected language
        ignoreList = Bukkit.createInventory(new IgnoreInventoryHolder(), 36, local.get("gui.ignore-list.title"));
        loadIgnoreIntoInventory(ignoreList);
    }

    public static void handleEvent() {
        final InMemoryChestStore inMemoryChestStore = getChestDataCache();
        final Map<Location, ChestData> allChestData = inMemoryChestStore.getAllChestData();
        if (!allChestData.isEmpty()) {
            final Date now = new Date();
            final Set<ChestData> needUpdate = new HashSet<>();
            final Set<ChestData> remove = new HashSet<>();
            final Set<ChestData> update = new HashSet<>();

            for (Map.Entry<Location, ChestData> chestEntry : allChestData.entrySet()) {
                final ChestData chestData = chestEntry.getValue();

                if (chestData == null) {
                    generateLog("Deadchest of [null] has invalid data set.");
                    continue;
                }
                World world = chestData.getChestLocation().getWorld();

                if (world != null) {
                    updateTimer(chestData, now);

                    final ExpiredActionType expiredActionType = handleExpirateDeadChest(chestData, now);
                    if (expiredActionType != ExpiredActionType.NOT_EXPIRED) {
                        if (expiredActionType == ExpiredActionType.FAIL_REMOVE_ARMORSTAND) {
                            needUpdate.add(chestData);
                        } else {
                            remove.add(chestData);
                        }
                        generateLog("Deadchest of [" + chestData.getPlayerName() + "] has expired in " + Objects.requireNonNull(chestData.getChestLocation().getWorld()).getName());
                    } else {
                        if (chestData.isChunkLoaded()) {
                            isChangesNeedToBeSave = replaceDeadChestIfItDisappears(chestData);
                            update.add(chestData);
                        }
                    }
                }
            }
            if (!needUpdate.isEmpty()) {
                inMemoryChestStore.addListOfChestData(needUpdate);
            }
            if (!remove.isEmpty()) {
                inMemoryChestStore.removeChestDataList(remove);
            }
            if (isChangesNeedToBeSave && !update.isEmpty()) {
                inMemoryChestStore.addListOfChestData(update);
                isChangesNeedToBeSave = false;
            }
        }
    }

    private void launchRepeatingTask() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, DeadChestLoader::handleEvent, 20, 20);
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, DeadChestLoader::handleAnimationEvent, 20, 4);
    }

    public static void handleAnimationEvent() {
        if (!config.getBoolean(ConfigKey.EFFECT_ANIMATION_ENABLED)) {
            return;
        }

        final Map<Location, ChestData> allChestData = getChestDataCache().getAllChestData();
        if (allChestData.isEmpty()) {
            return;
        }

        final long nowMs = System.currentTimeMillis();
        for (ChestData chestData : allChestData.values()) {
            if (chestData != null && chestData.isChunkLoaded()) {
                animateSoulOrbit(chestData, nowMs);
            }
        }
    }

    public static EffectAnimationStyle getConfiguredAnimationStyle() {
        EffectAnimationStyle style = EffectAnimationStyle.fromInput(config.getString(ConfigKey.EFFECT_ANIMATION_STYLE));
        return style == null ? EffectAnimationStyle.SOUL : style;
    }

    public DeadChestConfig getDataConfig() {
        return config;
    }

}
