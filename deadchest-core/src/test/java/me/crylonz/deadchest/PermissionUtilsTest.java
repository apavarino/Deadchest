package me.crylonz.deadchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.crylonz.deadchest.utils.PermissionUtils;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermissionUtilsTest {

    private PlayerMock player;
    private static Plugin plugin;

    @BeforeAll
    static void setUpServer() {
        MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
    }

    @AfterAll
    static void tearDownServer() {
        MockBukkit.unmock();
    }

    @BeforeEach
    void setUp() {
        player = new PlayerMock(MockBukkit.getMock(), "TestPlayer");
    }

    private void givePermission(String permission) {
        PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission(permission, true);
    }

    @Test
    void testHasOneOf_trueWhenOnePermissionPresent() {
        givePermission(Permission.REMOVE_OWN.label);
        assertTrue(PermissionUtils.hasOneOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasOneOf_falseWhenNoPermissionPresent() {
        assertFalse(PermissionUtils.hasOneOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasAllOf_trueWhenAllPermissionsPresent() {
        givePermission(Permission.REMOVE_OWN.label);
        givePermission(Permission.REMOVE_OTHER.label);
        assertTrue(PermissionUtils.hasAllOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasAllOf_falseWhenOneMissing() {
        givePermission(Permission.REMOVE_OWN.label);
        assertFalse(PermissionUtils.hasAllOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasAdminOrAllOf_trueWhenAdmin() {
        givePermission(Permission.ADMIN.label);
        assertTrue(PermissionUtils.hasAdminOrAllOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasAdminOrAllOf_trueWhenAllPresentWithoutAdmin() {
        givePermission(Permission.REMOVE_OWN.label);
        givePermission(Permission.REMOVE_OTHER.label);
        assertTrue(PermissionUtils.hasAdminOrAllOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasAdminOrAllOf_falseWhenMissingSomeAndNoAdmin() {
        givePermission(Permission.REMOVE_OWN.label);
        assertFalse(PermissionUtils.hasAdminOrAllOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasAdminOrOneOf_trueWhenAdmin() {
        givePermission(Permission.ADMIN.label);
        assertTrue(PermissionUtils.hasAdminOrOneOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasAdminOrOneOf_trueWhenOnePresentWithoutAdmin() {
        givePermission(Permission.REMOVE_OWN.label);
        assertTrue(PermissionUtils.hasAdminOrOneOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasAdminOrOneOf_falseWhenNonePresentWithoutAdmin() {
        assertFalse(PermissionUtils.hasAdminOrOneOf(player, PermissionUtils.REMOVE_ALL));
    }

    @Test
    void testHasAdminOr_trueWhenAdmin() {
        givePermission(Permission.ADMIN.label);
        assertTrue(PermissionUtils.hasAdminOr(player, Permission.REMOVE_OWN));
    }

    @Test
    void testHasAdminOr_trueWhenPermissionPresentWithoutAdmin() {
        givePermission(Permission.REMOVE_OWN.label);
        assertTrue(PermissionUtils.hasAdminOr(player, Permission.REMOVE_OWN));
    }

    @Test
    void testHasAdminOr_falseWhenNoPermissionAndNoAdmin() {
        assertFalse(PermissionUtils.hasAdminOr(player, Permission.REMOVE_OWN));
    }
}
