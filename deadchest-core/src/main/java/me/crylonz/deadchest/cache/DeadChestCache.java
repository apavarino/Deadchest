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
    private final Map<Location, ChestData> chestDataList = new HashMap<>();
    private final Map<UUID, Set<Location>> players = new HashMap<>();


    public void addChestData(final ChestData chestData) {
        if (chestDataList == null) {
            setChestData(new ArrayList<>());
        }
        addPlayerData(chestData);
        chestDataList.put(chestData.getChestLocation(), chestData);

    }

    public void addListOfChestData(final Set<ChestData> chestThatNeedsBeUpdated) {
        chestThatNeedsBeUpdated.forEach(( chestData) -> {
            if (chestData == null) {
                return;
            }
            addPlayerData(chestData);
            chestDataList.putIfAbsent(chestData.getChestLocation(), chestData);
        });
        ChestDataRepository.saveAllAsync(chestThatNeedsBeUpdated);
    }

    public void setChestData(final List<ChestData> chests) {
        if (chests != null) {
            chestDataList.clear();
            chests.forEach(this::addPlayerData);
        }
    }

    @Nullable
    public ChestData getChestData(@Nonnull final Location location) {
        return chestDataList.get(location);
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
        return chestDataList.isEmpty();
    }

    public Map<Location, ChestData> getAllChestData() {
        return Collections.unmodifiableMap(chestDataList);
    }

    public int getPlayerChestAmount(@Nonnull final Player player) {
        final Set<Location> locations = players.get(player.getUniqueId());
        if (locations != null) {
            return locations.size();
        }
        return 0;
    }

    public List<ChestData> getChestData() {
        if (chestDataList == null)
            return new ArrayList<>();
        return Collections.unmodifiableList(new ArrayList<>(chestDataList.values()));
    }

    public void removeChestData(@Nonnull final ChestData chest) {
        chest.removeArmorStand();
        chest.remove();
        removePlayerData(chest);
        chestDataList.remove(chest.getChestLocation());
    }

    public void removeChestData(@Nonnull final Location location) {
        chestDataList.compute(location, (chestLoc, chestData) -> {
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
            this.chestDataList.remove(chestData.getChestLocation());
            removePlayerData(chestData);
        });
        ChestDataRepository.removeBatchAsync(chests);
    }

    public List<ChestData> getChestDataList() {
        if (chestDataList == null)
            return new ArrayList<>();
        return Collections.unmodifiableList(new ArrayList<>(chestDataList.values()));
    }

    public void clearChestData() {
        if (chestDataList != null)
            chestDataList.clear();
        players.clear();
        ChestDataRepository.clearAsync();
    }

    public void save() {
        ChestDataRepository.batchSave(chestDataList.values());
    }


    private void addPlayerData(final ChestData chestData) {
        if (chestData.getPlayerUUID() == null) return;
        players.computeIfAbsent(chestData.getPlayerUUID(), k -> new HashSet<>())
                .add(chestData.getChestLocation());
    }

    private void removePlayerData(final ChestData chestData) {
        if (chestData.getPlayerUUID() == null) return;
        final UUID playerUUID = chestData.getPlayerUUID();
        Set<Location> set = players.get(playerUUID);
        if (set != null) {
            set.remove(chestData.getChestLocation());
            if (set.isEmpty()) {
                players.remove(playerUUID);
            }
        }
    }


}
