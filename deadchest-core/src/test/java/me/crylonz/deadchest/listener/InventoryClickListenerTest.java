package me.crylonz.deadchest.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.inventory.InventoryMock;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.IgnoreInventoryHolder;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.DeadChestConfig;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static me.crylonz.deadchest.DeadChestLoader.ignoreList;
import static org.junit.jupiter.api.Assertions.*;

class InventoryClickListenerTest {

    private ServerMock server;
    private PlayerMock player;
    private Inventory top;
    private Inventory bottom;
    private InventoryClickListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        DeadChestLoader.plugin = MockBukkit.createMockPlugin();
        DeadChestConfig.clearCache();
        DeadChestLoader.config = new DeadChestConfig(DeadChestLoader.plugin);
        DeadChestLoader.config.register(ConfigKey.IGNORED_ITEMS.toString(), java.util.Arrays.asList());
        player = server.addPlayer("Steve");

        top = new InventoryMock(new IgnoreInventoryHolder(), InventoryType.CHEST);
        bottom = player.getInventory();

        listener = new InventoryClickListener();
        ignoreList = top;
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private InventoryView openView() {
        return player.openInventory(top);
    }

    @Test
    void testRemoveItemFromTopInventory() {
        ItemStack stack = new ItemStack(Material.STONE, 2);
        top.setItem(0, stack);

        InventoryView view = openView();

        InventoryClickEvent event = new InventoryClickEvent(
                view,
                InventoryType.SlotType.CONTAINER,
                0,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL
        );
        event.setCurrentItem(stack);

        listener.onInventoryClick(event);

        server.getScheduler().performTicks(1);

        ItemStack result = top.getItem(0);
        assertNotNull(result, "Item must remain because the stack was 2");
        assertEquals(1, result.getAmount(), "The stack must be decrease by 1");
    }

    @Test
    void testRemoveLastItemFromTopInventory() {
        ItemStack stack = new ItemStack(Material.STONE, 1);
        top.setItem(0, stack);

        InventoryView view = openView();

        InventoryClickEvent event = new InventoryClickEvent(
                view,
                InventoryType.SlotType.CONTAINER,
                0,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL
        );
        event.setCurrentItem(stack);

        listener.onInventoryClick(event);

        server.getScheduler().performTicks(1);

        assertNull(top.getItem(0), "Item must be removed if quantity = 1");
    }

    @Test
    void testAddItemFromBottomInventory() {
        ItemStack stack = new ItemStack(Material.DIAMOND, 1);
        bottom.setItem(0, stack);

        InventoryView view = openView();

        int slot = top.getSize();

        InventoryClickEvent event = new InventoryClickEvent(
                view,
                InventoryType.SlotType.CONTAINER,
                slot,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL
        );
        event.setCurrentItem(stack);

        listener.onInventoryClick(event);

        server.getScheduler().performTicks(1);

        assertTrue(ignoreList.contains(Material.DIAMOND), "Item must be added to ignore list");
        assertEquals(java.util.List.of("DIAMOND"), DeadChestLoader.config.getIgnoredEntries());
    }

    @Test
    void testAddStackedItemFromBottomInventoryDoesNotDuplicateIgnoredEntry() {
        ignoreList.setItem(0, new ItemStack(Material.DIAMOND, 1));
        DeadChestLoader.config.setIgnoredEntries(java.util.List.of("DIAMOND"));

        ItemStack stack = new ItemStack(Material.DIAMOND, 64);
        bottom.setItem(0, stack);

        InventoryView view = openView();

        int slot = top.getSize();

        InventoryClickEvent event = new InventoryClickEvent(
                view,
                InventoryType.SlotType.CONTAINER,
                slot,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL
        );
        event.setCurrentItem(stack);

        listener.onInventoryClick(event);

        server.getScheduler().performTicks(1);

        int diamondEntries = 0;
        for (ItemStack item : ignoreList.getContents()) {
            if (item != null && item.getType() == Material.DIAMOND) {
                diamondEntries++;
                assertEquals(1, item.getAmount(), "Ignored GUI entries must remain normalized to one item");
            }
        }

        assertEquals(1, diamondEntries, "A stacked item should not create a duplicate ignore entry");
        assertEquals(java.util.List.of("DIAMOND"), DeadChestLoader.config.getIgnoredEntries());
    }

    @Test
    void testRemoveItemFromTopInventoryUpdatesYamlIgnoredItems() {
        ignoreList.setItem(0, new ItemStack(Material.STONE, 1));
        ignoreList.setItem(1, new ItemStack(Material.DIRT, 1));
        DeadChestLoader.config.setIgnoredEntries(java.util.List.of("STONE", "DIRT"));

        InventoryView view = openView();

        InventoryClickEvent event = new InventoryClickEvent(
                view,
                InventoryType.SlotType.CONTAINER,
                0,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL
        );
        event.setCurrentItem(ignoreList.getItem(0));

        listener.onInventoryClick(event);

        server.getScheduler().performTicks(1);

        assertFalse(DeadChestLoader.config.getIgnoredEntries().contains("STONE"));
        assertEquals(java.util.List.of("DIRT"), DeadChestLoader.config.getIgnoredEntries());
    }

    @Test
    void testCustomItemIsStoredAsSerializedIgnoredEntry() {
        ItemStack custom = new ItemStack(Material.DIAMOND_SWORD, 1);
        custom.editMeta(meta -> meta.setDisplayName("Boss sword"));
        bottom.setItem(0, custom);

        InventoryView view = openView();
        int slot = top.getSize();

        InventoryClickEvent event = new InventoryClickEvent(
                view,
                InventoryType.SlotType.CONTAINER,
                slot,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL
        );
        event.setCurrentItem(custom);

        listener.onInventoryClick(event);

        server.getScheduler().performTicks(1);

        Object firstEntry = DeadChestLoader.config.getIgnoredEntries().get(0);
        assertTrue(firstEntry instanceof ItemStack, "Custom ignored items should be stored as serialized ItemStacks in config");
        ItemStack stored = (ItemStack) firstEntry;
        assertEquals(Material.DIAMOND_SWORD, stored.getType());
        assertEquals("Boss sword", stored.getItemMeta().getDisplayName());
    }
}
