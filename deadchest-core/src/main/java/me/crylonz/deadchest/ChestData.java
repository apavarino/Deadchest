package me.crylonz.deadchest;

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

import me.crylonz.deadchest.utils.ConfigKey;

@SerializableAs("ChestData")
public final class ChestData implements ConfigurationSerializable {

    private final ItemStack[] inventory;
    private final Location chestLocation;
    private final String playerName;
    private final String playerUUID;
    private final Date chestDate;
    private final boolean isInfinity;
    private boolean isRemovedBlock;
    private final Location holographicTimer;
    private UUID holographicTimerId;
    private UUID holographicOwnerId;
    private final String worldName;

    private int xpStored;
    private List<Integer> triggeredWarnings;

    private long timeRemaining;

    // RESTORED COPY CONSTRUCTOR
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
        this.timeRemaining = chest.getTimeRemaining();
        this.triggeredWarnings = new ArrayList<>(chest.getTriggeredWarnings());
    }

    public List<Integer> getTriggeredWarnings() {
        return triggeredWarnings;
    }

    public long getTimeRemaining() { return timeRemaining; }

    public void setTimeRemaining(long timeRemaining) { this.timeRemaining = timeRemaining; }

    ChestData(final Inventory inv,
              final Location chestLocation,
              final Player p,
              final boolean isInfinity,
              final ArmorStand asTimer,
              final ArmorStand owner,
              final int xpToStore) {

        this.inventory = inv.getContents();
        this.chestLocation = chestLocation;
        this.playerName = p.getName();
        this.playerUUID = String.valueOf(p.getUniqueId());
        this.chestDate = new Date();
        this.isInfinity = isInfinity;
        this.isRemovedBlock = false;
        this.holographicTimer = asTimer.getLocation();
        this.holographicTimerId = asTimer.getUniqueId();
        this.holographicOwnerId = owner.getUniqueId();
        this.worldName = p.getWorld().getName();
        this.xpStored = xpToStore;
        this.timeRemaining = DeadChest.config.getInt(ConfigKey.DEADCHEST_DURATION);
        this.triggeredWarnings = new ArrayList<>();
    }


    @SuppressWarnings("unchecked")
    public static ChestData deserialize(final Map<String, Object> map) {
        return new ChestData(
                ((List<ItemStack>) map.get("inventory")).toArray(new ItemStack[0]),
                Location.deserialize((Map<String, Object>) map.get("chestLocation")),
                (String) map.get("playerName"),
                (String) map.get("playerUUID"),
                (Date) map.get("chestDate"),
                (boolean) map.get("isInfinity"),
                (Location) map.get("holographicTimer"),
                UUID.fromString((String) map.get("as_timer_id")),
                UUID.fromString((String) map.get("as_owner_id")),
                (int) map.get("xpStored"),
                (List<Integer>) map.get("triggeredWarnings"),
                map.containsKey("timeRemaining") ? ((Number) map.get("timeRemaining")).longValue() : -1
        );
    }

    private ChestData(ItemStack[] inventory, Location chestLocation, String playerName, String playerUUID, Date chestDate,
                      boolean isInfinity, Location holographicTimer, UUID asTimerId, UUID asOwnerId, int xpStored,
                      List<Integer> triggeredWarnings, long timeRemaining) {
        this.inventory = inventory;
        this.chestLocation = chestLocation;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.chestDate = chestDate;
        this.isInfinity = isInfinity;
        this.holographicTimer = holographicTimer;
        this.holographicTimerId = asTimerId;
        this.holographicOwnerId = asOwnerId;
        this.xpStored = xpStored;
        this.worldName = chestLocation.getWorld().getName();
        this.triggeredWarnings = triggeredWarnings;

        if (timeRemaining == -1) {
            long timeSinceCreation = (new Date().getTime() - this.getChestDate().getTime()) / 1000;
            this.timeRemaining = DeadChest.config.getInt(ConfigKey.DEADCHEST_DURATION) - timeSinceCreation;
        } else {
            this.timeRemaining = timeRemaining;
        }
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

    public ItemStack[] getInventory() {
        return inventory;
    }

    public boolean isRemovedBlock() {
        return isRemovedBlock;
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

    public boolean isInfinity() {
        return isInfinity;
    }

    public Location getHolographicTimer() {
        return holographicTimer;
    }

    public String getWorldName() {
        return worldName;
    }

    public void removeArmorStand() {

        final int radius = 1;
        final int armorStandShiftY = 1;

        if (chestLocation.getWorld() != null) {

            Collection<Entity> entities = chestLocation.getWorld().getNearbyEntities(
                    new Location(
                            chestLocation.getWorld(),
                            chestLocation.getX(),
                            chestLocation.getY() + armorStandShiftY,
                            chestLocation.getZ()
                    ), radius, radius, radius);

            for (Entity entity : entities) {
                if (entity.getUniqueId().equals(holographicOwnerId) || entity.getUniqueId().equals(holographicTimerId)) {
                    entity.remove();
                }
            }

        }
    }

    public boolean isChunkLoaded() {
        return chestLocation.getWorld() != null &&
                chestLocation.getWorld().isChunkLoaded(
                        chestLocation.getBlockX() >> 4,
                        chestLocation.getBlockZ() >> 4
                );
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("inventory", Arrays.asList(inventory));
        map.put("chestLocation", chestLocation.serialize());
        map.put("playerName", playerName);
        map.put("playerUUID", playerUUID);
        map.put("chestDate", chestDate);
        map.put("isInfinity", isInfinity);
        map.put("holographicTimer", holographicTimer);
        map.put("as_timer_id", holographicTimerId.toString());
        map.put("as_owner_id", holographicOwnerId.toString());
        map.put("xpStored", xpStored);
        map.put("triggeredWarnings", triggeredWarnings);
        map.put("timeRemaining", timeRemaining);
        return map;
    }

    public int getXpStored() {
        return xpStored;
    }
}