package me.crylonz.deadchest.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import org.bukkit.Material;
import org.bukkit.event.block.BlockFromToEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static me.crylonz.deadchest.DeadChestLoader.graveBlocks;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlockFromToListenerTest {

    private WorldMock world;
    private BlockMock fromBlock;
    private BlockMock toBlock;
    private BlockFromToListener listener;

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

        fromBlock = world.getBlockAt(0, 64, 0);
        toBlock = world.getBlockAt(1, 64, 0);

        DeadChestLoader.chestDataList = new ArrayList<>();

        graveBlocks.clear();
        graveBlocks.add(Material.CHEST);

        listener = new BlockFromToListener();
    }

    @Test
    void testEventCancelledWhenToBlockIsGraveBlockAndMatchesChestData() {
        toBlock.setType(Material.CHEST);

        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(toBlock.getLocation());

        DeadChestLoader.chestDataList.add(cd);

        BlockFromToEvent event = new BlockFromToEvent(fromBlock, toBlock);
        listener.onBlockFromToEvent(event);

        assertTrue(event.isCancelled(), "Event should be cancelled when toBlock is a grave block and matches chest location");
    }

    @Test
    void testEventNotCancelledWhenToBlockIsNotGraveBlock() {
        toBlock.setType(Material.DIRT);

        BlockFromToEvent event = new BlockFromToEvent(fromBlock, toBlock);
        listener.onBlockFromToEvent(event);

        assertFalse(event.isCancelled(), "Event should not be cancelled when toBlock is not a grave block");
    }

    @Test
    void testEventNotCancelledWhenLocationDoesNotMatchChestData() {
        toBlock.setType(Material.CHEST);

        BlockMock otherBlock = world.getBlockAt(5, 64, 5);
        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(otherBlock.getLocation());
        DeadChestLoader.chestDataList.add(cd);

        BlockFromToEvent event = new BlockFromToEvent(fromBlock, toBlock);
        listener.onBlockFromToEvent(event);

        assertFalse(event.isCancelled(),
                "Event should not be cancelled when chestData location does not match the toBlock location");
    }
}

