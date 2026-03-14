package me.crylonz.deadchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DeadChestAPITest {

    private ServerMock server;
    private WorldMock world;
    private Plugin plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        plugin = MockBukkit.createMockPlugin();

        DeadChestLoader.plugin = plugin;
        DeadChestLoader.getChestDataCache().setChestData(new ArrayList<>());
        DeadChestLoader.graveBlocks.clear();
        DeadChestLoader.graveBlocks.add(Material.CHEST);
    }

    @AfterEach
    public void tearDown() {
        DeadChestLoader.getChestDataCache().setChestData(new ArrayList<>());
        MockBukkit.unmock();
    }

    @Test
    public void removeChestClassicRemovesBlockAndCacheEntry() {
        ChestData chestData = chestDataAt(80, "Steve");
        DeadChestLoader.getChestDataCache().addChestData(chestData);
        world.getBlockAt(chestData.getChestLocation()).setType(Material.CHEST);

        boolean removed = DeadChestAPI.removeChest(chestData);

        assertTrue(removed);
        assertEquals(Material.AIR, world.getBlockAt(chestData.getChestLocation()).getType());
        assertNull(DeadChestLoader.getChestDataCache().getChestData(chestData.getChestLocation()));
    }

    @Test
    public void giveBackChestClassicDropsItemsAndRemovesTrackedChest() {
        PlayerMock player = server.addPlayer("Steve");
        player.teleport(new Location(world, 0, 65, 0));

        ChestData chestData = chestDataAt(81, player.getName());
        DeadChestLoader.getChestDataCache().addChestData(chestData);
        world.getBlockAt(chestData.getChestLocation()).setType(Material.CHEST);

        boolean givenBack = DeadChestAPI.giveBackChest(player, chestData);

        assertTrue(givenBack);
        assertEquals(Material.AIR, world.getBlockAt(chestData.getChestLocation()).getType());
        assertNull(DeadChestLoader.getChestDataCache().getChestData(chestData.getChestLocation()));
        assertFalse(world.getEntities().isEmpty());
    }

    private ChestData chestDataAt(int x, String playerName) {
        UUID playerId = UUID.nameUUIDFromBytes(("player-" + x).getBytes());
        UUID timerId = UUID.nameUUIDFromBytes(("timer-" + x).getBytes());
        UUID ownerId = UUID.nameUUIDFromBytes(("owner-" + x).getBytes());

        Location chestLocation = new Location(world, x, 64, x, 0f, 0f);
        Location holoLocation = new Location(world, x, 65, x, 0f, 0f);

        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(Material.DIAMOND, 1));
        inventory.add(new ItemStack(Material.GOLD_INGOT, 2));

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
}
