package me.crylonz.deadchest.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.DeadChestConfig;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ExplosionListenerTest {

    private WorldMock world;
    private ExplosionListener listener;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        world = new WorldMock();
        MockBukkit.getMock().addWorld(world);

        DeadChestLoader.chestData = new ArrayList<>();
        DeadChestLoader.config = mock(DeadChestConfig.class);
        DeadChestLoader.graveBlocks.add(Material.CHEST);


        listener = new ExplosionListener();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ChestData createMockChestAt(BlockMock block) {
        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(block.getLocation());
        when(cd.getPlayerName()).thenReturn("Steve");
        DeadChestLoader.chestData.add(cd);
        return cd;
    }

    @Test
    void testEntityExplosion_IndestructibleChest() {
        BlockMock chestBlock = world.getBlockAt(0, 64, 0);
        chestBlock.setType(Material.CHEST);

        ChestData cd = createMockChestAt(chestBlock);

        when(DeadChestLoader.config.getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)).thenReturn(true);

        Creeper creeper = (Creeper) world.spawnEntity(chestBlock.getLocation(), EntityType.CREEPER);
        List<org.bukkit.block.Block> explodedBlocks = new ArrayList<>();
        explodedBlocks.add(chestBlock);

        EntityExplodeEvent event = new EntityExplodeEvent(creeper, chestBlock.getLocation(), explodedBlocks, 1.0f);
        listener.onEntityExplodeEvent(event);

        assertFalse(event.blockList().contains(chestBlock));
        assertTrue(DeadChestLoader.chestData.contains(cd));
    }

    @Test
    void testEntityExplosion_DestructibleChest() {
        BlockMock chestBlock = world.getBlockAt(0, 64, 0);
        chestBlock.setType(Material.CHEST);

        ChestData cd = createMockChestAt(chestBlock);

        when(DeadChestLoader.config.getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)).thenReturn(false);

        Creeper creeper = (Creeper) world.spawnEntity(chestBlock.getLocation(), EntityType.CREEPER);
        List<org.bukkit.block.Block> explodedBlocks = new ArrayList<>();
        explodedBlocks.add(chestBlock);

        EntityExplodeEvent event = new EntityExplodeEvent(creeper, chestBlock.getLocation(), explodedBlocks, 1.0f);
        listener.onEntityExplodeEvent(event);

        assertFalse(DeadChestLoader.chestData.contains(cd), "ChestData should be removed after explosion when destructible");
        verify(cd, times(1)).removeArmorStand();
    }

    @Test
    void testBlockExplosion_IndestructibleChest() {
        BlockMock chestBlock = world.getBlockAt(1, 64, 1);
        chestBlock.setType(Material.CHEST);

        ChestData cd = createMockChestAt(chestBlock);

        when(DeadChestLoader.config.getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)).thenReturn(true);

        List<org.bukkit.block.Block> explodedBlocks = new ArrayList<>();
        explodedBlocks.add(chestBlock);

        BlockExplodeEvent event = new BlockExplodeEvent(chestBlock, explodedBlocks, 1.0f);
        listener.onBlockExplodeEvent(event);


        assertFalse(event.blockList().contains(chestBlock));
        assertTrue(DeadChestLoader.chestData.contains(cd));

    }

    @Test
    void testBlockExplosion_DestructibleChest() {
        BlockMock chestBlock = world.getBlockAt(2, 64, 2);
        chestBlock.setType(Material.CHEST);

        ChestData cd = createMockChestAt(chestBlock);

        when(DeadChestLoader.config.getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)).thenReturn(false);

        List<org.bukkit.block.Block> explodedBlocks = new ArrayList<>();
        explodedBlocks.add(chestBlock);

        BlockExplodeEvent event = new BlockExplodeEvent(chestBlock, explodedBlocks, 1.0f);
        listener.onBlockExplodeEvent(event);

        assertFalse(DeadChestLoader.chestData.contains(cd), "ChestData should be removed after block explosion when destructible");
        verify(cd, times(1)).removeArmorStand();
    }
}
