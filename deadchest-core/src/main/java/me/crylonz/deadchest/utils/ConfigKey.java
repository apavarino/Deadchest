package me.crylonz.deadchest.utils;

public enum ConfigKey {
    AUTO_UPDATE("auto-update"),
    INDESTRUCTIBLE_CHEST("IndestructibleChest"),
    ONLY_OWNER_CAN_OPEN_CHEST("OnlyOwnerCanOpenDeadChest"),
    DEADCHEST_DURATION("DeadChestDuration"),
    MAX_DEAD_CHEST_PER_PLAYER("maxDeadChestPerPlayer"),
    LOG_DEADCHEST_ON_CONSOLE("logDeadChestOnConsole"),
    REQUIRE_PERMISSION_TO_GENERATE("RequirePermissionToGenerate"),
    REQUIRE_PERMISSION_TO_GET_CHEST("RequirePermissionToGetChest"),
    REQUIRE_PERMISSION_TO_LIST_OWN("RequirePermissionToListOwn"),
    AUTO_CLEANUP_ON_START("AutoCleanupOnStart"),
    GENERATE_DEADCHEST_IN_CREATIVE("GenerateDeadChestInCreative"),
    DISPLAY_POSITION_ON_DEATH("DisplayDeadChestPositionOnDeath"),
    ITEMS_DROPPED_AFTER_TIMEOUT("ItemsDroppedAfterTimeOut"),
    WORLD_GUARD_DETECTION("EnableWorldGuardDetection"),
    DROP_MODE("DropMode"),
    DROP_BLOCK("DropBlock"),
    GENERATE_ON_LAVA("GenerateOnLava"),
    GENERATE_ON_WATER("GenerateOnWater"),
    GENERATE_ON_RAILS("GenerateOnRails"),
    GENERATE_IN_MINECART("GenerateInMinecart"),
    GENERATE_IN_THE_END("GenerateInTheEnd"),
    EXCLUDED_WORLDS("ExcludedWorld"),
    EXCLUDED_ITEMS("ExcludedItems"),
    IGNORED_ITEMS("IgnoredItems"),
    STORE_XP("StoreXP"),
    STORE_XP_PERCENTAGE("StoreXPPercentage"),
    KEEP_INVENTORY_ON_PVP_DEATH("KeepInventoryOnPvpDeath"),
    ITEM_DURABILITY_LOSS_ON_DEATH("item-durability-loss-on-death"),
    SUSPEND_COUNTDOWNS_WHEN_PLAYER_IS_OFFLINE("SuspendCountdownsWhenPlayerIsOffline"),
    EXPIRE_ACTION("ExpireAction");
    private final String text;

    ConfigKey(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
