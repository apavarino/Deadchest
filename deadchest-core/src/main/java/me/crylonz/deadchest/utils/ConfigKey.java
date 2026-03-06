package me.crylonz.deadchest.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum ConfigKey {
    AUTO_UPDATE("updates.auto-check", "auto-update"),
    INDESTRUCTIBLE_CHEST("chest.indestructible", "IndestructibleChest", "IndestuctibleChest"),
    ONLY_OWNER_CAN_OPEN_CHEST("chest.owner-only-open", "OnlyOwnerCanOpenDeadChest"),
    DEADCHEST_DURATION("chest.duration-seconds", "DeadChestDuration"),
    MAX_DEAD_CHEST_PER_PLAYER("chest.max-per-player", "maxDeadChestPerPlayer"),
    LOG_DEADCHEST_ON_CONSOLE("logging.deadchest-create-to-console", "logDeadChestOnConsole"),
    REQUIRE_PERMISSION_TO_GENERATE("permissions.require-generate", "RequirePermissionToGenerate"),
    REQUIRE_PERMISSION_TO_GET_CHEST("permissions.require-claim", "RequirePermissionToGetChest"),
    REQUIRE_PERMISSION_TO_LIST_OWN("permissions.require-list-own", "RequirePermissionToListOwn"),
    AUTO_CLEANUP_ON_START("maintenance.cleanup-on-startup", "AutoCleanupOnStart"),
    GENERATE_DEADCHEST_IN_CREATIVE("generation.allow-in-creative", "GenerateDeadChestInCreative"),
    DISPLAY_POSITION_ON_DEATH("messages.display-position-on-death", "DisplayDeadChestPositionOnDeath"),
    ITEMS_DROPPED_AFTER_TIMEOUT("chest.drop-items-on-timeout", "ItemsDroppedAfterTimeOut"),
    WORLD_GUARD_DETECTION("integrations.worldguard.enabled", "EnableWorldGuardDetection"),
    WORLD_GUARD_FLAG_DEFAULT("integrations.worldguard.default-allow", "EnableWorldGuardFlagDefault"),
    DROP_MODE("chest.recovery-mode", "DropMode"),
    DROP_BLOCK("chest.block-type", "DropBlock"),
    GENERATE_ON_LAVA("generation.allow-on-lava", "GenerateOnLava"),
    GENERATE_ON_WATER("generation.allow-on-water", "GenerateOnWater"),
    GENERATE_ON_RAILS("generation.allow-on-rails", "GenerateOnRails"),
    GENERATE_IN_MINECART("generation.allow-in-minecart", "GenerateInMinecart"),
    GENERATE_IN_THE_END("generation.allow-in-end-worlds", "GenerateInTheEnd"),
    EXCLUDED_WORLDS("filters.excluded-worlds", "ExcludedWorld"),
    EXCLUDED_ITEMS("filters.excluded-items", "ExcludedItems"),
    IGNORED_ITEMS("filters.ignored-items", "IgnoredItems"),
    STORE_XP("xp.store-on-death", "StoreXP"),
    STORE_XP_PERCENTAGE("xp.store-percentage", "StoreXPPercentage"),
    KEEP_INVENTORY_ON_PVP_DEATH("pvp.keep-inventory-on-player-kill", "KeepInventoryOnPvpDeath"),
    ITEM_DURABILITY_LOSS_ON_DEATH("durability.loss-on-death-percent", "item-durability-loss-on-death"),
    EFFECT_ANIMATION_ENABLED("visuals.effect-animation.enabled"),
    EFFECT_ANIMATION_STYLE("visuals.effect-animation.style"),
    EFFECT_ANIMATION_RADIUS("visuals.effect-animation.radius"),
    EFFECT_ANIMATION_SPEED("visuals.effect-animation.speed"),
    PICKUP_ANIMATION_ENABLED("visuals.pickup-animation.enabled"),
    PICKUP_ANIMATION_PARTICLE("visuals.pickup-animation.particle"),
    PICKUP_ANIMATION_COUNT("visuals.pickup-animation.count"),
    PICKUP_ANIMATION_OFFSET_X("visuals.pickup-animation.offset-x"),
    PICKUP_ANIMATION_OFFSET_Y("visuals.pickup-animation.offset-y"),
    PICKUP_ANIMATION_OFFSET_Z("visuals.pickup-animation.offset-z"),
    PICKUP_ANIMATION_SPEED("visuals.pickup-animation.speed"),
    PICKUP_ANIMATION_Y_SHIFT("visuals.pickup-animation.y-shift"),
    LOCALIZATION_LANGUAGE("localization.language", "language");

    private static final Map<String, ConfigKey> BY_CANONICAL = new HashMap<>();

    static {
        for (ConfigKey key : values()) {
            BY_CANONICAL.put(key.canonicalPath, key);
        }
    }

    private final String canonicalPath;
    private final String[] aliases;

    ConfigKey(final String canonicalPath, final String... aliases) {
        this.canonicalPath = canonicalPath;
        this.aliases = aliases;
    }

    public String canonicalPath() {
        return canonicalPath;
    }

    public String[] aliases() {
        return Arrays.copyOf(aliases, aliases.length);
    }

    public static ConfigKey fromCanonicalPath(String path) {
        return BY_CANONICAL.get(path);
    }

    @Override
    public String toString() {
        return canonicalPath;
    }
}
