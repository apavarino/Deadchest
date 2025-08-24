package me.crylonz.deadchest.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import me.crylonz.deadchest.ChestData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static me.crylonz.deadchest.DeadChestLoader.chestDataList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PistonListenerTest {

    private ServerMock server;
    private PistonListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        listener = new PistonListener();
        chestDataList = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testPistonCancelledWhenPushingHeadWithChestData() {
        Block piston = new BlockMock(Material.PISTON);

        World world = server.addSimpleWorld("world");
        Location loc = new Location(world, 10, 64, 10);

        BlockMock headBlock = new BlockMock(Material.PLAYER_HEAD, loc);

        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(loc);
        chestDataList.add(cd);

        List<Block> moved = new ArrayList<>();
        moved.add(headBlock);

        BlockPistonExtendEvent event = new BlockPistonExtendEvent(
                piston, moved, BlockFace.NORTH
        );

        listener.onBlockPistonExtendEvent(event);

        assertTrue(event.isCancelled(), "L'événement doit être annulé si le piston pousse une tombe");
    }

    @Test
    void testPistonNotCancelledWhenPushingOtherBlock() {
        Block piston = new BlockMock(Material.PISTON);
        Block dirt = new BlockMock(Material.DIRT);

        List<Block> moved = new ArrayList<>();
        moved.add(dirt);

        BlockPistonExtendEvent event = new BlockPistonExtendEvent(
                piston, moved, BlockFace.NORTH
        );

        listener.onBlockPistonExtendEvent(event);

        assertFalse(event.isCancelled(), "L'événement ne doit pas être annulé si le piston pousse un bloc normal");
    }
}
