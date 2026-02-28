package me.crylonz.deadchest.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.crylonz.deadchest.Permission;
import org.bukkit.command.Command;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DCTabCompletionTest {

    private ServerMock server;
    private DCTabCompletion tabCompletion;
    private Command dcCommand;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        tabCompletion = new DCTabCompletion();
        dcCommand = mock(Command.class);
        when(dcCommand.getName()).thenReturn("dc");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void adminGetsMainSuggestions() {
        PlayerMock admin = server.addPlayer("Admin");
        admin.addAttachment(MockBukkit.createMockPlugin(), Permission.ADMIN.label, true);

        List<String> suggestions = tabCompletion.onTabComplete(admin, dcCommand, "dc", new String[]{""});

        assertTrue(suggestions.contains("reload"));
        assertTrue(suggestions.contains("removeinfinite"));
        assertTrue(suggestions.contains("removeall"));
        assertTrue(suggestions.contains("repair"));
        assertTrue(suggestions.contains("ignore"));
    }

    @Test
    void removeSecondArgSuggestsOnlinePlayersForAuthorizedUser() {
        PlayerMock moderator = server.addPlayer("Mod");
        moderator.addAttachment(MockBukkit.createMockPlugin(), Permission.REMOVE_OTHER.label, true);
        server.addPlayer("Steve");
        server.addPlayer("Alex");

        List<String> suggestions = tabCompletion.onTabComplete(moderator, dcCommand, "dc", new String[]{"remove", ""});

        assertTrue(suggestions.contains("Steve"));
        assertTrue(suggestions.contains("Alex"));
    }

    @Test
    void listSecondArgContainsAllAndPlayerNames() {
        PlayerMock admin = server.addPlayer("Admin");
        admin.addAttachment(MockBukkit.createMockPlugin(), Permission.ADMIN.label, true);
        server.addPlayer("Steve");

        List<String> suggestions = tabCompletion.onTabComplete(admin, dcCommand, "dc", new String[]{"list", ""});

        assertTrue(suggestions.contains("all"));
        assertTrue(suggestions.contains("Steve"));
    }
}

