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
        // ADD THESE TWO LINES
        this.hasPlayed30sWarning = chest.hasPlayed30sWarning();
        this.hasPlayed5sWarning = chest.hasPlayed5sWarning();
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

        ChestData newChestData = new ChestData( // Create the object first
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
        // Load the warning flags, defaulting to false if they don't exist (for old chests)
        newChestData.setPlayed30sWarning(map.get("hasPlayed30sWarning") != null && (boolean) map.get("hasPlayed30sWarning"));
        newChestData.setPlayed5sWarning(map.get("hasPlayed5sWarning") != null && (boolean) map.get("hasPlayed5sWarning"));

        return newChestData;
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
        map.put("hasPlayed30sWarning", hasPlayed30sWarning);
        map.put("hasPlayed5sWarning", hasPlayed5sWarning);
        return map;
    }

    public int getXpStored() {
        return xpStored;
    }

    public void setXpStored(int xpStored) {
        this.xpStored = xpStored;
    }

    enum Indexes {WORLD_NAME, LOC_X, LOC_Y, LOC_Z}

    // Inside ChestData.java
    private boolean hasPlayed30sWarning = false;
    private boolean hasPlayed5sWarning = false;

    public boolean hasPlayed30sWarning() { return hasPlayed30sWarning; }
    public void setPlayed30sWarning(boolean hasPlayed30sWarning) { this.hasPlayed30sWarning = hasPlayed30sWarning; }

    public boolean hasPlayed5sWarning() { return hasPlayed5sWarning; }
    public void setPlayed5sWarning(boolean hasPlayed5sWarning) { this.hasPlayed5sWarning = hasPlayed5sWarning; }
}
