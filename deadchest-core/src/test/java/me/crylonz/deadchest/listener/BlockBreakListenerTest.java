package me.crylonz.deadchest.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.Localization;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.DeadChestConfig;
import me.crylonz.deadchest.utils.Utils;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BlockBreakListenerTest {

    private PlayerMock player;
    private WorldMock world;
    private BlockMock block;
    private BlockBreakListener listener;

    private static List<ChestData> chestDataBackup;

    @BeforeAll
    static void beforeAll() {
        MockBukkit.mock();
    }

    @AfterAll
    static void afterAll() {
        MockBukkit.unmock();
    }

    @BeforeEach
    void setUp() {
        world = new WorldMock();
        player = new PlayerMock(MockBukkit.getMock(), "TestPlayer");
        block = world.getBlockAt(0, 64, 0);
        listener = new BlockBreakListener();

        if (DeadChestLoader.chestDataList == null) {
            DeadChestLoader.chestDataList = new ArrayList<>();
        }
    }

    @AfterEach
    void tearDown() {
        DeadChestLoader.chestDataList.clear();
    }

    @Test
    void testEventCancelledWhenIndestructibleChestAndMatchingLocation() {
        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {
            utilsMock.when(() -> Utils.isGraveBlock(Material.CHEST)).thenReturn(true);

            DeadChestLoader.config = mock(DeadChestConfig.class);
            when(DeadChestLoader.config.getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)).thenReturn(true);

            DeadChestLoader.local = mock(Localization.class);
            when(DeadChestLoader.local.get("loc_prefix")).thenReturn("[DeadChest] ");
            when(DeadChestLoader.local.get("loc_not_owner")).thenReturn("Not your chest");

            ChestData cd = mock(ChestData.class);
            when(cd.getChestLocation()).thenReturn(block.getLocation());
            DeadChestLoader.chestDataList.clear();
            DeadChestLoader.chestDataList.add(cd);

            block.setType(Material.CHEST);

            BlockBreakEvent event = new BlockBreakEvent(block, player);
            listener.onBlockBreakEvent(event);

            assertTrue(event.isCancelled(), "Event should be cancelled for indestructible chest");
            assertTrue(player.nextMessage().contains("Not your chest"));
        }
    }

    @Test
    void testEventNotCancelledWhenNotGraveBlock() {
        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {
            utilsMock.when(() -> Utils.isGraveBlock(Material.DIRT)).thenReturn(false);

            block.setType(Material.DIRT);

            BlockBreakEvent event = new BlockBreakEvent(block, player);
            listener.onBlockBreakEvent(event);

            assertFalse(event.isCancelled(), "Event should not be cancelled if not a grave block");
        }
    }
}
