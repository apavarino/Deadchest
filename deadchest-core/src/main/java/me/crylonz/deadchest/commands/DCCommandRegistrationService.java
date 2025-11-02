package me.crylonz.deadchest.commands;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.Permission;
import me.crylonz.deadchest.db.ChestDataRepository;
import me.crylonz.deadchest.utils.ConfigKey;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static me.crylonz.deadchest.DeadChestLoader.*;
import static me.crylonz.deadchest.DeadChestManager.cleanAllDeadChests;
import static me.crylonz.deadchest.db.IgnoreItemListRepository.loadIgnoreIntoInventory;

public class DCCommandRegistrationService extends DCCommandRegistration {

    public DCCommandRegistrationService(DeadChestLoader plugin) {
        super(plugin);
    }

    public void registerReload() {
        registerCommand("dc reload", Permission.ADMIN.label, () -> {
            fileManager.reloadLocalizationConfig();
            chestDataList = ChestDataRepository.findAll();
            loadIgnoreIntoInventory(ignoreList);
            DeadChestLoader.plugin.reloadConfig();
            plugin.registerConfig();
            local.set(fileManager.getLocalizationConfig().getConfigurationSection("localisation").getValues(true));

            sender.sendMessage(ChatColor.GREEN + local.get("loc_prefix") + " Plugin reload successfully");
        });
    }

    public void registerRepairForce() {
        registerCommand("dc repair force", Permission.ADMIN.label, () -> {
            repair(true);
        });
    }

    public void registerRepair() {
        registerCommand("dc repair", Permission.ADMIN.label, () -> {
            repair(false);
        });
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

                    // Deprecated (support for deadchest 3.X and lower)
                    else if (as.getCustomName() != null && as.getCustomName().contains("×")) {
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

            int cpt = 0;
            if (chestDataList != null && !chestDataList.isEmpty()) {
                Iterator<ChestData> chestDataIt = chestDataList.iterator();
                while (chestDataIt.hasNext()) {
                    ChestData chestData = chestDataIt.next();

                    if (chestData.getChestLocation().getWorld() != null) {
                        if (chestData.isInfinity() || config.getInt(ConfigKey.DEADCHEST_DURATION) == 0) {

                            // remove chest
                            Location loc = chestData.getChestLocation();
                            loc.getWorld().getBlockAt(loc).setType(Material.AIR);

                            // remove holographic
                            chestData.removeArmorStand();

                            // remove in memory
                            chestDataIt.remove();

                            cpt++;
                        }
                    }
                }
                ChestDataRepository.saveAllAsync(chestDataList);
            }
            sender.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" +
                    ChatColor.GREEN + cpt + ChatColor.GOLD + "] deadchest(s) removed");
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
        registerCommand("dc remove {0}", Permission.REMOVE_OTHER.label, () -> {
            removeAllDeadChestOfPlayer(args[1]);
        });
    }

    private void removeAllDeadChestOfPlayer(String playerName) {
        int cpt = 0;
        if (chestDataList != null && !chestDataList.isEmpty()) {

            Iterator<ChestData> chestDataIt = chestDataList.iterator();
            while (chestDataIt.hasNext()) {

                ChestData cd = chestDataIt.next();

                if (cd.getChestLocation().getWorld() != null) {

                    if (cd.getPlayerName().equalsIgnoreCase(playerName)) {
                        // remove chest
                        Location loc = cd.getChestLocation();
                        loc.getWorld().getBlockAt(loc).setType(Material.AIR);

                        // remove holographics
                        cd.removeArmorStand();

                        // remove in memory
                        chestDataIt.remove();

                        cpt++;
                    }
                }
            }
            ChestDataRepository.saveAllAsync(chestDataList);
        }
        sender.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" +
                ChatColor.GREEN + cpt + ChatColor.GOLD + "] deadchest(s) removed of player " + playerName);
    }

    public void registerListOwn() {
        registerCommand("dc list", null, () -> {
            if (player != null) {
                if (player.hasPermission(Permission.LIST_OWN.label) || !config.getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_LIST_OWN)) {
                    Date now = new Date();
                    if (!chestDataList.isEmpty()) {
                        sender.sendMessage(local.get("loc_prefix") + local.get("loc_dclistown") + " :");
                        for (ChestData data : chestDataList) {
                            if (data.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString())) {
                                displayChestData(now, data);
                            }
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

            Date now = new Date();
            if (args[1].equalsIgnoreCase("all")) {
                if (!chestDataList.isEmpty()) {
                    sender.sendMessage(local.get("loc_prefix") + local.get("loc_dclistall") + ":");
                    for (ChestData data : chestDataList) {
                        displayChestData(now, data);
                    }
                } else {
                    sender.sendMessage(local.get("loc_prefix") + local.get("loc_nodcs"));
                }

            } else {
                if (!chestDataList.isEmpty()) {
                    sender.sendMessage(local.get("loc_prefix") + ChatColor.GREEN + args[1] + " deadchests :");
                    for (ChestData data : chestDataList) {
                        if (data.getPlayerName().equalsIgnoreCase(args[1])) {
                            displayChestData(now, data);
                        }
                    }
                } else {
                    sender.sendMessage(local.get("loc_prefix") + local.get("loc_nodcs"));
                }
            }
        });
    }

    private void displayChestData(Date now, ChestData chestData) {
        String worldName = chestData.getChestLocation().getWorld() != null ?
                chestData.getChestLocation().getWorld().getName() : "???";

        if (chestData.isInfinity() || config.getInt(ConfigKey.DEADCHEST_DURATION) == 0) {
            sender.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + worldName + " |"
                    + ChatColor.AQUA + " X: " + ChatColor.WHITE + chestData.getChestLocation().getX()
                    + ChatColor.AQUA + " Y: " + ChatColor.WHITE + chestData.getChestLocation().getY()
                    + ChatColor.AQUA + " Z: " + ChatColor.WHITE + chestData.getChestLocation().getZ()
                    + " | "
                    + "∞ " + local.get("loc_endtimer"));
        } else {
            long diff = now.getTime() - (chestData.getChestDate().getTime() + config.getInt(ConfigKey.DEADCHEST_DURATION) * 1000L);
            long diffSeconds = Math.abs(diff / 1000 % 60);
            long diffMinutes = Math.abs(diff / (60 * 1000) % 60);
            long diffHours = Math.abs(diff / (60 * 60 * 1000));
            player.sendMessage("-" + ChatColor.AQUA + " X: " + ChatColor.WHITE + chestData.getChestLocation().getX()
                    + ChatColor.AQUA + " Y: " + ChatColor.WHITE + chestData.getChestLocation().getY()
                    + ChatColor.AQUA + " Z: " + ChatColor.WHITE + chestData.getChestLocation().getZ()
                    + " | " +
                    +diffHours + "h "
                    + diffMinutes + "m "
                    + diffSeconds + "s " + local.get("loc_endtimer"));
        }
    }

    public void registerGiveBack() {
        registerCommand("dc giveback {0}", Permission.GIVEBACK.label, () -> {
            Player targetPlayer = null;
            boolean hadOverflow = false;

            for (ChestData data : chestDataList) {
                if (data.getPlayerName().equalsIgnoreCase(args[1])) {

                    targetPlayer = Bukkit.getPlayer(UUID.fromString(data.getPlayerUUID()));

                    if (targetPlayer != null && player.isOnline()) {
                        // Restore items to their original slots
                        List<ItemStack> inventory = data.getInventory();
                        org.bukkit.inventory.PlayerInventory playerInv = targetPlayer.getInventory();

                        for (int i = 0; i < inventory.size(); i++) {
                            ItemStack itemStack = inventory.get(i);
                            if (itemStack != null) {
                                // Check if the slot is empty and restore to original position
                                ItemStack currentItem = playerInv.getItem(i);

                                if (currentItem == null || currentItem.getType() == Material.AIR) {
                                    // Slot is empty, restore item to original position
                                    playerInv.setItem(i, itemStack);
                                } else {
                                    // Slot is occupied, try to add to any available slot
                                    java.util.HashMap<Integer, ItemStack> overflow = playerInv.addItem(itemStack);

                                    // If the inventory is full It will drop the remaining items around the player
                                    if (!overflow.isEmpty()) {
                                        hadOverflow = true;
                                        for (ItemStack overflowItem : overflow.values()) {
                                            targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), overflowItem);
                                        }
                                    }
                                }
                            }
                        }

                        // Remove chest and hologram
                        targetPlayer.getWorld().getBlockAt(data.getChestLocation()).setType(Material.AIR);
                        data.removeArmorStand();
                        chestDataList.remove(data);
                    }
                    break;
                }
            }

            if (targetPlayer != null) {
                if (hadOverflow) {
                    sender.sendMessage(local.get("loc_prefix") + local.get("loc_dcgbsuccess_overflow"));
                    targetPlayer.sendMessage(local.get("loc_prefix") + local.get("loc_gbplayer_overflow"));
                } else {
                    sender.sendMessage(local.get("loc_prefix") + local.get("loc_dcgbsuccess"));
                    targetPlayer.sendMessage(local.get("loc_prefix") + local.get("loc_gbplayer"));
                }
            } else {
                sender.sendMessage(local.get("loc_prefix") + local.get("loc_givebackInfo"));
            }
        });
    }

    public void registerIgnoreList() {
        registerCommand("dc ignore ", Permission.ADMIN.label, () -> {
            player.openInventory(ignoreList);
        });
    }
}
