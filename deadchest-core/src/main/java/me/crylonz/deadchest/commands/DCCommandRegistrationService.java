package me.crylonz.deadchest.commands;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChest;
import me.crylonz.deadchest.Permission;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.DeadChestManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static me.crylonz.deadchest.DeadChest.*;
import static me.crylonz.deadchest.DeadChestManager.cleanAllDeadChests;

public class DCCommandRegistrationService extends DCCommandRegistration {

    public DCCommandRegistrationService(DeadChest plugin) {
        super(plugin);
    }

    public void registerReload() {
        registerCommand("dc reload", Permission.ADMIN.label, () -> {
            fileManager.reloadChestDataConfig();
            fileManager.reloadLocalizationConfig();
            plugin.reloadConfig();
            plugin.registerConfig();

            @SuppressWarnings("unchecked")
            List<ChestData> tmp = (List<ChestData>) fileManager.getChestDataConfig().get("chestData");
            if (tmp != null) {
                chestData = tmp;
            }
            if (fileManager.getLocalizationConfig().getConfigurationSection("localisation") != null) {
                local.set(fileManager.getLocalizationConfig().getConfigurationSection("localisation").getValues(true));
            }
            sender.sendMessage(ChatColor.GREEN + local.get("loc_prefix") + " Plugin reload successfully");
        });
    }

    public void registerRepairForce() {
        registerCommand("dc repair force", Permission.ADMIN.label, () -> repair(true));
    }

    public void registerRepair() {
        registerCommand("dc repair", Permission.ADMIN.label, () -> repair(false));
    }

    private void repair(Boolean forced) {
        if (player != null) {
            Collection<Entity> entities = player.getWorld().getNearbyEntities(
                    player.getLocation(), 100.0D, 25.0D, 100.0D);
            int holoRemoved = 0;
            for (Entity entity : entities) {
                if (entity.getType() == EntityType.ARMOR_STAND) {
                    ArmorStand as = (ArmorStand) entity;
                    if (as.hasMetadata("deadchest") || forced) {
                        holoRemoved++;
                        entity.remove();
                    }
                }
            }
            player.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" + holoRemoved + "] hologram(s) removed");
        } else {
            sender.sendMessage(local.get("loc_prefix") + ChatColor.RED + "Command must be called by a player");
        }
    }

    public void registerRemoveInfinite() {
        registerCommand("dc removeinfinite", Permission.ADMIN.label, () -> {
            int initialSize = chestData.size();
            if (chestData != null) {
                chestData.removeIf(cd -> {
                    if (cd.getChestLocation().getWorld() != null && (cd.isInfinity() || config.getInt(ConfigKey.DEADCHEST_DURATION) == 0)) {
                        cd.getChestLocation().getBlock().setType(Material.AIR);
                        cd.removeArmorStand();
                        return true;
                    }
                    return false;
                });
                fileManager.saveModification();
            }
            int removedCount = initialSize - chestData.size();
            sender.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" +
                    ChatColor.GREEN + removedCount + ChatColor.GOLD + "] infinite deadchest(s) removed");
        });
    }

    public void registerRemoveAll() {
        registerCommand("dc removeall", Permission.ADMIN.label, () -> {
            int cpt = cleanAllDeadChests();
            sender.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" +
                    ChatColor.GREEN + cpt + ChatColor.GOLD + "] deadchest(s) removed");
        });
    }

    public void registerRemoveOwn() {
        registerCommand("dc remove", Permission.REMOVE_OWN.label, () -> {
            if (player != null) {
                removeAllDeadChestOfPlayer(player.getName());
            } else {
                sender.sendMessage(local.get("loc_prefix") + ChatColor.RED + "Command must be called by a player");
            }
        });
    }

    public void registerRemoveOther() {
        registerCommand("dc remove {0}", Permission.REMOVE_OTHER.label, () -> removeAllDeadChestOfPlayer(args[1]));
    }

    private void removeAllDeadChestOfPlayer(String playerName) {
        int initialSize = chestData.size();
        if (chestData != null) {
            chestData.removeIf(cd -> {
                if (cd.getPlayerName().equalsIgnoreCase(playerName) && cd.getChestLocation().getWorld() != null) {
                    cd.getChestLocation().getBlock().setType(Material.AIR);
                    cd.removeArmorStand();
                    return true;
                }
                return false;
            });
            fileManager.saveModification();
        }
        int removedCount = initialSize - chestData.size();
        sender.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" +
                ChatColor.GREEN + removedCount + ChatColor.GOLD + "] deadchest(s) removed of player " + playerName);
    }

    public void registerListOwn() {
        registerCommand("dc list", null, () -> {
            if (player != null) {
                if (player.hasPermission(Permission.LIST_OWN.label) || !config.getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_LIST_OWN)) {
                    List<ChestData> ownChests = new ArrayList<>();
                    for (ChestData data : chestData) {
                        if (data.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString())) {
                            ownChests.add(data);
                        }
                    }

                    if (!ownChests.isEmpty()) {
                        sender.sendMessage(local.get("loc_prefix") + local.get("loc_dclistown") + " :");
                        for (ChestData data : ownChests) {
                            displayChestData(data);
                        }
                    } else {
                        player.sendMessage(local.get("loc_prefix") + local.get("loc_nodc"));
                    }
                }
            } else {
                sender.sendMessage(local.get("loc_prefix") + ChatColor.RED + "Command must be called by a player");
            }
        });
    }

    public void registerListOther() {
        registerCommand("dc list {0}", Permission.LIST_OTHER.label, () -> {
            if (args[1].equalsIgnoreCase("all")) {
                if (!chestData.isEmpty()) {
                    sender.sendMessage(local.get("loc_prefix") + local.get("loc_dclistall") + ":");
                    for (ChestData data : chestData) {
                        displayChestData(data);
                    }
                } else {
                    sender.sendMessage(local.get("loc_prefix") + local.get("loc_nodcs"));
                }
            } else {
                List<ChestData> otherChests = new ArrayList<>();
                for (ChestData data : chestData) {
                    if (data.getPlayerName().equalsIgnoreCase(args[1])) {
                        otherChests.add(data);
                    }
                }

                if(!otherChests.isEmpty()) {
                    sender.sendMessage(local.get("loc_prefix") + ChatColor.GREEN + args[1] + " deadchests :");
                    for (ChestData data : otherChests) {
                        displayChestData(data);
                    }
                } else {
                    sender.sendMessage(local.get("loc_prefix") + local.get("loc_nodcs"));
                }
            }
        });
    }

    private void displayChestData(ChestData chestData) {
        String worldName = chestData.getWorldName() != null ? chestData.getWorldName() : "???";
        Location loc = chestData.getChestLocation();

        if (chestData.isInfinity() || config.getInt(ConfigKey.DEADCHEST_DURATION) == 0) {
            sender.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + worldName + " |"
                    + ChatColor.AQUA + " X: " + ChatColor.WHITE + loc.getBlockX()
                    + ChatColor.AQUA + " Y: " + ChatColor.WHITE + loc.getBlockY()
                    + ChatColor.AQUA + " Z: " + ChatColor.WHITE + loc.getBlockZ()
                    + " | "
                    + "∞ " + local.get("loc_endtimer"));
        } else {
            long remaining = chestData.getTimeRemaining();
            long diffHours = remaining / 3600;
            long diffMinutes = (remaining % 3600) / 60;
            long diffSeconds = remaining % 60;

            sender.sendMessage("-" + ChatColor.AQUA + " X: " + ChatColor.WHITE + loc.getBlockX()
                    + ChatColor.AQUA + " Y: " + ChatColor.WHITE + loc.getBlockY()
                    + ChatColor.AQUA + " Z: " + ChatColor.WHITE + loc.getBlockZ()
                    + " | " + diffHours + "h " + diffMinutes + "m " + diffSeconds + "s " + local.get("loc_endtimer"));
        }
    }

    public void registerGiveBack() {
        registerCommand("dc giveback {0}", Permission.GIVEBACK.label, () -> {
            Player targetPlayer = Bukkit.getPlayer(args[1]);

            if (targetPlayer == null || !targetPlayer.isOnline()) {
                sender.sendMessage(local.get("loc_prefix") + "§cThat player is not online.");
                return;
            }

            ChestData chestToGive = null;
            Iterator<ChestData> it = chestData.iterator();
            while (it.hasNext()) {
                ChestData data = it.next();
                if (data.getPlayerUUID().equals(targetPlayer.getUniqueId().toString())) {
                    chestToGive = data;
                    it.remove(); // Remove the chest safely while iterating
                    break;
                }
            }

            if (chestToGive != null) {
                boolean useSmartEquip = config.getBoolean(ConfigKey.ATTEMPT_RE_EQUIP);
                DeadChestManager.giveItemsToPlayer(targetPlayer, chestToGive, targetPlayer.getLocation(), useSmartEquip);

                targetPlayer.getWorld().getBlockAt(chestToGive.getChestLocation()).setType(Material.AIR);
                chestToGive.removeArmorStand();

                sender.sendMessage(local.get("loc_prefix") + local.get("loc_dcgbsuccess"));
                targetPlayer.sendMessage(local.get("loc_prefix") + local.get("loc_gbplayer"));

                fileManager.saveModification();
            } else {
                sender.sendMessage(local.get("loc_prefix") + "§cThat player has no active deadchests.");
            }
        });
    }
}