package me.crylonz.deadchest.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.FileManager;
import me.crylonz.deadchest.Localization;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.DeadChestConfig;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DeadChest main interaction listener.
 * Covers inventory restore, drop mode, permission handling,
 * event cancellation and cleanup.
 */
class ClickListenerTest {

    private WorldMock world;
    private PlayerMock player;
    private BlockMock chestBlock;
    private ClickListener listener;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        world = new WorldMock();
        MockBukkit.getMock().addWorld(world);
        player = new PlayerMock(MockBukkit.getMock(), "Steve");
        chestBlock = world.getBlockAt(0, 64, 0);
        chestBlock.setType(Material.CHEST);
        DeadChestLoader.graveBlocks.add(Material.CHEST);


        DeadChestLoader.chestData = new ArrayList<>();
        DeadChestLoader.local = mock(Localization.class);
        DeadChestLoader.fileManager = mock(FileManager.class);
        when(DeadChestLoader.local.get("loc_prefix")).thenReturn("[DC] ");
        when(DeadChestLoader.local.get("loc_not_owner")).thenReturn("You are not the owner");
        when(DeadChestLoader.local.get("loc_noPermsToGet")).thenReturn("You cannot take this chest");

        DeadChestLoader.config = mock(DeadChestConfig.class);
        listener = new ClickListener();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testIsNearGraveChest_CancelsEvent() {
        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(chestBlock.getLocation());
        DeadChestLoader.chestData.add(cd);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK,
                new ItemStack(Material.STONE), chestBlock, BlockFace.UP);

        listener.onClick(event);
        assertTrue(event.isCancelled(), "Right click near DeadChest should cancel the event");
    }

    @Test
    void testClickOnDeadChest_NotOwner_Denied() {
        // Config requires ownership
        when(DeadChestLoader.config.getBoolean(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST)).thenReturn(true);

        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(chestBlock.getLocation());
        when(cd.getPlayerUUID()).thenReturn("other-uuid");
        DeadChestLoader.chestData.add(cd);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK,
                new ItemStack(Material.CHEST), chestBlock, BlockFace.UP);

        listener.onClick(event);

        assertTrue(event.isCancelled(), "Event should be cancelled for non-owner");
        assertEquals("[DC] You are not the owner", player.nextMessage());
    }

    @Test
    void testClickOnDeadChest_Owner_RestoreInventory() {
        // Config: owner can open, drop mode = 1 (restore)
        when(DeadChestLoader.config.getBoolean(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST)).thenReturn(true);
        when(DeadChestLoader.config.getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_GET_CHEST)).thenReturn(false);
        when(DeadChestLoader.config.getInt(ConfigKey.DROP_MODE)).thenReturn(1);

        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(chestBlock.getLocation());
        when(cd.getPlayerUUID()).thenReturn(player.getUniqueId().toString());
        when(cd.getInventory()).thenReturn(Arrays.asList(new ItemStack(Material.DIAMOND), new ItemStack(Material.APPLE)));
        when(cd.getXpStored()).thenReturn(5);
        DeadChestLoader.chestData.add(cd);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK,
                new ItemStack(Material.CHEST), chestBlock, BlockFace.UP);

        listener.onClick(event);

        PlayerInventory inv = player.getInventory();
        assertTrue(inv.contains(Material.DIAMOND), "Diamond should be restored to player inventory");
        assertTrue(inv.contains(Material.APPLE), "Apple should be restored to player inventory");
        assertEquals(Material.AIR, chestBlock.getType(), "Chest block should be removed after pickup");
    }

    @Test
    void testClickOnDeadChest_DropModeDropsItems() {
        // Config: drop mode = 2
        when(DeadChestLoader.config.getBoolean(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST)).thenReturn(false);
        when(DeadChestLoader.config.getInt(ConfigKey.DROP_MODE)).thenReturn(2);

        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(chestBlock.getLocation());
        when(cd.getInventory()).thenReturn(List.of(new ItemStack(Material.EMERALD)));
        when(cd.getXpStored()).thenReturn(10);
        DeadChestLoader.chestData.add(cd);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK,
                new ItemStack(Material.CHEST), chestBlock, BlockFace.UP);

        listener.onClick(event);

        assertEquals(Material.AIR, chestBlock.getType(), "Chest should be removed in drop mode");
        // World drops are not directly testable in MockBukkit (but you can spy WorldMock if needed)
    }

    @Test
    void testHasGetPermission_Denied() {
        when(DeadChestLoader.config.getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_GET_CHEST)).thenReturn(true);
        when(DeadChestLoader.config.getBoolean(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST)).thenReturn(false);

        ChestData cd = mock(ChestData.class);
        when(cd.getChestLocation()).thenReturn(chestBlock.getLocation());
        when(cd.getPlayerUUID()).thenReturn(player.getUniqueId().toString());
        DeadChestLoader.chestData.add(cd);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK,
                new ItemStack(Material.CHEST), chestBlock, BlockFace.UP);

        listener.onClick(event);

        assertTrue(event.isCancelled(), "Event should be cancelled when player lacks GET permission");
        assertEquals("[DC] You cannot take this chest", player.nextMessage());
    }
}
