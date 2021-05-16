package me.crylonz;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import junit.framework.TestCase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChestDataTest extends TestCase {

    private ServerMock server;
    private DeadChest plugin;
    private World world;

    @Before
    public void setUp() {
        DeadChest.bstats = false;
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DeadChest.class);
        world = new WorldMock(Material.DIRT, 3);
    }

    @After
    public void tearDown() {
        MockBukkit.unmock();
    }

    public void testDeserialize() throws ParseException {
        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(Material.BOW, 1));
        inventory.add(new ItemStack(Material.DIRT, 34));
        inventory.add(new ItemStack(Material.DARK_OAK_DOOR, 3));

        Map<String, Object> map = new HashMap<>();
        map.put("chestLocation", "world;145.0;66.0;242.0");
        map.put("holographicTimer", "world;145.5;66.79999995231628;242.5");
        map.put("playerName", "E3GE");
        map.put("isInfinity", false);
        map.put("worldName", "world");
        map.put("as_timer_id", "6243ee63-64b7-46a2-823c-29963bdd682d");
        map.put("as_owner_id", "8642d1ca-15a5-4745-9f1c-f8bbbad48654");
        map.put("playerUUID", "3bcfd9d6-31fd-3ef1-9ed3-4c0f0b5d89e4");
        map.put("chestDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .parse("2020-10-18T12:29:35.599Z"));
        map.put("inventory", inventory);

        ChestData chestData = ChestData.deserialize(map);

        assertEquals("E3GE", chestData.getPlayerName());
        assertEquals("world", chestData.getWorldName());
        assertEquals("3bcfd9d6-31fd-3ef1-9ed3-4c0f0b5d89e4", chestData.getPlayerUUID());
        assertEquals(UUID.fromString("8642d1ca-15a5-4745-9f1c-f8bbbad48654"), chestData.getHolographicOwnerId());
        assertEquals(UUID.fromString("6243ee63-64b7-46a2-823c-29963bdd682d"), chestData.getHolographicTimerId());
        assertFalse(chestData.isInfinity());
        assertFalse(chestData.getInventory().isEmpty());
        assertEquals(3, chestData.getInventory().size());
        assertEquals(34, chestData.getInventory().get(1).getAmount());
        assertEquals(145.0, chestData.getChestLocation().getX());
        assertEquals(66.0, chestData.getChestLocation().getY());
        assertEquals(242.0, chestData.getChestLocation().getZ());
    }

    public void testSerialize() {
        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(Material.BOW, 1));
        inventory.add(new ItemStack(Material.DIRT, 34));
        inventory.add(new ItemStack(Material.DARK_OAK_DOOR, 3));
        Date date = new Date();
        Location chestLocation = new Location(world, 0, 0, 0);

        ChestData chestData = new ChestData(
                inventory,
                chestLocation,
                "E3GE",
                "3bcfd9d6-31fd-3ef1-9ed3-4c0f0b5d89e4",
                date,
                false,
                new Location(world, 0, 1, 0),
                UUID.fromString("6243ee63-64b7-46a2-823c-29963bdd682d"),
                UUID.fromString("8642d1ca-15a5-4745-9f1c-f8bbbad48654"),
                "world"
        );

        Map<String, Object> serializedObject = chestData.serialize();

        assertEquals(10, serializedObject.size());
        assertEquals(inventory.size(), ((List<ItemStack>) serializedObject.get("inventory")).size());
        assertEquals(inventory, serializedObject.get("inventory"));
        assertEquals(ChestData.serializeLocation("world", chestLocation), serializedObject.get("chestLocation"));
        assertEquals("E3GE", serializedObject.get("playerName"));
        assertEquals("3bcfd9d6-31fd-3ef1-9ed3-4c0f0b5d89e4", serializedObject.get("playerUUID"));
        assertFalse((Boolean) serializedObject.get("isInfinity"));
        assertEquals("world", serializedObject.get("worldName"));
        assertEquals(date, serializedObject.get("chestDate"));
    }
}