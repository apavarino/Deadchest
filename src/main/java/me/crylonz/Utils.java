package me.crylonz;


import com.sk89q.worldguard.protection.flags.BooleanFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import static me.crylonz.DeadChest.wgsdc;

public class Utils {


    public static BooleanFlag DEADCHEST_NOBODY_FLAG;
    public static BooleanFlag DEADCHEST_OWNER_FLAG;
    public static BooleanFlag DEADCHEST_MEMBER_FLAG;

    static boolean isInventoryEmpty(Inventory inv) {
        for (ItemStack it : inv.getContents()) {
            if (it != null) return false;
        }
        return true;
    }

    static void generateLog(String message) {
        File log = new File("plugins/DeadChest/deadchest.log");
        try {
            log.createNewFile();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            String finalMsg = "[" + formatter.format(date) + "] " + message + "\n";
            Files.write(Paths.get("plugins/DeadChest/deadchest.log"), finalMsg.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            DeadChest.log.warning("Can't write log for Deadchest : " + e);
        }
    }

    public static Location getFreeBlockAroundThisPlace(World world, Location loc) {
        Location newLoc = loc.clone();
        if (world.getBlockAt(loc).isEmpty()) {
            return newLoc;
        }

        if (world.getBlockAt(newLoc.clone().add(1, 0, 0)).isEmpty()) {
            return newLoc.add(1, 0, 0);
        }

        if (world.getBlockAt(newLoc.clone().add(-1, 0, 0)).isEmpty()) {
            return newLoc.add(-1, 0, 0);
        }

        if (world.getBlockAt(newLoc.clone().add(0, 1, 0)).isEmpty()) {
            return newLoc.add(0, 1, 0);
        }

        if (world.getBlockAt(newLoc.clone().add(0, -1, 0)).isEmpty()) {
            return newLoc.add(0, -1, 0);
        }

        return null;

    }

    public static boolean worldGuardCheck(Player p) {
        if (wgsdc != null) {
            return wgsdc.worldGuardChecker(p);
        }
        return true;
    }

    public static boolean isBefore1_16() {
        return (Bukkit.getVersion().contains("1.15")
                || Bukkit.getVersion().contains("1.14")
                || Bukkit.getVersion().contains("1.13")
                || Bukkit.getVersion().contains("1.12"));
    }

}

