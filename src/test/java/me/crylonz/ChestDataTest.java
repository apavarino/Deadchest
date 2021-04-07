package me.crylonz;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import junit.framework.TestCase;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestDataTest extends TestCase {

    private ServerMock server;
    private DeadChest plugin;

    @Before
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DeadChest.class);
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
        map.put("worldName", "World");
        map.put("as_timer_id", "6243ee63-64b7-46a2-823c-29963bdd682d");
        map.put("as_owner_id", "8642d1ca-15a5-4745-9f1c-f8bbbad48654");
        map.put("playerUUID", "3bcfd9d6-31fd-3ef1-9ed3-4c0f0b5d89e4");
        map.put("chestDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .parse("2020-10-18T12:29:35.599Z"));
        map.put("inventory", inventory);

        ChestData chestData = ChestData.deserialize(map);

        assertEquals("E3GE", chestData.getPlayerName());
        assertEquals("3bcfd9d6-31fd-3ef1-9ed3-4c0f0b5d89e4", chestData.getPlayerUUID());
        assertFalse(chestData.isInfinity());
        assertFalse(chestData.getInventory().isEmpty());
        assertEquals(3, chestData.getInventory().size());
        assertEquals(34, chestData.getInventory().get(1).getAmount());
        assertEquals(145.0, chestData.getChestLocation().getX());
        assertEquals(66.0, chestData.getChestLocation().getY());
        assertEquals(242.0, chestData.getChestLocation().getZ());
    }

    public void testRemoveArmorStand() {
        //TODO
    }

    public void testSerialize() {
        //TODO
    }
}