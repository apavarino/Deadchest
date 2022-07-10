package me.crylonz;

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

import java.util.*;

import static me.crylonz.DeadChest.*;
import static me.crylonz.Utils.computeChestType;
import static me.crylonz.Utils.isGraveBlock;

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
     * Generate an hologram at the given position
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

    public static boolean handleExpirateDeadChest(ChestData chestData, Iterator<ChestData> chestDataIt, Date date) {
        if (chestData.getChestDate().getTime() + chestDuration * 1000L < date.getTime() && !chestData.isInfinity()
                && chestDuration != 0) {

            Location loc = chestData.getChestLocation();

            if (loc.getWorld() != null) {
                loc.getWorld().getBlockAt(loc).setType(Material.AIR);
                if (itemsDroppedAfterTimeOut) {
                    for (ItemStack itemStack : chestData.getInventory()) {
                        if (itemStack != null) {
                            loc.getWorld().dropItemNaturally(loc, itemStack);
                        }
                    }
                     chestData.cleanInventory();
                }
            }
            if (chestData.removeArmorStand()) {
                chestDataIt.remove();
            }

            return true;
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
                        long diff = date.getTime() - (chestData.getChestDate().getTime() + chestDuration * 1000L);
                        long diffSeconds = Math.abs(diff / 1000 % 60);
                        long diffMinutes = Math.abs(diff / (60 * 1000) % 60);
                        long diffHours = Math.abs(diff / (60 * 60 * 1000));

                        if (!chestData.isInfinity() && chestDuration != 0) {
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
