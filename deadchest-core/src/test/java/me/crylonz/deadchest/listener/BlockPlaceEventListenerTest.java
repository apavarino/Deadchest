package me.crylonz.deadchest.listener;


import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.Localization;
import me.crylonz.deadchest.db.InMemoryChestStore;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.inventory.EquipmentSlot.HAND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlockPlaceEventListenerTest {

    private WorldMock world;
    private PlayerMock player;
    private BlockPlaceEventListener listener;
    private InMemoryChestStore chestData;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        world = new WorldMock();
        MockBukkit.getMock().addWorld(world);
        player = new PlayerMock(MockBukkit.getMock(), "Steve");

        chestData = DeadChestLoader.getChestDataCache();
        chestData.setChestData( new ArrayList<>());

        Localization localization = new Localization();
        Map<String, Object> values = new HashMap<>();
        values.put("common.prefix", "[DeadChest] ");
        values.put("chest.double-block", "You can't put a chest next to a Deadchest");
        localization.set(values);
        DeadChestLoader.local = localization;

        listener = new BlockPlaceEventListener();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testEventCancelledWhenPlacingNextToDeadChest() {
        BlockMock existingBlock = world.getBlockAt(0, 64, 0);
        existingBlock.setType(Material.CHEST);

        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(existingBlock.getLocation());
        chestData.addChestData(cd);

        BlockMock newBlock = world.getBlockAt(1, 64, 0);
        newBlock.setType(Material.CHEST);

        BlockPlaceEvent event = new BlockPlaceEvent(
                newBlock,
                newBlock.getState(),
                existingBlock, // the block against which the player is placing
                player.getItemInHand(),
                player,
                true,
                HAND
        );

        listener.onBlockPlaceEvent(event);

        assertTrue(event.isCancelled(), "Event should be cancelled when placing a chest next to a DeadChest");
        assertTrue(player.nextMessage().contains("next to a Deadchest"),
                "Player should be informed about double chest restriction");
    }

    @Test
    void testEventNotCancelledWhenNoAdjacentDeadChest() {
        BlockMock existingBlock = world.getBlockAt(0, 64, 0);
        existingBlock.setType(Material.STONE); // not a chest

        BlockMock newBlock = world.getBlockAt(1, 64, 0);
        newBlock.setType(Material.AIR);

        BlockPlaceEvent event = new BlockPlaceEvent(
                newBlock,
                newBlock.getState(),
                existingBlock,
                player.getItemInHand(),
                player,
                true,
                HAND
        );

        listener.onBlockPlaceEvent(event);

        assertFalse(event.isCancelled(), "Event should not be cancelled when no DeadChest is adjacent");
        assertNull(player.nextMessage(), "Player should not receive a message if no DeadChest is adjacent");
    }
}
