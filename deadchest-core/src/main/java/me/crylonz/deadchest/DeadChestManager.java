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

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.inventory.PlayerInventory;
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

    // In DeadChestManager.java, add this new public static method
    public static void giveItemsToPlayer(Player player, ChestData chestData) {
        // Check DropMode from config, default to 1 (inventory) if not present
        if (config.getInt(ConfigKey.DROP_MODE) == 1) {
            final PlayerInventory playerInventory = player.getInventory();
            player.giveExp(chestData.getXpStored());
            for (ItemStack i : chestData.getInventory()) {
                if (i != null) {
                    if (Utils.isHelmet(i) && playerInventory.getHelmet() == null)
                        playerInventory.setHelmet(i);
                    else if (Utils.isBoots(i) && playerInventory.getBoots() == null)
                        playerInventory.setBoots(i);
                    else if (Utils.isChestplate(i) && playerInventory.getChestplate() == null)
                        playerInventory.setChestplate(i);
                    else if (Utils.isLeggings(i) && playerInventory.getLeggings() == null)
                        playerInventory.setLeggings(i);
                    else if (playerInventory.firstEmpty() != -1)
                        playerInventory.addItem(i);
                    else
                        player.getWorld().dropItemNaturally(player.getLocation(), i);
                }
            }
        } else {
            // DropMode 2: Drop all items on the ground at the player's location
            player.giveExp(chestData.getXpStored());
            for (ItemStack i : chestData.getInventory()) {
                if (i != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), i);
                }
            }
        }
    }

    // Inside DeadChestManager.java
    public static boolean handleExpirateDeadChest(ChestData chestData, Iterator<ChestData> chestDataIt) {
        if (chestData.isInfinity() || config.getInt(ConfigKey.DEADCHEST_DURATION) == 0) {
            return false; // Never expires
        }

        if (chestData.getTimeRemaining() <= 0) {
            Player player = Bukkit.getPlayer(UUID.fromString(chestData.getPlayerUUID()));

            // Expiration only happens if the player is online
            if (player != null && player.isOnline()) {
                Location loc = chestData.getChestLocation();
                World world = loc.getWorld();
                if (world == null) return false;

                int expireAction = config.getInt(ConfigKey.EXPIRE_ACTION);

                // Remove the physical grave block
                world.getBlockAt(loc).setType(Material.AIR);

                switch (expireAction) {
                    case 1: // Drop items at the grave
                        for (ItemStack item : chestData.getInventory()) {
                            if (item != null) world.dropItemNaturally(loc, item);
                        }
                        break;
                    case 2: // Return items to inventory (respecting DropMode)
                    case 3: // Drop items at the player (respecting DropMode)
                        giveItemsToPlayer(player, chestData); // Use the new helper method
                        break;
                    // Case 0 (delete items) is the default, do nothing.
                }

                // Play final retrieval sound/message if items weren't just deleted
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
                chestDataIt.remove(); // Remove the chest from the list
                return true; // The chest has been handled
            }
        }
        return false; // Not expired or player is offline
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
