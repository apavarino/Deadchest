package me.crylonz.deadchest.cache;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.db.ChestDataRepository;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class DeadChestCache {
    private final Map<Location, ChestData> chestDataMap = new HashMap<>();
    private final Map<UUID, Set<Location>> players = new HashMap<>();


    public void addChestData(final ChestData chestData) {
        addPlayerData(chestData);
        chestDataMap.put(chestData.getChestLocation(), chestData);

    }

    public void addListOfChestData(final Set<ChestData> chestThatNeedsBeUpdated) {
        chestThatNeedsBeUpdated.forEach(( chestData) -> {
            if (chestData == null) {
                return;
            }
            addPlayerData(chestData);
            chestDataMap.putIfAbsent(chestData.getChestLocation(), chestData);
        });
        ChestDataRepository.saveAllAsync(chestThatNeedsBeUpdated);
    }

    public void setChestData(final List<ChestData> chests) {
        if (chests != null) {
            chestDataMap.clear();
            players.clear();
            chests.forEach(this::addChestData);
        }
    }

    @Nullable
    public ChestData getChestData(@Nonnull final Location location) {
        return chestDataMap.get(location);
    }

    @Nullable
    public Set<Location> getPlayerLinkedData(@Nonnull final Player player) {
        return players.get(player.getUniqueId());
    }

    public void getPlayerLinkedDeadChestData(@Nonnull final Player player, @Nonnull final Consumer<ChestData> dataConsumer) {
        final Set<Location> locations = getPlayerLinkedData(player);
        if (locations != null) {
            locations.forEach(location -> {
                final ChestData chestData = getChestData(location);
                if (chestData != null) {
                    dataConsumer.accept(chestData);
                }
            });
        }
    }

    public boolean contains(@Nonnull final ChestData chestData) {
        return getChestData(chestData.getChestLocation()) != null;
    }

    public boolean isEmpty() {
        return chestDataMap.isEmpty();
    }

    public Map<Location, ChestData> getAllChestData() {
        return Collections.unmodifiableMap(chestDataMap);
    }

    public int getPlayerChestAmount(@Nonnull final Player player) {
        final Set<Location> locations = players.get(player.getUniqueId());
        if (locations != null) {
            return locations.size();
        }
        return 0;
    }

    public List<ChestData> getChestData() {
        return Collections.unmodifiableList(new ArrayList<>(chestDataMap.values()));
    }

    public void removeChestData(@Nonnull final ChestData chest) {
        chest.removeArmorStand();
        chest.remove();
        removePlayerData(chest);
        chestDataMap.remove(chest.getChestLocation());
    }

    public void removeChestData(@Nonnull final Location location) {
        chestDataMap.compute(location, (chestLoc, chestData) -> {
            if (chestData != null && chestData.getChestLocation().equals(location)) {
                chestData.removeArmorStand();
                chestData.remove();
                removePlayerData(chestData);
                return null;
            }
            return chestData;
        });
    }

    public void removeChestDataList(@Nonnull final Collection<ChestData> chests) {
        chests.forEach(chestData -> {
            chestData.removeArmorStand();
            this.chestDataMap.remove(chestData.getChestLocation());
            removePlayerData(chestData);
        });
        ChestDataRepository.removeBatchAsync(chests);
    }

    public List<ChestData> getChestDataMap() {
        return Collections.unmodifiableList(new ArrayList<>(chestDataMap.values()));
    }

    public void clearChestData() {
        chestDataMap.clear();
        players.clear();
        ChestDataRepository.clearAsync();
    }

    public void save() {
        ChestDataRepository.batchSave(chestDataMap.values());
    }


    private void addPlayerData(final ChestData chestData) {
        if (chestData.getPlayerUUID() == null) {
            return;
        }
        players.computeIfAbsent(chestData.getPlayerUUID(), uuid -> new HashSet<>())
                .add(chestData.getChestLocation());
    }

    private void removePlayerData(final ChestData chestData) {
        if (chestData.getPlayerUUID() == null) return;
        final UUID playerUUID = chestData.getPlayerUUID();
        Set<Location> locations = players.get(playerUUID);
        if (locations != null) {
            locations.remove(chestData.getChestLocation());
            if (locations.isEmpty()) {
                players.remove(playerUUID);
            }
        }
    }


}
