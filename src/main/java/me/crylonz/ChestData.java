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

import java.text.MessageFormat;
import java.util.*;

@SerializableAs("ChestData")
public final class ChestData implements ConfigurationSerializable {

    private List<ItemStack> inventory;
    private Location chestLocation;
    private String playerName;
    private String playerUUID;
    private Date chestDate;
    private boolean isInfinity;
    private Location holographicTimer;
    private UUID holographicTimerId;
    private UUID holographicOwnerId;
    private String worldName;

    ChestData(final Inventory inv, final Location chestLocation, final Player p, final boolean isInfinity, final ArmorStand asTimer, final ArmorStand owner) {

        if (p != null) {
            this.inventory = Arrays.asList(inv.getContents());
            this.chestLocation = chestLocation.clone();
            this.playerName = p.getName();
            this.playerUUID = String.valueOf(p.getUniqueId());
            this.chestDate = new Date();
            this.isInfinity = isInfinity;
            this.holographicTimer = asTimer.getLocation().clone();
            this.holographicTimerId = asTimer.getUniqueId();
            this.holographicOwnerId = owner.getUniqueId();
            if (chestLocation.getWorld() != null)
                this.worldName = chestLocation.getWorld().getName();
        }
    }

    public ChestData(final List<ItemStack> inventory, final Location chestLocation,
                     final String playerName, final String playerUUID,
                     final Date chestDate, final boolean isInfinity,
                     final Location holographicTimer, final UUID asTimerId,
                     final UUID asOwnerId, final String worldName) {
        this.inventory = inventory;
        this.chestLocation = chestLocation;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.chestDate = chestDate;
        this.isInfinity = isInfinity;
        this.holographicTimer = holographicTimer;
        this.holographicTimerId = asTimerId;
        this.holographicOwnerId = asOwnerId;
        this.worldName = worldName;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public static ChestData deserialize(final Map<String, Object> map) {
        return new ChestData(
                (List<ItemStack>) map.get("inventory"),
                deserializeLocation((String) map.get("chestLocation")),
                (String) map.get("playerName"),
                (String) map.get("playerUUID"),
                (Date) map.get("chestDate"),
                (boolean) map.get("isInfinity"),
                deserializeLocation((String) map.get("holographicTimer")),
                UUID.fromString((String) map.get("as_timer_id")),
                UUID.fromString((String) map.get("as_owner_id")),
                (String) map.get("worldName")
        );
    }

    public static String serializeLocation(String worldName, Location location) {
        return MessageFormat.format("{0};{1};{2};{3}", worldName, location.getX(), location.getY(), location.getZ());
    }

    public static Location deserializeLocation(String locationStr) {
        String[] location = locationStr.split(";");
        return new Location(
                Bukkit.getWorld(location[Indexes.WORLD_NAME.ordinal()]),
                Double.parseDouble(location[Indexes.LOC_X.ordinal()]),
                Double.parseDouble(location[Indexes.LOC_Y.ordinal()]),
                Double.parseDouble(location[Indexes.LOC_Z.ordinal()]));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("inventory", inventory);
        map.put("chestLocation", serializeLocation(worldName, chestLocation));
        map.put("playerName", playerName);
        map.put("playerUUID", playerUUID);
        map.put("chestDate", chestDate);
        map.put("isInfinity", isInfinity);
        map.put("holographicTimer", serializeLocation(worldName, holographicTimer));
        map.put("worldName", worldName);
        map.put("as_timer_id", holographicTimerId.toString());
        map.put("as_owner_id", holographicOwnerId.toString());
        return map;
    }

    public void removeArmorStand() {

        final int radius = 1;
        final int armorStandShiftY = 1;

        if (chestLocation.getWorld() != null) {

            Collection<Entity> entities = chestLocation.getWorld().getNearbyEntities(
                    new Location(chestLocation.getWorld(), chestLocation.getX(), chestLocation.getY() + armorStandShiftY,
                            chestLocation.getZ()), radius, radius, radius);

            for (Entity entity : entities) {
                if (entity.getUniqueId().equals(holographicOwnerId) || entity.getUniqueId().equals(holographicTimerId)) {
                    entity.remove();
                }
            }
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

    public List<ItemStack> getInventory() {
        return inventory;
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

    enum Indexes {WORLD_NAME, LOC_X, LOC_Y, LOC_Z}
}
