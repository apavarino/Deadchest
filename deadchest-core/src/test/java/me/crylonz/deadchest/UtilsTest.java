package me.crylonz.deadchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.inventory.PlayerInventoryMock;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.*;

public class UtilsTest {
    private static ServerMock server;

    @BeforeAll
    public static void setup() {
        DeadChest.bstats = false;

        server = MockBukkit.mock();
    }

    @AfterAll
    public static void cleanup() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("isInventoryEmpty should return true on empty inventories")
    public void isInventoryEmpty_emptyInv() {
        Inventory i = new PlayerInventoryMock(server.addPlayer());

        Assertions.assertTrue(Utils.isInventoryEmpty(i));
    }
}
