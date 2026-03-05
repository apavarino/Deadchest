package me.crylonz.deadchest.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.Localization;
import me.crylonz.deadchest.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DCCommandRegistrationServiceTest {

    private ServerMock server;
    private DCCommandRegistrationService service;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Localization localization = new Localization();
        Map<String, Object> values = new HashMap<>();
        values.put("common.prefix", "[DeadChest] ");
        values.put("commands.error.player-only", "Command must be called by a player");
        localization.set(values);
        DeadChestLoader.local = localization;
        DeadChestLoader.ignoreList = server.createInventory(null, 9);
        service = new DCCommandRegistrationService(mock(DeadChestLoader.class));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void registerRemoveOwnRequiresPlayerContext() {
        CommandSender console = mock(CommandSender.class);

        service.register(console, new String[]{"remove"});
        service.registerRemoveOwn();

        assertTrue(service.isCommandSucceed());
        verify(console, atLeastOnce()).sendMessage(contains("Command must be called by a player"));
    }

    @Test
    void registerIgnoreListOpensInventoryForAdminPlayer() {
        PlayerMock admin = server.addPlayer("Admin");
        admin.addAttachment(MockBukkit.createMockPlugin(), Permission.ADMIN.label, true);
        Inventory ignoreInventory = DeadChestLoader.ignoreList;

        service.register(admin, new String[]{"ignore"});
        service.registerIgnoreList();

        assertTrue(service.isCommandSucceed());
        assertNotNull(admin.getOpenInventory());
        assertSame(ignoreInventory, admin.getOpenInventory().getTopInventory());
    }
}
