package me.crylonz.deadchest

import me.crylonz.Permission
import me.crylonz.Permission.ADMIN
import org.bukkit.entity.Player

object PermissionUtils {

    var REMOVE_ALL = listOf(Permission.REMOVE_OWN, Permission.REMOVE_OTHER)
    val LIST_ALL = listOf(Permission.LIST_OWN, Permission.LIST_OTHER)

    fun hasOneOf(player: Player, permissions: List<Permission>): Boolean {
        return permissions.any { player.hasPermission(it.label) }
    }

    fun hasAllOf(player: Player, permissions: List<Permission>): Boolean {
        return permissions.all { player.hasPermission(it.label) }
    }

    fun hasAdminOrAllOf(player: Player, permissions: List<Permission>): Boolean {
        return if (player.hasPermission(ADMIN.label)) true else hasAllOf(player, permissions)
    }

    fun hasAdminOrOneOf(player: Player, permissions: List<Permission>): Boolean {
        return if (player.hasPermission(ADMIN.label)) true else hasOneOf(player, permissions)
    }

    fun hasAdminOr(player: Player, permission: Permission): Boolean {
        return if (player.hasPermission(ADMIN.label)) true else player.hasPermission(permission.label)
    }
}

