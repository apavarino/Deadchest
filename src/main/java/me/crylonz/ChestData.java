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
    private boolean isInfiny;
    private Location holographicTimer;
    private UUID holographicTimerId;
    private UUID holographicOwnerId;
    private String worldName;

    ChestData(final Inventory inv, final Location chestLocation, final Player p, final boolean isInfiny, final ArmorStand asTimer, final ArmorStand owner) {

        if (p != null) {
            this.inventory = Arrays.asList(inv.getContents());
            this.chestLocation = chestLocation.clone();
            this.playerName = p.getName();
            this.playerUUID = String.valueOf(p.getUniqueId());
            this.chestDate = new Date();
            this.isInfiny = isInfiny;
            this.holographicTimer = asTimer.getLocation().clone();
            this.holographicTimerId = asTimer.getUniqueId();
            this.holographicOwnerId = owner.getUniqueId();
            if (chestLocation.getWorld() != null)
                this.worldName = chestLocation.getWorld().getName();
        }


    }

    public ChestData(final List<ItemStack> inventory, final Location chestLocation,
                     final String playerName, final String playerUUID,
                     final Date chestDate, final boolean isInfiny,
                     final Location holographicTimer, final UUID asTimerId,
                     final UUID asOwnerId, final String worldName) {
        this.inventory = inventory;
        this.chestLocation = chestLocation;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.chestDate = chestDate;
        this.isInfiny = isInfiny;
        this.holographicTimer = holographicTimer;
        this.holographicTimerId = asTimerId;
        this.holographicOwnerId = asOwnerId;
        this.worldName = worldName;

    }

    public static ChestData deserialize(final Map<String, Object> map) {
        final int worldNameIndex = 0;
        final int locXIndex = 1;
        final int locYIndex = 2;
        final int locZIndex = 3;

        String[] loc = ((String) map.get("chestLocation")).split(";");
        String[] locHolo = ((String) map.get("holographicTimer")).split(";");

        Location myloc = new Location(Bukkit.getWorld(loc[worldNameIndex]),
                Double.parseDouble(loc[locXIndex]), Double.parseDouble(loc[locYIndex]),
                Double.parseDouble(loc[locZIndex]));

        Location mylocHolo = new Location(Bukkit.getWorld(locHolo[worldNameIndex]),
                Double.parseDouble(locHolo[locXIndex]), Double.parseDouble(locHolo[locYIndex]),
                Double.parseDouble(locHolo[locZIndex]));

        return new ChestData(
                (List<ItemStack>) map.get("inventory"),
                myloc,
                (String) map.get("playerName"),
                (String) map.get("playerUUID"),
                (Date) map.get("chestDate"),
                (boolean) map.get("isInfiny"),
                mylocHolo,
                UUID.fromString((String) map.get("as_timer_id")),
                UUID.fromString((String) map.get("as_owner_id")),
                (String) map.get("worldName")
        );
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

    public boolean isInfiny() {
        return isInfiny;
    }

    public Location getHolographicTimer() {
        return holographicTimer;
    }

    public void removeArmorStand() {

        final int radius = 1;
        final int armorStandShiftY = 1;

        if (chestLocation.getWorld() != null) {

            Collection<Entity> entities = chestLocation.getWorld().getNearbyEntities(
                    new Location(chestLocation.getWorld(), chestLocation.getX(), chestLocation.getY() - armorStandShiftY,
                            chestLocation.getZ()), radius, radius, radius);

            for (Entity entity : entities) {
                if (entity.getUniqueId().equals(holographicOwnerId) || entity.getUniqueId().equals(holographicTimerId)) {
                    entity.remove();
                }
            }
        }
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
        map.put("isInfiny", isInfiny);
        map.put("holographicTimer", worldName + ";" + holographicTimer.getX() + ";" + holographicTimer.getY()
                + ";" + holographicTimer.getZ());
        map.put("worldName", worldName);
        map.put("as_timer_id", holographicTimerId.toString());
        map.put("as_owner_id", holographicOwnerId.toString());
        return map;
    }
}
