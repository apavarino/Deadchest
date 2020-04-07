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
public class ChestData implements ConfigurationSerializable {

    private List<ItemStack> inventory;
    private Location chestLocation;
    private String playerName;
    private String playerUUID;
    private Date chestDate;
    private boolean isInfiny;
    private Location holographicTimer;
    private UUID holographic_timer_id;
    private UUID holographic_owner_id;
    private String worldName;

    ChestData(Inventory inv, Location chestLocation, Player p, boolean isInfiny, ArmorStand as_timer, ArmorStand owner) {

        if (p != null) {
            this.inventory = Arrays.asList(inv.getContents());
            this.chestLocation = chestLocation.clone();
            this.playerName = p.getName();
            this.playerUUID = String.valueOf(p.getUniqueId());
            this.chestDate = new Date();
            this.isInfiny = isInfiny;
            this.holographicTimer = as_timer.getLocation().clone();
            this.holographic_timer_id = as_timer.getUniqueId();
            this.holographic_owner_id = owner.getUniqueId();
            this.worldName = chestLocation.getWorld().getName();
        }


    }

    public ChestData(List<ItemStack> inventory, Location chestLocation, String playerName, String playerUUID,
                     Date chestDate, boolean isInfiny, Location holographicTimer, UUID as_timer_id, UUID as_owner_id
            , String worldName) {
        this.inventory = inventory;
        this.chestLocation = chestLocation;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.chestDate = chestDate;
        this.isInfiny = isInfiny;
        this.holographicTimer = holographicTimer;
        this.holographic_timer_id = as_timer_id;
        this.holographic_owner_id = as_owner_id;
        this.worldName = worldName;

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

    public UUID getHolographic_owner_id() {
        return holographic_owner_id;
    }

    public UUID getHolographic_timer_id() {
        return holographic_timer_id;
    }

    public void removeArmorStand() {

        if (chestLocation.getWorld() == null)
            return;

        Collection<Entity> entities = chestLocation.getWorld().getNearbyEntities(
                new Location(chestLocation.getWorld(), chestLocation.getX(), chestLocation.getY() - 1, chestLocation.getZ()), 1, 1, 1);

        for (Entity entity : entities) {
            if (entity.getUniqueId().equals(holographic_owner_id) || entity.getUniqueId().equals(holographic_timer_id)) {
                entity.remove();
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("inventory", inventory);
        map.put("chestLocation", worldName + ";" + chestLocation.getX() + ";" + chestLocation.getY() + ";" + chestLocation.getZ());
        map.put("playerName", playerName);
        map.put("playerUUID", playerUUID);
        map.put("chestDate", chestDate);
        map.put("isInfiny", isInfiny);
        map.put("holographicTimer", worldName + ";" + holographicTimer.getX() + ";" + holographicTimer.getY() + ";" + holographicTimer.getZ());
        map.put("worldName", worldName);
        map.put("as_timer_id", holographic_timer_id.toString());
        map.put("as_owner_id", holographic_owner_id.toString());
        return map;
    }

    public static ChestData deserialize(Map<String, Object> map) {
        String loc[] = ((String) map.get("chestLocation")).split(";");
        String locHolo[] = ((String) map.get("holographicTimer")).split(";");
        Location myloc = new Location(Bukkit.getWorld(loc[0]), Double.parseDouble(loc[1]), Double.parseDouble(loc[2]), Double.parseDouble(loc[3]));
        Location mylocHolo = new Location(Bukkit.getWorld(locHolo[0]), Double.parseDouble(locHolo[1]), Double.parseDouble(locHolo[2]), Double.parseDouble(locHolo[3]));
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
}
