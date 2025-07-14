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

import org.bukkit.Sound;
import java.util.UUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import static me.crylonz.deadchest.DeadChest.*;
import static me.crylonz.deadchest.Utils.computeChestType;
import static me.crylonz.deadchest.Utils.isGraveBlock;

public class DeadChestManager {

    /**
     * Remove all active deadchests
     *
     * @return number of deadchests removed
     */
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

    /**
     * Generate a hologram at the given position
     *
     * @param location position to place
     * @param text     text to display
     * @param shiftX   x shifting
     * @param shiftY   y shifting
     * @param shiftZ   z shifting
     * @return the generated armorstand
     */
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

    /**
     * get the number of deadchest for a player
     *
     * @param p player
     * @return number of deadchests
     */
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

    /**
     * Regeneration of metaData for holos
     */
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

    // Inside DeadChestManager.java
    public static boolean handleExpirateDeadChest(ChestData chestData, Iterator<ChestData> chestDataIt, Date date) {
        // This part of the method stays the same
        if (chestData.getChestDate().getTime() + config.getInt(ConfigKey.DEADCHEST_DURATION) * 1000L < date.getTime() && !chestData.isInfinity()
                && config.getInt(ConfigKey.DEADCHEST_DURATION) != 0) {

            Location loc = chestData.getChestLocation();
            World world = loc.getWorld();

            if (world != null) {

                // --- NEW LOGIC START ---
                int expireAction = config.getInt(ConfigKey.EXPIRE_ACTION);
                Player player = Bukkit.getPlayer(UUID.fromString(chestData.getPlayerUUID()));

                // Only try to return items if the player is online
                if (player != null && player.isOnline()) {
                    world.getBlockAt(loc).setType(Material.AIR); // Remove the grave block

                    if (expireAction == 1 || expireAction == 3) { // Actions for returning items to inventory
                        player.giveExp(chestData.getXpStored());
                        for (ItemStack item : chestData.getInventory()) {
                            if (item != null) {
                                // This checks for empty slots and adds the item. If the inventory is full, it drops the item at the player's feet.
                                if (player.getInventory().firstEmpty() == -1) {
                                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                                } else {
                                    player.getInventory().addItem(item);
                                }
                            }
                        }
                        player.sendMessage(local.get("loc_prefix") + local.get("loc_gbplayer"));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                    } else if (expireAction == 2) { // Action for dropping items on the ground at the chest
                        for (ItemStack itemStack : chestData.getInventory()) {
                            if (itemStack != null) {
                                world.dropItemNaturally(loc, itemStack);
                            }
                        }
                    }
                    // If expireAction is 0, we do nothing, and the items are deleted below.

                    chestData.removeArmorStand();
                    chestDataIt.remove(); // Remove the chest from the list
                    return true; // The chest has been handled

                } else {
                    // If the player is offline, we do NOT expire the chest. The timer will just continue the next time they log in.
                    // This is the intended behavior with SuspendCountdownsWhenPlayerIsOffline = true
                    return false;

                }
                // --- NEW LOGIC END ---
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
                        long diff = date.getTime() - (chestData.getChestDate().getTime() + config.getInt(ConfigKey.DEADCHEST_DURATION) * 1000L);
                        long diffSeconds = Math.abs(diff / 1000 % 60);
                        long diffMinutes = Math.abs(diff / (60 * 1000) % 60);
                        long diffHours = Math.abs(diff / (60 * 60 * 1000));

                        if (!chestData.isInfinity() && config.getInt(ConfigKey.DEADCHEST_DURATION) != 0) {
                            entity.setCustomName(local.replaceTimer(local.get("holo_timer"), diffHours, diffMinutes, diffSeconds));
                        } else {
                            entity.setCustomName(local.get("loc_infinityChest"));
                        }
                    }
                }
            }
        }
    }
}
