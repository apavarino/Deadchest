/*
 * Copyright (c) 2023 Edgeburn Media. All rights reserved.
 */

package me.crylonz;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SerializableAs("ChestData")
public final class ChestData implements ConfigurationSerializable {

    private List<ItemStack> inventory;
    private Location chestLocation;
    private String playerName;
    private String playerUUID;
    private Date chestDate;
    private boolean isInfinity;
    private boolean isRemovedBlock;
    private Location holographicTimer;
    private UUID holographicTimerId;
    private UUID holographicOwnerId;
    private String worldName;

    private int xpStored;

    public ChestData(ChestData chest) {
        this.inventory = chest.getInventory();
        this.chestLocation = chest.getChestLocation();
        this.playerName = chest.getPlayerName();
        this.playerUUID = chest.getPlayerUUID();
        this.chestDate = chest.getChestDate();
        this.isRemovedBlock = chest.isRemovedBlock();
        this.isInfinity = chest.isInfinity();
        this.holographicTimer = chest.getHolographicTimer();
        this.holographicTimerId = chest.getHolographicTimerId();
        this.holographicOwnerId = chest.getHolographicOwnerId();
        this.worldName = chest.getWorldName();
        this.xpStored = chest.getXpStored();
    }

    ChestData(final Inventory inv,
              final Location chestLocation,
              final Player p,
              final boolean isInfinity,
              final ArmorStand asTimer,
              final ArmorStand owner,
              final int xpToStore) {

        if (p != null) {
            this.inventory = Arrays.asList(inv.getContents());
            this.chestLocation = chestLocation.clone();
            this.playerName = p.getName();
            this.playerUUID = String.valueOf(p.getUniqueId());
            this.chestDate = new Date();
            this.isInfinity = isInfinity;
            this.isRemovedBlock = false;
            this.holographicTimer = asTimer.getLocation().clone();
            this.holographicTimerId = asTimer.getUniqueId();
            this.holographicOwnerId = owner.getUniqueId();
            this.xpStored = xpToStore;
            if (chestLocation.getWorld() != null)
                this.worldName = chestLocation.getWorld().getName();
        }
    }

    public ChestData(final List<ItemStack> inventory,
                     final Location chestLocation,
                     final String playerName,
                     final String playerUUID,
                     final Date chestDate,
                     final boolean isInfinity,
                     final boolean isRemovedBlock,
                     final Location holographicTimer,
                     final UUID asTimerId,
                     final UUID asOwnerId,
                     final String worldName,
                     final int xpStored) {
        this.inventory = inventory;
        this.chestLocation = chestLocation;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.chestDate = chestDate;
        this.isRemovedBlock = isRemovedBlock;
        this.isInfinity = isInfinity;
        this.holographicTimer = holographicTimer;
        this.holographicTimerId = asTimerId;
        this.holographicOwnerId = asOwnerId;
        this.worldName = worldName;
        this.xpStored = xpStored;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public static ChestData deserialize(final Map<String, Object> map) {

        String[] loc = ((String) map.get("chestLocation")).split(";");
        String[] locHolo = ((String) map.get("holographicTimer")).split(";");

        Location myloc = new Location(Bukkit.getWorld(loc[Indexes.WORLD_NAME.ordinal()]),
                Double.parseDouble(loc[Indexes.LOC_X.ordinal()]), Double.parseDouble(loc[Indexes.LOC_Y.ordinal()]),
                Double.parseDouble(loc[Indexes.LOC_Z.ordinal()]));

        Location mylocHolo = new Location(Bukkit.getWorld(locHolo[Indexes.WORLD_NAME.ordinal()]),
                Double.parseDouble(locHolo[Indexes.LOC_X.ordinal()]), Double.parseDouble(locHolo[Indexes.LOC_Y.ordinal()]),
                Double.parseDouble(locHolo[Indexes.LOC_Z.ordinal()]));

        return new ChestData(
                (List<ItemStack>) map.get("inventory"),
                myloc,
                (String) map.get("playerName"),
                (String) map.get("playerUUID"),
                (Date) map.get("chestDate"),
                (boolean) map.get("isInfinity"),
                map.get("isRemovedBlock") != null && (boolean) map.get("isRemovedBlock"), // compatiblity under 4.14
                mylocHolo,
                UUID.fromString((String) map.get("as_timer_id")),
                UUID.fromString((String) map.get("as_owner_id")),
                (String) map.get("worldName"),
                (int) (map.get("xpStored") != null ? map.get("xpStored") : 0)  // compatiblity under 4.15
        );
    }

    public UUID getHolographicTimerId() {
        return holographicTimerId;
    }

    public UUID getHolographicOwnerId() {
        return holographicOwnerId;
    }

    public void setHolographicOwnerId(UUID holographicOwnerId) {
        this.holographicOwnerId = holographicOwnerId;
    }

    public List<ItemStack> getInventory() {
        return inventory;
    }

    public void cleanInventory() {
        inventory = new ArrayList<>();
    }

    public void setInventory(List<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public void setRemovedBlock(final boolean removedBlock) {
        this.isRemovedBlock = removedBlock;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public Date getChestDate() {
        return chestDate;
    }

    public boolean isRemovedBlock() {
        return isRemovedBlock;
    }

    public boolean isInfinity() {
        return isInfinity;
    }

    public Location getHolographicTimer() {
        return holographicTimer;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean removeArmorStand() {
        final int radius = 1;
        final int armorStandShiftY = 1;

        if (chestLocation.getWorld() != null) {

            chestLocation.getChunk().setForceLoaded(true);

            Collection<Entity> entities = chestLocation.getWorld().getNearbyEntities(
                    new Location(
                            chestLocation.getWorld(),
                            chestLocation.getX(),
                            chestLocation.getY() + armorStandShiftY,
                            chestLocation.getZ()
                    ), radius, radius, radius);

            boolean isEmpty = entities.size() > 0;
            for (Entity entity : entities) {
                if (entity.getUniqueId().equals(holographicOwnerId) || entity.getUniqueId().equals(holographicTimerId)) {
                    entity.remove();
                }
            }

            if (isChunkForceLoaded()) {
                chestLocation.getWorld().unloadChunk(chestLocation.getBlockX() >> 4, chestLocation.getBlockZ() >> 4);
                chestLocation.getChunk().setForceLoaded(false);
            }

            return isEmpty;
        }
        return false;
    }

    public boolean isChunkLoaded() {
        return chestLocation.getWorld() == null ||
                chestLocation.getWorld().isChunkLoaded(
                        chestLocation.getBlockX() >> 4,
                        chestLocation.getBlockZ() >> 4
                );
    }

    public boolean isChunkForceLoaded() {
        return chestLocation.getWorld() == null ||
                chestLocation.getWorld().isChunkForceLoaded(
                        chestLocation.getBlockX() >> 4,
                        chestLocation.getBlockZ() >> 4
                );
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("inventory", inventory);
        map.put("chestLocation", worldName + ";" + chestLocation.getX() + ";" + chestLocation.getY() + ";"
                + chestLocation.getZ());
        map.put("playerName", playerName);
        map.put("playerUUID", playerUUID);
        map.put("chestDate", chestDate);
        map.put("isRemovedBlock", isRemovedBlock);
        map.put("isInfinity", isInfinity);
        map.put("holographicTimer", worldName + ";" + holographicTimer.getX() + ";" + holographicTimer.getY()
                + ";" + holographicTimer.getZ());
        map.put("worldName", worldName);
        map.put("as_timer_id", holographicTimerId.toString());
        map.put("as_owner_id", holographicOwnerId.toString());
        map.put("xpStored", xpStored);
        return map;
    }

    public int getXpStored() {
        return xpStored;
    }

    public void setXpStored(int xpStored) {
        this.xpStored = xpStored;
    }

    enum Indexes {WORLD_NAME, LOC_X, LOC_Y, LOC_Z}
}
