package me.crylonz.deadchest.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.ArmorStandMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorstandListenerTest {

    private static Plugin plugin;
    private PlayerMock player;
    private ArmorStandMock armorStand;
    private ArmorstandListener listener;

    @BeforeAll
    static void beforeAll() {
        MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
    }

    @AfterAll
    static void afterAll() {
        MockBukkit.unmock();
    }

    @BeforeEach
    void setUp() {
        player = new PlayerMock(MockBukkit.getMock(), "TestPlayer");
        armorStand = new ArmorStandMock(MockBukkit.getMock(), UUID.randomUUID());
        armorStand.teleport(player.getLocation());
        listener = new ArmorstandListener();
    }

    @Test
    void testEventCancelledWhenInvisibleAndHasMetadata() {
        armorStand.setVisible(false);
        armorStand.setMetadata("deadchest", new FixedMetadataValue(plugin, true));

        PlayerArmorStandManipulateEvent event =
                new PlayerArmorStandManipulateEvent(player, armorStand, null, null, null);

        listener.onPlayerArmorStandManipulateEvent(event);

        assertTrue(event.isCancelled(), "Event should be cancelled when armorstand is invisible and has deadchest metadata");
    }

    @Test
    void testEventNotCancelledWhenVisible() {
        armorStand.setVisible(true);
        armorStand.setMetadata("deadchest", new FixedMetadataValue(plugin, true));

        PlayerArmorStandManipulateEvent event =
                new PlayerArmorStandManipulateEvent(player, armorStand, null, null, null);

        listener.onPlayerArmorStandManipulateEvent(event);

        assertFalse(event.isCancelled(), "Event should not be cancelled when armorstand is visible");
    }

    @Test
    void testEventNotCancelledWhenNoMetadata() {
        armorStand.setVisible(false);

        PlayerArmorStandManipulateEvent event =
                new PlayerArmorStandManipulateEvent(player, armorStand, null, null, null);

        listener.onPlayerArmorStandManipulateEvent(event);

        assertFalse(event.isCancelled(), "Event should not be cancelled when armorstand has no deadchest metadata");
    }
}
