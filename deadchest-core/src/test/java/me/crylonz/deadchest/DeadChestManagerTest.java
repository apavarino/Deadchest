package me.crylonz.deadchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.crylonz.deadchest.db.ChestDataRepository;
import me.crylonz.deadchest.db.IgnoreItemListRepository;
import me.crylonz.deadchest.db.SQLExecutor;
import me.crylonz.deadchest.db.SQLite;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.DeadChestConfig;
import me.crylonz.deadchest.utils.ExpiredActionType;
import me.crylonz.deadchest.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeadChestManagerTest {

    private ServerMock server;
    private WorldMock world;
    private Plugin plugin;
    private DeadChestConfig config;

    @BeforeEach
    void setUp() throws Exception {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        plugin = MockBukkit.createMockPlugin();

        DeadChestLoader.plugin = plugin;
        DeadChestLoader.log = Logger.getLogger("DeadChestManagerTest");
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

        DeadChestLoader.getChestDataCache().setChestData(new ArrayList<>());
        DeadChestLoader.graveBlocks.clear();
        DeadChestLoader.graveBlocks.add(Material.CHEST);

        config = mock(DeadChestConfig.class);
        when(config.getInt(any(ConfigKey.class))).thenReturn(300);
        when(config.getBoolean(any(ConfigKey.class))).thenReturn(false);
        DeadChestLoader.config = config;
        DeadChestLoader.local = new Localization();
    }

    @AfterEach
    void tearDown() {
        DeadChestLoader.getChestDataCache().setChestData(new ArrayList<>());
        DeadChestLoader.sqlExecutor.shutdown();
        DeadChestLoader.db.close();
        DeadChestLoader.sqlExecutor = new SQLExecutor();
        MockBukkit.unmock();
    }

    @Test
    void playerDeadChestAmountReturnsZeroForNullPlayer() {
        assertEquals(0, DeadChestManager.playerDeadChestAmount(null));
    }

    @Test
    void playerDeadChestAmountReturnsPlayerCount() {
        PlayerMock steve = server.addPlayer("Steve");
        PlayerMock alex = server.addPlayer("Alex");

        DeadChestLoader.getChestDataCache().addChestData(chestDataAt(10, steve.getName(), steve.getUniqueId(), false));
        DeadChestLoader.getChestDataCache().addChestData(chestDataAt(11, steve.getName(), steve.getUniqueId(), false));
        DeadChestLoader.getChestDataCache().addChestData(chestDataAt(12, alex.getName(), alex.getUniqueId(), false));

        assertEquals(2, DeadChestManager.playerDeadChestAmount(steve));
        assertEquals(1, DeadChestManager.playerDeadChestAmount(alex));
    }

    @Test
    void cleanAllDeadChestsRemovesAllActiveChests() {
        try {
            PlayerMock steve = server.addPlayer("Steve");
            Location locA = new Location(world, 20, 64, 20);
            Location locB = new Location(world, 21, 64, 21);
            ChestData chestA = mock(ChestData.class);
            ChestData chestB = mock(ChestData.class);
            when(chestA.getPlayerUUID()).thenReturn(steve.getUniqueId());
            when(chestB.getPlayerUUID()).thenReturn(steve.getUniqueId());
            when(chestA.getPlayerStringUUID()).thenReturn(steve.getUniqueId().toString());
            when(chestB.getPlayerStringUUID()).thenReturn(steve.getUniqueId().toString());
            when(chestA.getChestLocation()).thenReturn(locA);
            when(chestB.getChestLocation()).thenReturn(locB);
            when(chestA.removeArmorStand()).thenReturn(true);
            when(chestB.removeArmorStand()).thenReturn(true);
            DeadChestLoader.getChestDataCache().addChestData(chestA);
            DeadChestLoader.getChestDataCache().addChestData(chestB);
            world.getBlockAt(locA).setType(Material.CHEST);
            world.getBlockAt(locB).setType(Material.CHEST);

            int removed = DeadChestManager.cleanAllDeadChests();
            awaitAsyncDb();

            assertEquals(2, removed);
            assertTrue(DeadChestLoader.getChestDataCache().isEmpty());
            assertEquals(Material.AIR, world.getBlockAt(locA).getType());
            assertEquals(Material.AIR, world.getBlockAt(locB).getType());
        } catch (Throwable t) {
            fail("Unexpected abort/failure in cleanAllDeadChests test", t);
        }
    }

    @Test
    void handleExpirateDeadChestNotExpiredReturnsNotExpired() {
        ChestData chest = mock(ChestData.class);
        Date now = new Date(2_000_000L);
        when(chest.getChestDate()).thenReturn(new Date(now.getTime() - 10_000));
        when(chest.isInfinity()).thenReturn(false);

        ExpiredActionType result = DeadChestManager.handleExpirateDeadChest(chest, now);

        assertEquals(ExpiredActionType.NOT_EXPIRED, result);
        verify(chest, never()).removeArmorStand();
    }

    @Test
    void handleExpirateDeadChestExpiredAndArmorStandRemoved() {
        World realWorld = world;
        Location loc = new Location(realWorld, 30, 64, 30);
        realWorld.getBlockAt(loc).setType(Material.CHEST);

        ChestData chest = mock(ChestData.class);
        Date now = new Date(2_000_000L);
        when(config.getInt(ConfigKey.DEADCHEST_DURATION)).thenReturn(1);
        when(config.getBoolean(ConfigKey.ITEMS_DROPPED_AFTER_TIMEOUT)).thenReturn(false);
        when(chest.getChestDate()).thenReturn(new Date(now.getTime() - 10_000));
        when(chest.isInfinity()).thenReturn(false);
        when(chest.isRemovedBlock()).thenReturn(false);
        when(chest.getChestLocation()).thenReturn(loc);
        when(chest.removeArmorStand()).thenReturn(true);

        ExpiredActionType result = DeadChestManager.handleExpirateDeadChest(chest, now);

        assertEquals(ExpiredActionType.REMOVED_ARMORSTAND, result);
        assertEquals(Material.AIR, realWorld.getBlockAt(loc).getType());
        verify(chest, times(1)).setRemovedBlock(true);
    }

    @Test
    void handleExpirateDeadChestExpiredAndArmorStandNotRemoved() {
        Location loc = new Location(world, 31, 64, 31);
        world.getBlockAt(loc).setType(Material.CHEST);

        ChestData chest = mock(ChestData.class);
        Date now = new Date(2_000_000L);
        when(config.getInt(ConfigKey.DEADCHEST_DURATION)).thenReturn(1);
        when(config.getBoolean(ConfigKey.ITEMS_DROPPED_AFTER_TIMEOUT)).thenReturn(false);
        when(chest.getChestDate()).thenReturn(new Date(now.getTime() - 10_000));
        when(chest.isInfinity()).thenReturn(false);
        when(chest.isRemovedBlock()).thenReturn(false);
        when(chest.getChestLocation()).thenReturn(loc);
        when(chest.removeArmorStand()).thenReturn(false);

        ExpiredActionType result = DeadChestManager.handleExpirateDeadChest(chest, now);

        assertEquals(ExpiredActionType.FAIL_REMOVE_ARMORSTAND, result);
        assertEquals(Material.AIR, world.getBlockAt(loc).getType());
    }

    @Test
    void generateHologramReturnsNullWhenWorldMissing() {
        assertNull(DeadChestManager.generateHologram(new Location(null, 0, 64, 0), "txt", 0, 0, 0, true));
    }

    @Test
    void generateHologramConfiguresArmorStand() {
        World mockedWorld = mock(World.class);
        ArmorStand armorStand = mock(ArmorStand.class);
        Location loc = new Location(mockedWorld, 5, 64, 5);
        when(mockedWorld.spawnEntity(any(Location.class), eq(EntityType.ARMOR_STAND))).thenReturn(armorStand);

        ArmorStand result = DeadChestManager.generateHologram(loc, "hello", 0.5f, -1f, 0.5f, true);

        assertSame(armorStand, result);
        verify(armorStand).setInvulnerable(true);
        verify(armorStand).setSmall(true);
        verify(armorStand).setGravity(false);
        verify(armorStand).setCanPickupItems(false);
        verify(armorStand).setVisible(false);
        verify(armorStand).setCollidable(false);
        verify(armorStand).setCustomName("hello");
        verify(armorStand).setSilent(true);
        verify(armorStand).setMarker(true);
        verify(armorStand).setCustomNameVisible(true);
    }

    @Test
    void reloadMetaDataReappliesMetadataForLinkedArmorStands() {
        World mockedWorld = mock(World.class);
        UUID ownerId = UUID.randomUUID();
        UUID timerId = UUID.randomUUID();

        Entity owner = mock(Entity.class);
        Entity timer = mock(Entity.class);
        when(owner.getUniqueId()).thenReturn(ownerId);
        when(timer.getUniqueId()).thenReturn(timerId);

        ChestData chestData = mock(ChestData.class);
        Location chestLoc = new Location(mockedWorld, 40, 64, 40);
        Location holoLoc = new Location(mockedWorld, 40, 65, 40);
        when(chestData.getChestLocation()).thenReturn(chestLoc);
        when(chestData.getHolographicTimer()).thenReturn(holoLoc);
        when(chestData.getHolographicOwnerId()).thenReturn(ownerId);
        when(chestData.getHolographicTimerId()).thenReturn(timerId);
        Collection<Entity> nearbyEntities = new ArrayList<>(List.of(owner, timer));

        DeadChestManager.reloadMetaData(chestData, nearbyEntities);

        verify(owner).setMetadata(eq("deadchest"), any(FixedMetadataValue.class));
        verify(timer).setMetadata(eq("deadchest"), any(FixedMetadataValue.class));
    }

    @Test
    void updateTimerUsesChestWorldWhenHologramWorldIsInconsistent() {
        World chestWorld = mock(World.class);
        World hologramWorld = mock(World.class);
        Entity timerStand = mock(Entity.class);
        Location chestLoc = new Location(chestWorld, 10, 64, -25);
        Location holoLoc = new Location(hologramWorld, 11, 46.8, -25);

        ChestData chestData = mock(ChestData.class);
        when(chestData.getChestLocation()).thenReturn(chestLoc);
        when(chestData.getHolographicTimer()).thenReturn(holoLoc);
        when(chestData.isChunkLoaded()).thenReturn(true);
        when(chestData.getChestDate()).thenReturn(new Date(System.currentTimeMillis() - 1_000));
        when(chestData.isInfinity()).thenReturn(false);
        when(chestData.getHolographicTimerId()).thenReturn(UUID.randomUUID());
        when(chestData.getHolographicOwnerId()).thenReturn(UUID.randomUUID());
        when(timerStand.getType()).thenReturn(EntityType.ARMOR_STAND);
        when(timerStand.hasMetadata("deadchest")).thenReturn(true);
        when(timerStand.getMetadata("deadchest")).thenReturn(List.of(new FixedMetadataValue(plugin, true)));
        when(config.getInt(ConfigKey.DEADCHEST_DURATION)).thenReturn(300);
        when(chestWorld.getNearbyEntities(any(Location.class), eq(1.0), eq(1.0), eq(1.0)))
                .thenReturn(new ArrayList<>(List.of(timerStand)));

        DeadChestManager.updateTimer(chestData, new Date());

        verify(chestWorld).getNearbyEntities(argThat(location -> location != null && location.getWorld() == chestWorld), eq(1.0), eq(1.0), eq(1.0));
        verify(hologramWorld, never()).getNearbyEntities(any(Location.class), anyDouble(), anyDouble(), anyDouble());
        verify(timerStand).setCustomName(anyString());
    }

    @Test
    void replaceDeadChestIfItDisappearsReturnsFalseWhenWorldIsNull() {
        ChestData chestData = mock(ChestData.class);
        when(chestData.getChestLocation()).thenReturn(new Location(null, 1, 64, 1));

        assertFalse(DeadChestManager.replaceDeadChestIfItDisappears(chestData));
    }

    @Test
    void replaceDeadChestIfItDisappearsReturnsTrueWhenBlockIsCorrupted() {
        World mockedWorld = mock(World.class);
        Block block = mock(Block.class);
        Entity linked = mock(Entity.class);
        UUID ownerId = UUID.randomUUID();

        Location chestLoc = new Location(mockedWorld, 50, 64, 50);
        Location holoLoc = new Location(mockedWorld, 50, 65, 50);

        ChestData chestData = mock(ChestData.class);
        when(chestData.getChestLocation()).thenReturn(chestLoc);
        when(chestData.getHolographicTimer()).thenReturn(holoLoc);
        when(chestData.getHolographicOwnerId()).thenReturn(ownerId);
        when(chestData.getHolographicTimerId()).thenReturn(UUID.randomUUID());
        when(chestData.getPlayerUUID()).thenReturn(UUID.randomUUID());
        when(chestData.getPlayerName()).thenReturn("Steve");
        when(linked.getUniqueId()).thenReturn(ownerId);
        when(mockedWorld.getNearbyEntities(holoLoc, 1.0, 1.0, 1.0)).thenReturn(List.of(linked));
        when(mockedWorld.getBlockAt(chestLoc)).thenReturn(block);
        when(block.getType()).thenReturn(Material.STONE);

        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {
            boolean changed = DeadChestManager.replaceDeadChestIfItDisappears(chestData);
            assertTrue(changed);
            utilsMock.verify(() -> Utils.generateDeadChest(block, null), times(1));
        }
    }

    @Test
    void updateTimerUpdatesArmorStandNameWhenActive() {
        World mockedWorld = mock(World.class);
        Entity timerStand = mock(Entity.class);
        Location holoLoc = new Location(mockedWorld, 60, 65, 60);

        ChestData chestData = mock(ChestData.class);
        when(chestData.getHolographicTimer()).thenReturn(holoLoc);
        when(chestData.isChunkLoaded()).thenReturn(true);
        when(chestData.getChestDate()).thenReturn(new Date(System.currentTimeMillis() - 1_000));
        when(chestData.isInfinity()).thenReturn(false);
        when(timerStand.getType()).thenReturn(EntityType.ARMOR_STAND);
        when(timerStand.hasMetadata("deadchest")).thenReturn(true);
        when(timerStand.getMetadata("deadchest")).thenReturn(List.of(new FixedMetadataValue(plugin, true)));
        when(mockedWorld.getNearbyEntities(holoLoc, 1.0, 1.0, 1.0)).thenReturn(new ArrayList<>(List.of(timerStand)));
        when(config.getInt(ConfigKey.DEADCHEST_DURATION)).thenReturn(300);

        DeadChestManager.updateTimer(chestData, new Date());

        verify(timerStand, times(1)).setCustomName(anyString());
    }

    @Test
    void updateTimerShowsInfinityLabelForInfiniteChest() {
        World mockedWorld = mock(World.class);
        Entity timerStand = mock(Entity.class);
        Location holoLoc = new Location(mockedWorld, 61, 65, 61);

        ChestData chestData = mock(ChestData.class);
        when(chestData.getHolographicTimer()).thenReturn(holoLoc);
        when(chestData.isChunkLoaded()).thenReturn(true);
        when(chestData.getChestDate()).thenReturn(new Date(System.currentTimeMillis() - 1_000));
        when(chestData.isInfinity()).thenReturn(true);
        when(timerStand.getType()).thenReturn(EntityType.ARMOR_STAND);
        when(timerStand.hasMetadata("deadchest")).thenReturn(true);
        when(timerStand.getMetadata("deadchest")).thenReturn(List.of(new FixedMetadataValue(plugin, true)));
        when(mockedWorld.getNearbyEntities(holoLoc, 1.0, 1.0, 1.0)).thenReturn(new ArrayList<>(List.of(timerStand)));
        when(config.getInt(ConfigKey.DEADCHEST_DURATION)).thenReturn(300);

        DeadChestManager.updateTimer(chestData, new Date());

        verify(timerStand).setCustomName(DeadChestLoader.local.get("chest.infinity"));
    }

    @Test
    void handleChestTickKeepsChestTrackedWhenArmorStandRemovalFails() {
        Location loc = new Location(world, 62, 64, 62);
        world.getBlockAt(loc).setType(Material.CHEST);

        ChestData chestData = mock(ChestData.class);
        when(config.getInt(ConfigKey.DEADCHEST_DURATION)).thenReturn(1);
        when(config.getBoolean(ConfigKey.ITEMS_DROPPED_AFTER_TIMEOUT)).thenReturn(false);
        when(chestData.getChestLocation()).thenReturn(loc);
        when(chestData.getHolographicTimer()).thenReturn(new Location(world, 62, 65, 62));
        when(chestData.getChestDate()).thenReturn(new Date(0L));
        when(chestData.isInfinity()).thenReturn(false);
        when(chestData.isRemovedBlock()).thenReturn(false);
        when(chestData.isChunkLoaded()).thenReturn(false);
        when(chestData.removeArmorStand()).thenReturn(false);
        when(chestData.getPlayerName()).thenReturn("Steve");
        when(chestData.getPlayerUUID()).thenReturn(UUID.randomUUID());

        DeadChestLoader.getChestDataCache().addChestData(chestData);

        DeadChestManager.handleChestTick(chestData, new Date(10_000L));

        assertFalse(DeadChestLoader.getChestDataCache().isEmpty());
        verify(chestData, times(1)).update(any());
        verify(chestData, never()).remove();
    }

    private ChestData chestDataAt(int x, String playerName, UUID playerId, boolean infinity) {
        UUID timerId = UUID.nameUUIDFromBytes(("timer-" + x).getBytes());
        UUID ownerId = UUID.nameUUIDFromBytes(("owner-" + x).getBytes());

        Location chestLocation = new Location(world, x, 64, x, 90f, 10f);
        Location holoLocation = new Location(world, x, 65, x, 0f, 0f);

        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(Material.DIAMOND, 1));

        return new ChestData(
                inventory,
                chestLocation,
                playerName,
                playerId,
                new Date(1_700_000_000_000L + x),
                infinity,
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
