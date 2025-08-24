package me.crylonz.deadchest.listener;


import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.Localization;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.bukkit.inventory.EquipmentSlot.HAND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlockPlaceEventListenerTest {

    private WorldMock world;
    private PlayerMock player;
    private BlockPlaceEventListener listener;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        world = new WorldMock();
        MockBukkit.getMock().addWorld(world);
        player = new PlayerMock(MockBukkit.getMock(), "Steve");

        DeadChestLoader.chestDataList = new ArrayList<>();
        DeadChestLoader.local = mock(Localization.class);
        when(DeadChestLoader.local.get("loc_prefix")).thenReturn("[DC] ");
        when(DeadChestLoader.local.get("loc_doubleDC")).thenReturn("You can't place a double DeadChest!");

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
        DeadChestLoader.chestDataList.add(cd);

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
        assertTrue(player.nextMessage().contains("double DeadChest"),
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