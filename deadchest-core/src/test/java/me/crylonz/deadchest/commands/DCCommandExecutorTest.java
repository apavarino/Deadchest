package me.crylonz.deadchest.commands;

import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.Localization;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DCCommandExecutorTest {

    private Command command;
    private CommandSender sender;
    private DCCommandExecutor executor;

    @BeforeEach
    void setUp() {
        Localization localization = new Localization();
        Map<String, Object> values = new HashMap<>();
        values.put("common.prefix", "[DeadChest] ");
        values.put("commands.error.unknown", "Unrecognized Command");
        values.put("commands.error.player-only", "Command must be called by a player");
        localization.set(values);
        DeadChestLoader.local = localization;
        DeadChestLoader.plugin = mock(org.bukkit.plugin.Plugin.class);
        executor = new DCCommandExecutor(mock(DeadChestLoader.class));
        sender = mock(CommandSender.class);
        command = mock(Command.class);
        when(command.getName()).thenReturn("dc");
    }

    @Test
    void unknownCommandReturnsFalseAndSendsFeedback() {
        boolean result = executor.onCommand(sender, command, "dc", new String[]{"unknown"});

        assertFalse(result);
        verify(sender, times(1)).sendMessage(contains("Unrecognized Command"));
    }

    @Test
    void recognizedRemoveCommandFromConsoleReturnsTrue() {
        boolean result = executor.onCommand(sender, command, "dc", new String[]{"remove"});

        assertTrue(result);
        verify(sender, atLeastOnce()).sendMessage(contains("Command must be called by a player"));
    }
}
