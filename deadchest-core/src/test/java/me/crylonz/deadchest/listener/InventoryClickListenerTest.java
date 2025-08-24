package me.crylonz.deadchest.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.inventory.InventoryMock;
import me.crylonz.deadchest.IgnoreInventoryHolder;
import me.crylonz.deadchest.db.IgnoreItemListRepository;
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
import org.mockito.MockedStatic;

import static me.crylonz.deadchest.DeadChestLoader.ignoreList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;

class InventoryClickListenerTest {

    private ServerMock server;
    private PlayerMock player;
    private Inventory top;
    private Inventory bottom;
    private InventoryClickListener listener;
    private MockedStatic<IgnoreItemListRepository> mockRepo;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer("Steve");

        top = new InventoryMock(new IgnoreInventoryHolder(), InventoryType.CHEST);
        bottom = player.getInventory();

        listener = new InventoryClickListener();
        ignoreList = new InventoryMock(null, InventoryType.CHEST);

        mockRepo = mockStatic(IgnoreItemListRepository.class);
        mockRepo.when(() -> IgnoreItemListRepository.saveIgnoreIntoInventory(any()))
                .thenAnswer(inv -> null);
    }

    @AfterEach
    void tearDown() {
        mockRepo.close();
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
    }
}
