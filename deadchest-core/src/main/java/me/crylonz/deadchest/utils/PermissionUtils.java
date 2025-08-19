package me.crylonz.deadchest.utils;

import me.crylonz.deadchest.Permission;
import org.bukkit.entity.Player;

public class PermissionUtils {

    public static final Permission[] REMOVE_ALL = {Permission.REMOVE_OWN, Permission.REMOVE_OTHER};
    public static final Permission[] LIST_ALL = {Permission.LIST_OWN, Permission.LIST_OTHER};

    public static boolean hasOneOf(Player player, Permission[] permissions) {
        for(Permission permission: permissions) {
            if(player.hasPermission(permission.label))
                return true;
        }
        return false;
    }

    public static boolean hasAllOf(Player player, Permission[] permissions) {
        for(Permission permission: permissions) {
            if(!player.hasPermission(permission.label))
                return false;
        }
        return true;
    }

    public static boolean hasAdminOrAllOf(Player player, Permission[] permissions) {
        if(player.hasPermission(Permission.ADMIN.label))
            return true;
        return hasAllOf(player, permissions);
    }

    public static boolean hasAdminOrOneOf(Player player, Permission[] permissions) {
        if(player.hasPermission(Permission.ADMIN.label))
            return true;
        return hasOneOf(player, permissions);
    }

    public static boolean hasAdminOr(Player player, Permission permission) {
        if(player.hasPermission(Permission.ADMIN.label))
            return true;
        return player.hasPermission(permission.label);
    }

}
