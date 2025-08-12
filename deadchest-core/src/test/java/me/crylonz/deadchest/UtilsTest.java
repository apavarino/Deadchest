package me.crylonz.deadchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import be.seeseemelk.mockbukkit.inventory.PlayerInventoryMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;

public class UtilsTest {
    private static ServerMock server;
    private static World world;

    @BeforeAll
    public static void setup() {
        DeadChestLoader.bstats = false;

        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
    }

    @AfterAll
    public static void cleanup() {
        MockBukkit.unmock();
    }

    // isInventoryEmpty

    @Test
    @DisplayName("[isInventoryEmpty] - it should return true on empty inventories")
    public void isInventoryEmpty_emptyInv() {
        Inventory i = new PlayerInventoryMock(server.addPlayer());

        Assertions.assertTrue(Utils.isInventoryEmpty(i));
    }

    @Test
    @DisplayName("[isInventoryEmpty] - it should return false on inventories containing items")
    public void isInventoryEmpty_filledInv() {
        Inventory i = new PlayerInventoryMock(server.addPlayer());
        i.addItem(new ItemStack(Material.ACACIA_BOAT));

        Assertions.assertFalse(Utils.isInventoryEmpty(i));
    }

    // getFreeBlockAroundThisPlace

    @Test
    @DisplayName("[getFreeBlockAroundThisPlace] - if the block is free, it should return the given location")
    public void getFreeBlockAroundThisPlace_freeLoc() {
        Location l = new Location(world, 100,100,100);
        world.setBlockData(l.clone(), BlockDataMock.mock(Material.AIR));

        Location found = Utils.getFreeBlockAroundThisPlace(world, l);
        Assertions.assertNotNull(found);
        this.assertLocationEquals(l, found);
    }

    @Test
    @DisplayName("[getFreeBlockAroundThisPlace] - if no block are free around given location, it should return null")
    public void getFreeBlockAroundThisPlace_noFreeLoc() {
        Location l = new Location(world, 100,100,100);
        world.setBlockData(l.clone(), BlockDataMock.mock(Material.COBBLESTONE));
        world.setBlockData(l.clone().add(1,0,0), BlockDataMock.mock(Material.COBBLESTONE));
        world.setBlockData(l.clone().add(-1,0,0), BlockDataMock.mock(Material.COBBLESTONE));
        world.setBlockData(l.clone().add(0,1,0), BlockDataMock.mock(Material.COBBLESTONE));
        world.setBlockData(l.clone().add(0,-1,0), BlockDataMock.mock(Material.COBBLESTONE));

        Location found = Utils.getFreeBlockAroundThisPlace(world, l);
        Assertions.assertNull(found);
    }

    @Test
    @DisplayName("[getFreeBlockAroundThisPlace] - if the block in x+1 is available, should return it location")
    public void getFreeBlockAroundThisPlace_xPlusOne() {
        Location l = new Location(world, 100,100,100);
        world.setBlockData(l.clone(), BlockDataMock.mock(Material.COBBLESTONE));
        world.setBlockData(l.clone().add(-1,0,0), BlockDataMock.mock(Material.COBBLESTONE));
        world.setBlockData(l.clone().add(0,1,0), BlockDataMock.mock(Material.COBBLESTONE));
        world.setBlockData(l.clone().add(0,-1,0), BlockDataMock.mock(Material.COBBLESTONE));

        world.setBlockData(l.add(1,0,0), BlockDataMock.mock(Material.AIR));

        Location found = Utils.getFreeBlockAroundThisPlace(world, l);
        Assertions.assertNotNull(found);
        this.assertLocationEquals(l, found);
    }

    @Test
    @DisplayName("[getFreeBlockAroundThisPlace] - if all block around are available, it should return the one in x+1")
    public void getFreeBlockAroundThisPlace_allAround() {
        Location l = new Location(world, 100,100,100);
        world.setBlockData(l.clone(), BlockDataMock.mock(Material.COBBLESTONE));
        world.setBlockData(l.clone().add(-1,0,0), BlockDataMock.mock(Material.AIR));
        world.setBlockData(l.clone().add(0,1,0), BlockDataMock.mock(Material.AIR));
        world.setBlockData(l.clone().add(0,-1,0), BlockDataMock.mock(Material.AIR));

        world.setBlockData(l.add(1,0,0), BlockDataMock.mock(Material.AIR));

        Location found = Utils.getFreeBlockAroundThisPlace(world, l);
        Assertions.assertNotNull(found);
        this.assertLocationEquals(l, found);
    }

    // Test utils

    private void assertLocationEquals(Location l1, Location l2) {
        Assertions.assertEquals(l1.x(), l2.x());
        Assertions.assertEquals(l1.y(), l2.y());
        Assertions.assertEquals(l1.z(), l2.z());
    }
}
