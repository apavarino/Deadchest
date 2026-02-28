package me.crylonz.deadchest.db;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryChestStoreTest {

    private ServerMock server;
    private WorldMock world;
    private Plugin plugin;
    private InMemoryChestStore store;

    @BeforeEach
    void setUp() throws Exception {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        plugin = MockBukkit.createMockPlugin();

        DeadChestLoader.plugin = plugin;
        DeadChestLoader.log = Logger.getLogger("InMemoryChestStoreTest");
        DeadChestLoader.sqlExecutor = new SQLExecutor();
        DeadChestLoader.db = new SQLite(plugin);

        Path dbPath = plugin.getDataFolder().toPath().resolve("data.db");
        Files.createDirectories(plugin.getDataFolder().toPath());
        Files.deleteIfExists(dbPath);

        DeadChestLoader.db.init();
        ChestDataRepository.initTable(() -> {
        });
        IgnoreItemListRepository.initTable();
        awaitAsyncDb();

        store = new InMemoryChestStore();
    }

    @AfterEach
    void tearDown() {
        DeadChestLoader.sqlExecutor.shutdown();
        DeadChestLoader.db.close();
        DeadChestLoader.sqlExecutor = new SQLExecutor();
        MockBukkit.unmock();
    }

    @Test
    void addChestDataStoresInBothIndexes() {
        Player player = server.addPlayer("Steve");
        ChestData chest = chestDataAt(10, player.getName(), player.getUniqueId(), Material.DIAMOND, 2);

        store.addChestData(chest);

        assertFalse(store.isEmpty());
        assertTrue(store.contains(chest));
        assertEquals(1, store.getPlayerChestAmount(player));
        assertEquals(chest, store.getChestData(chest.getChestLocation()));
    }

    @Test
    void setChestDataReplacesExistingDataset() {
        Player old = server.addPlayer("Old");
        Player fresh = server.addPlayer("Fresh");

        store.addChestData(chestDataAt(20, old.getName(), old.getUniqueId(), Material.IRON_INGOT, 1));
        assertEquals(1, store.getPlayerChestAmount(old));

        List<ChestData> replacement = new ArrayList<>();
        replacement.add(chestDataAt(21, fresh.getName(), fresh.getUniqueId(), Material.GOLD_INGOT, 3));
        replacement.add(chestDataAt(22, fresh.getName(), fresh.getUniqueId(), Material.EMERALD, 1));
        store.setChestData(replacement);

        assertEquals(2, store.getChestData().size());
        assertEquals(0, store.getPlayerChestAmount(old));
        assertEquals(2, store.getPlayerChestAmount(fresh));
    }

    @Test
    void getPlayerLinkedDataReturnsImmutableSnapshot() {
        Player player = server.addPlayer("Snap");
        ChestData chest = chestDataAt(30, player.getName(), player.getUniqueId(), Material.STONE, 1);
        store.addChestData(chest);

        Set<Location> linked = store.getPlayerLinkedData(player);
        assertNotNull(linked);
        assertEquals(1, linked.size());
        assertThrows(UnsupportedOperationException.class, () -> linked.add(new Location(world, 999, 64, 999)));

        store.setChestData(new ArrayList<>());
        assertEquals(1, linked.size(), "Snapshot should not mutate after store updates");
    }

    @Test
    void getPlayerLinkedDeadChestDataIteratesOwnedChestsOnly() {
        Player owner = server.addPlayer("Owner");
        Player other = server.addPlayer("Other");

        ChestData chestA = chestDataAt(40, owner.getName(), owner.getUniqueId(), Material.APPLE, 1);
        ChestData chestB = chestDataAt(41, owner.getName(), owner.getUniqueId(), Material.BREAD, 1);
        ChestData chestC = chestDataAt(42, other.getName(), other.getUniqueId(), Material.CARROT, 1);
        store.addChestData(chestA);
        store.addChestData(chestB);
        store.addChestData(chestC);

        List<ChestData> owned = new ArrayList<>();
        store.getPlayerLinkedDeadChestData(owner, owned::add);

        assertEquals(2, owned.size());
        assertTrue(owned.contains(chestA));
        assertTrue(owned.contains(chestB));
        assertFalse(owned.contains(chestC));
    }

    @Test
    void addListOfChestDataUpdatesExistingLocationAndReindexesOwner() {
        Player ownerA = server.addPlayer("OwnerA");
        Player ownerB = server.addPlayer("OwnerB");

        ChestData oldChest = chestDataAt(50, ownerA.getName(), ownerA.getUniqueId(), Material.IRON_INGOT, 1);
        ChestData newChestSameLoc = chestDataAt(50, ownerB.getName(), ownerB.getUniqueId(), Material.DIAMOND, 4);

        store.addChestData(oldChest);
        assertEquals(1, store.getPlayerChestAmount(ownerA));
        assertEquals(0, store.getPlayerChestAmount(ownerB));

        Set<ChestData> update = Set.of(newChestSameLoc);
        store.addListOfChestData(update);
        awaitAsyncDb();

        ChestData loaded = store.getChestData(new Location(world, 50, 64, 50));
        assertNotNull(loaded);
        assertEquals(ownerB.getUniqueId(), loaded.getPlayerUUID());
        assertEquals(Material.DIAMOND, loaded.getInventory().get(0).getType());
        assertEquals(0, store.getPlayerChestAmount(ownerA));
        assertEquals(1, store.getPlayerChestAmount(ownerB));
    }

    @Test
    void removeChestDataRemovesIndexesAndInvokesHooks() {
        Player player = server.addPlayer("Hooked");
        Location loc = new Location(world, 60, 64, 60);
        ChestData chest = mock(ChestData.class);
        when(chest.getPlayerUUID()).thenReturn(player.getUniqueId());
        when(chest.getChestLocation()).thenReturn(loc);

        store.addChestData(chest);
        assertEquals(1, store.getPlayerChestAmount(player));

        store.removeChestData(chest);

        verify(chest, times(1)).removeArmorStand();
        verify(chest, times(1)).remove();
        assertNull(store.getChestData(loc));
        assertEquals(0, store.getPlayerChestAmount(player));
    }

    @Test
    void removeChestDataListRemovesAllRequestedChests() {
        Player player = server.addPlayer("Batch");
        Location locA = new Location(world, 70, 64, 70);
        Location locB = new Location(world, 71, 64, 71);
        ChestData chestA = mock(ChestData.class);
        ChestData chestB = mock(ChestData.class);

        when(chestA.getPlayerUUID()).thenReturn(player.getUniqueId());
        when(chestA.getPlayerStringUUID()).thenReturn(player.getUniqueId().toString());
        when(chestA.getChestLocation()).thenReturn(locA);
        when(chestA.removeArmorStand()).thenReturn(true);

        when(chestB.getPlayerUUID()).thenReturn(player.getUniqueId());
        when(chestB.getPlayerStringUUID()).thenReturn(player.getUniqueId().toString());
        when(chestB.getChestLocation()).thenReturn(locB);
        when(chestB.removeArmorStand()).thenReturn(true);

        store.addChestData(chestA);
        store.addChestData(chestB);
        assertEquals(2, store.getPlayerChestAmount(player));

        store.removeChestDataList(List.of(chestA, chestB));
        awaitAsyncDb();

        assertTrue(store.isEmpty());
        assertEquals(0, store.getPlayerChestAmount(player));
        verify(chestA, times(1)).removeArmorStand();
        verify(chestB, times(1)).removeArmorStand();
    }

    @Test
    void clearChestDataClearsMemoryAndDatabase() {
        Player player = server.addPlayer("Clear");
        ChestData chest = chestDataAt(80, player.getName(), player.getUniqueId(), Material.COBBLESTONE, 5);

        store.addListOfChestData(Set.of(chest));
        awaitAsyncDb();
        assertFalse(ChestDataRepository.findAll().isEmpty());

        store.clearChestData();
        awaitAsyncDb();

        assertTrue(store.isEmpty());
        assertEquals(0, store.getPlayerChestAmount(player));
        assertTrue(ChestDataRepository.findAll().isEmpty());
    }

    @Test
    void savePersistsCurrentDatasetSynchronously() {
        Player player = server.addPlayer("Saver");
        ChestData chestA = chestDataAt(90, player.getName(), player.getUniqueId(), Material.COAL, 1);
        ChestData chestB = chestDataAt(91, player.getName(), player.getUniqueId(), Material.REDSTONE, 2);

        store.addChestData(chestA);
        store.addChestData(chestB);
        store.save();

        List<ChestData> persisted = ChestDataRepository.findAll();
        assertEquals(2, persisted.size());
        assertNotNull(findByX(persisted, 90));
        assertNotNull(findByX(persisted, 91));
    }

    @Test
    void getAllChestDataIsReadOnlyView() {
        Player player = server.addPlayer("Readonly");
        ChestData chest = chestDataAt(100, player.getName(), player.getUniqueId(), Material.SAND, 1);
        store.addChestData(chest);

        assertThrows(UnsupportedOperationException.class, () -> store.getAllChestData().clear());
    }

    private ChestData findByX(Collection<ChestData> chests, int x) {
        for (ChestData chest : chests) {
            if (chest.getChestLocation().getBlockX() == x) {
                return chest;
            }
        }
        fail("No chest at x=" + x);
        return null;
    }

    private ChestData chestDataAt(int x, String playerName, UUID playerId, Material material, int amount) {
        UUID timerId = UUID.nameUUIDFromBytes(("timer-" + x).getBytes());
        UUID ownerId = UUID.nameUUIDFromBytes(("owner-" + x).getBytes());

        Location chestLocation = new Location(world, x, 64, x, 90f, 10f);
        Location holoLocation = new Location(world, x, 65, x, 0f, 0f);

        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(material, amount));

        return new ChestData(
                inventory,
                chestLocation,
                playerName,
                playerId,
                new Date(1_700_000_000_000L + x),
                false,
                false,
                holoLocation,
                timerId,
                ownerId,
                world.getName(),
                0
        );
    }

    private void awaitAsyncDb() {
        CountDownLatch latch = new CountDownLatch(1);
        DeadChestLoader.sqlExecutor.runAsync(latch::countDown);
        try {
            assertTrue(latch.await(3, TimeUnit.SECONDS), "SQL async queue did not flush in time");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting SQL queue", e);
        }
        server.getScheduler().performTicks(1L);
    }
}
