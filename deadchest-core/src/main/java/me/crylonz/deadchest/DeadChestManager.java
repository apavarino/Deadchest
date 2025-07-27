package me.crylonz.deadchest;

import me.crylonz.deadchest.utils.ConfigKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.inventory.PlayerInventory;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import static me.crylonz.deadchest.DeadChest.*;
import static me.crylonz.deadchest.Utils.computeChestType;
import static me.crylonz.deadchest.Utils.isGraveBlock;

public class DeadChestManager {

    public static int cleanAllDeadChests() {

        int chestDataRemoved = 0;
        if (chestData != null && !chestData.isEmpty()) {
            Iterator<ChestData> chestDataIt = chestData.iterator();
            while (chestDataIt.hasNext()) {

                ChestData chestData = chestDataIt.next();
                if (chestData.getChestLocation().getWorld() != null) {

                    Location loc = chestData.getChestLocation();
                    loc.getWorld().getBlockAt(loc).setType(Material.AIR);
                    chestData.removeArmorStand();
                    chestDataIt.remove();
                    chestDataRemoved++;
                }
            }
            fileManager.saveModification();
        }
        return chestDataRemoved;
    }

    public static ArmorStand generateHologram(Location location, String text, float shiftX, float shiftY, float shiftZ, boolean isTimer) {
        if (location != null && location.getWorld() != null) {
            Location holoLoc = new Location(location.getWorld(),
                    location.getX() + shiftX,
                    location.getY() + shiftY + 2,
                    location.getZ() + shiftZ);

            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
            armorStand.setInvulnerable(true);
            armorStand.setSmall(true);
            armorStand.setGravity(false);
            armorStand.setCanPickupItems(false);
            armorStand.setVisible(false);
            armorStand.setCollidable(false);
            armorStand.setMetadata("deadchest", new FixedMetadataValue(plugin, isTimer));
            armorStand.setCustomName(text);
            armorStand.setSilent(true);
            armorStand.setMarker(true);
            armorStand.setCustomNameVisible(true);

            return armorStand;
        }
        return null;
    }

    static int playerDeadChestAmount(Player p) {
        int count = 0;
        if (p != null) {
            for (ChestData chestData : chestData) {
                if (p.getUniqueId().toString().equals(chestData.getPlayerUUID()))
                    count++;
            }
        }
        return count;
    }

    static void reloadMetaData() {

        for (ChestData cdata : chestData) {
            World world = cdata.getChestLocation().getWorld();

            if (world != null) {
                Collection<Entity> nearbyEntities =
                        world.getNearbyEntities(cdata.getHolographicTimer(), 1, 1, 1);

                for (Entity ne : nearbyEntities) {
                    if (ne.getUniqueId().equals(cdata.getHolographicOwnerId())) {
                        ne.setMetadata("deadchest", new FixedMetadataValue(DeadChest.plugin, false));
                    } else if (ne.getUniqueId().equals(cdata.getHolographicTimerId())) {
                        ne.setMetadata("deadchest", new FixedMetadataValue(DeadChest.plugin, true));
                    }
                }
            }
        }
    }

    public static boolean replaceDeadChestIfItDeseapears(ChestData chestData) {
        World world = chestData.getChestLocation().getWorld();
        if (world != null && !isGraveBlock(world.getBlockAt(chestData.getChestLocation()).getType())) {
            Block b = world.getBlockAt(chestData.getChestLocation());
            computeChestType(b, Bukkit.getPlayer(chestData.getPlayerUUID()));
            return true;
        }
        return false;
    }

    public static void giveItemsToPlayer(Player player, ChestData chestData, Location dropLocation, boolean useSmartEquip) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] itemsToGive = chestData.getInventory();
        ArrayList<ItemStack> overflow = new ArrayList<>();

        if (chestData.getXpStored() > 0) {
            player.giveExp(chestData.getXpStored());
        }

        boolean[] itemHandled = new boolean[itemsToGive.length];

        if (useSmartEquip) {
            for (int i = 0; i < itemsToGive.length; i++) {
                ItemStack item = itemsToGive[i];
                if (item == null || item.getType() == Material.AIR) {
                    itemHandled[i] = true;
                    continue;
                }
                if (i == 40 && (playerInventory.getItemInOffHand() == null || playerInventory.getItemInOffHand().getType() == Material.AIR)) {
                    playerInventory.setItemInOffHand(item);
                    itemHandled[i] = true;
                } else if (i == 39 && (playerInventory.getHelmet() == null || playerInventory.getHelmet().getType() == Material.AIR)) {
                    playerInventory.setHelmet(item);
                    itemHandled[i] = true;
                } else if (i == 38 && (playerInventory.getChestplate() == null || playerInventory.getChestplate().getType() == Material.AIR)) {
                    playerInventory.setChestplate(item);
                    itemHandled[i] = true;
                } else if (i == 37 && (playerInventory.getLeggings() == null || playerInventory.getLeggings().getType() == Material.AIR)) {
                    playerInventory.setLeggings(item);
                    itemHandled[i] = true;
                } else if (i == 36 && (playerInventory.getBoots() == null || playerInventory.getBoots().getType() == Material.AIR)) {
                    playerInventory.setBoots(item);
                    itemHandled[i] = true;
                }
            }
        }

        for (int i = 0; i < itemsToGive.length; i++) {
            if (!itemHandled[i]) {
                ItemStack item = itemsToGive[i];
                if (item == null) continue;
                HashMap<Integer, ItemStack> didNotFit = playerInventory.addItem(item);
                if (!didNotFit.isEmpty()) {
                    overflow.addAll(didNotFit.values());
                }
            }
        }

        if (!overflow.isEmpty()) {
            for (ItemStack item : overflow) {
                player.getWorld().dropItemNaturally(dropLocation, item);
            }
        }
    }

    public static boolean handleExpirateDeadChest(ChestData chestData, Iterator<ChestData> chestDataIt) {
        if (chestData.isInfinity() || config.getInt(ConfigKey.DEADCHEST_DURATION) == 0) {
            return false; // Never expires
        }

        if (chestData.getTimeRemaining() <= 0) {
            Player player = Bukkit.getPlayer(UUID.fromString(chestData.getPlayerUUID()));

            if (player != null && player.isOnline()) {
                Location loc = chestData.getChestLocation();
                World world = loc.getWorld();
                if (world == null) return false;

                int expireAction = config.getInt(ConfigKey.EXPIRE_ACTION);
                world.getBlockAt(loc).setType(Material.AIR);
                boolean useSmartEquip = config.getBoolean(ConfigKey.ATTEMPT_RE_EQUIP);

                switch (expireAction) {
                    case 1:
                        for (ItemStack item : chestData.getInventory()) {
                            if (item != null && item.getType() != Material.AIR) {
                                world.dropItemNaturally(loc, item);
                            }
                        }
                        break;
                    case 2:
                        giveItemsToPlayer(player, chestData, loc, useSmartEquip);
                        break;
                    case 3:
                        giveItemsToPlayer(player, chestData, player.getLocation(), useSmartEquip);
                        break;
                }

                if (expireAction > 0) {
                    try {
                        String soundName = plugin.getConfig().getString("warnings.retrieval.sound", "ENTITY_PLAYER_LEVELUP");
                        player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0f, 1.0f);
                        String message = plugin.getConfig().getString("warnings.retrieval.message");
                        if (message != null && !message.isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }
                    } catch (IllegalArgumentException ex) {
                        plugin.getLogger().warning("Invalid sound name in config.yml for retrieval.");
                    }
                }

                chestData.removeArmorStand();
                chestDataIt.remove();
                return true;
            }
        }
        return false;
    }

    public static void updateTimer(ChestData chestData, Date date) {
        Location chestTimer = chestData.getHolographicTimer();

        if (chestTimer.getWorld() != null && chestData.isChunkLoaded()) {

            ArrayList<Entity> entityList = (ArrayList<Entity>) chestTimer.getWorld().getNearbyEntities(chestTimer, 1.0, 1.0, 1.0);
            for (Entity entity : entityList) {
                if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                    if (!entity.hasMetadata("deadchest")) {
                        reloadMetaData();
                    }
                    if (entity.getMetadata("deadchest").size() > 0 && entity.getMetadata("deadchest").get(0).asBoolean()) {
                        if (chestData.isInfinity() || config.getInt(ConfigKey.DEADCHEST_DURATION) == 0) {
                            entity.setCustomName(local.get("loc_infinityChest"));
                        } else if (config.getBoolean(ConfigKey.SUSPEND_COUNTDOWNS_WHEN_PLAYER_IS_OFFLINE) && !Bukkit.getOfflinePlayer(UUID.fromString(chestData.getPlayerUUID())).isOnline()) {
                            entity.setCustomName("§7(Paused)");
                        } else {
                            long remainingSeconds = chestData.getTimeRemaining();
                            if (remainingSeconds > 0) {
                                long diffHours = remainingSeconds / 3600;
                                long diffMinutes = (remainingSeconds % 3600) / 60;
                                long diffSecs = remainingSeconds % 60;
                                entity.setCustomName(local.replaceTimer(local.get("holo_timer"), diffHours, diffMinutes, diffSecs));
                            } else {
                                entity.setCustomName("§cExpired");
                            }
                        }
                    }
                }
            }
        }
    }
}