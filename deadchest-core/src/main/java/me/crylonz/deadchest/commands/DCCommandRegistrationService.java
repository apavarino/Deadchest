package me.crylonz.deadchest.commands;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.Permission;
import me.crylonz.deadchest.db.InMemoryChestStore;
import me.crylonz.deadchest.utils.ConfigKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static me.crylonz.deadchest.DeadChestLoader.*;
import static me.crylonz.deadchest.DeadChestManager.cleanAllDeadChests;
import static me.crylonz.deadchest.DeadChestManager.removeDeadChest;
import static me.crylonz.deadchest.db.IgnoreItemListRepository.loadIgnoreIntoInventory;

public class DCCommandRegistrationService extends DCCommandRegistration {

    public DCCommandRegistrationService(DeadChestLoader plugin) {
        super(plugin);
    }

    public void registerReload() {
        registerCommand("dc reload", Permission.ADMIN.label, () -> {
            loadIgnoreIntoInventory(ignoreList);
            DeadChestLoader.plugin.reloadConfig();
            plugin.registerConfig();
            local.reloadLanguage(config.getString(ConfigKey.LOCALIZATION_LANGUAGE));

            sender.sendMessage(local.prefixed("commands.reload.success"));
        });
    }

    public void registerRepairForce() {
        registerCommand("dc repair force", Permission.ADMIN.label, () -> repair(true));
    }

    public void registerRepair() {
        registerCommand("dc repair", Permission.ADMIN.label, () -> repair(false));
    }

    private void repair(Boolean forced) {
        if (player == null) {
            sender.sendMessage(local.prefixed("commands.error.player-only"));
            return;
        }

        getSchedulerAdapter().executeForEntity(player, () -> {
            Collection<Entity> entities = player.getWorld().getNearbyEntities(player.getLocation(), 100.0D, 25.0D, 100.0D);
            int holoRemoved = 0;

            for (Entity entity : entities) {
                if (entity.getType() != EntityType.ARMOR_STAND) {
                    continue;
                }

                ArmorStand armorStand = (ArmorStand) entity;
                if (armorStand.hasMetadata("deadchest") || forced) {
                    holoRemoved++;
                    entity.remove();
                }
            }

            player.sendMessage(local.prefixed("commands.operation.holograms-removed", holoRemoved));
        });
    }

    public void registerRemoveInfinite() {
        registerCommand("dc removeinfinite", Permission.ADMIN.label, () -> {
            int count = 0;
            final InMemoryChestStore inMemoryChestStore = DeadChestLoader.getChestDataCache();
            final Map<Location, ChestData> chestDataMap = inMemoryChestStore.getAllChestData();

            if (chestDataMap != null && !chestDataMap.isEmpty()) {
                for (final ChestData chestData : chestDataMap.values()) {
                    if (chestData.getChestLocation().getWorld() != null &&
                            (chestData.isInfinity() || config.getInt(ConfigKey.DEADCHEST_DURATION) == 0)) {
                        getSchedulerAdapter().executeAtLocation(chestData.getChestLocation(), () -> removeDeadChest(chestData));
                        count++;
                    }
                }
            }

            sender.sendMessage(local.prefixed("commands.operation.deadchests-removed", count));
        });
    }

    public void registerRemoveAll() {
        registerCommand("dc removeall", Permission.ADMIN.label, () -> {
            int count = cleanAllDeadChests();
            sender.sendMessage(local.prefixed("commands.operation.deadchests-removed", count));
        });
    }

    public void registerRemoveOwn() {
        registerCommand("dc remove", Permission.REMOVE_OWN.label, () -> {
            if (player != null) {
                removeAllDeadChestOfPlayer(player.getName());
            } else {
                sender.sendMessage(local.prefixed("commands.error.player-only"));
            }
        });
    }

    public void registerRemoveOther() {
        registerCommand("dc remove {0}", Permission.REMOVE_OTHER.label, () -> removeAllDeadChestOfPlayer(args[1]));
    }

    private void removeAllDeadChestOfPlayer(String playerName) {
        int count = 0;
        final InMemoryChestStore chestDataCache = getChestDataCache();
        final Map<Location, ChestData> chestDataList = chestDataCache.getAllChestData();

        if (chestDataList != null && !chestDataList.isEmpty()) {
            for (ChestData chestData : chestDataList.values()) {
                if (chestData != null && chestData.getPlayerName().equalsIgnoreCase(playerName)) {
                    getSchedulerAdapter().executeAtLocation(chestData.getChestLocation(), () -> removeDeadChest(chestData));
                    count++;
                }
            }
        }

        sender.sendMessage(local.prefixed("commands.operation.deadchests-removed-player", count, playerName));
    }

    public void registerListOwn() {
        registerCommand("dc list", null, () -> {
            if (player == null) {
                sender.sendMessage(local.prefixed("commands.error.player-only"));
                return;
            }

            if (player.hasPermission(Permission.LIST_OWN.label) || !config.getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_LIST_OWN)) {
                Date now = new Date();
                final Map<Location, ChestData> chestDataList = getChestDataCache().getAllChestData();
                if (!chestDataList.isEmpty()) {
                    sender.sendMessage(local.prefixed("commands.list.title.own"));
                    for (ChestData data : chestDataList.values()) {
                        if (data.getPlayerUUID().equals(player.getUniqueId())) {
                            displayChestData(now, data);
                        }
                    }
                } else {
                    player.sendMessage(local.prefixed("commands.list.none.player"));
                }
            }
        });
    }

    public void registerListOther() {
        registerCommand("dc list {0}", Permission.LIST_OTHER.label, () -> {
            Date now = new Date();
            final Map<Location, ChestData> chestDataList = getChestDataCache().getAllChestData();

            if (args[1].equalsIgnoreCase("all")) {
                if (!chestDataList.isEmpty()) {
                    sender.sendMessage(local.prefixed("commands.list.title.all"));
                    for (ChestData data : chestDataList.values()) {
                        displayChestData(now, data);
                    }
                } else {
                    sender.sendMessage(local.prefixed("commands.list.none.global"));
                }
                return;
            }

            if (!chestDataList.isEmpty()) {
                sender.sendMessage(local.prefixed("commands.list.title.player", args[1]));
                for (ChestData data : chestDataList.values()) {
                    if (data.getPlayerName().equalsIgnoreCase(args[1])) {
                        displayChestData(now, data);
                    }
                }
            } else {
                sender.sendMessage(local.prefixed("commands.list.none.global"));
            }
        });
    }

    private void displayChestData(Date now, ChestData chestData) {
        String worldName = chestData.getChestLocation().getWorld() != null ?
                chestData.getChestLocation().getWorld().getName() : local.get("common.unknown-world");

        if (chestData.isInfinity() || config.getInt(ConfigKey.DEADCHEST_DURATION) == 0) {
            sender.sendMessage(local.format(
                    "commands.list.entry.infinity",
                    worldName,
                    chestData.getChestLocation().getX(),
                    chestData.getChestLocation().getY(),
                    chestData.getChestLocation().getZ(),
                    local.get("chest.time-left")
            ));
            return;
        }

        long diff = now.getTime() - (chestData.getChestDate().getTime() + config.getInt(ConfigKey.DEADCHEST_DURATION) * 1000L);
        long diffSeconds = Math.abs(diff / 1000 % 60);
        long diffMinutes = Math.abs(diff / (60 * 1000) % 60);
        long diffHours = Math.abs(diff / (60 * 60 * 1000));

        sender.sendMessage(local.format(
                "commands.list.entry.timed",
                chestData.getChestLocation().getX(),
                chestData.getChestLocation().getY(),
                chestData.getChestLocation().getZ(),
                diffHours,
                diffMinutes,
                diffSeconds,
                local.get("chest.time-left")
        ));
    }

    public void registerGiveBack() {
        registerCommand("dc giveback {0}", Permission.GIVEBACK.label, () -> {
            Player targetPlayer = null;
            final InMemoryChestStore inMemoryChestStore = DeadChestLoader.getChestDataCache();
            final Map<Location, ChestData> chestDataList = inMemoryChestStore.getAllChestData();

            for (ChestData data : chestDataList.values()) {
                if (!data.getPlayerName().equalsIgnoreCase(args[1])) {
                    continue;
                }

                targetPlayer = Bukkit.getPlayer(data.getPlayerUUID());
                if (targetPlayer != null && player != null && player.isOnline()) {
                    final Player finalTargetPlayer = targetPlayer;
                    getSchedulerAdapter().executeForEntity(finalTargetPlayer, () -> {
                        for (ItemStack itemStack : data.getInventory()) {
                            if (itemStack != null) {
                                finalTargetPlayer.getWorld().dropItemNaturally(finalTargetPlayer.getLocation(), itemStack);
                            }
                        }
                    });
                    getSchedulerAdapter().executeAtLocation(data.getChestLocation(), () -> removeDeadChest(data));
                }
                break;
            }

            if (targetPlayer != null) {
                sender.sendMessage(local.prefixed("commands.giveback.success.sender"));
                targetPlayer.sendMessage(local.prefixed("commands.giveback.success.target"));
            } else {
                sender.sendMessage(local.prefixed("commands.giveback.target-not-found"));
            }
        });
    }

    public void registerIgnoreList() {
        registerCommand("dc ignore ", Permission.ADMIN.label, () ->
                getSchedulerAdapter().executeForEntity(player, () -> player.openInventory(ignoreList))
        );
    }
}
