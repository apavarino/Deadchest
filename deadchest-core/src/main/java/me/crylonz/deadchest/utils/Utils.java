package me.crylonz.deadchest.utils;

import me.crylonz.deadchest.DeadChestLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import static me.crylonz.deadchest.DeadChestLoader.*;

public class Utils {


    public static void generateLog(String message) {
        File log = new File("plugins/DeadChest/deadchest.log");
        try {
            log.createNewFile();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            String finalMsg = "[" + formatter.format(date) + "] " + message + "\n";
            Files.write(Paths.get("plugins/DeadChest/deadchest.log"), finalMsg.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            DeadChestLoader.log.warning("Can't write log for Deadchest : " + e);
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


    public static boolean isBefore1_18() {
        return Bukkit.getVersion().contains("1.17") || isBefore1_17();
    }

    public static boolean isBefore1_17() {
        return Bukkit.getVersion().contains("1.16") || isBefore1_16();
    }

    public static boolean isBefore1_16() {
        return (Bukkit.getVersion().contains("1.15")
                || Bukkit.getVersion().contains("1.14")
                || Bukkit.getVersion().contains("1.13")
                || Bukkit.getVersion().contains("1.12"));
    }

    public static boolean isGraveBlock(Material material) {
        return graveBlocks.contains(material);
    }

    public static int computeMinHeight() {
        if (isBefore1_18()) {
            return 1;
        } else {
            return -64;
        }
    }

    public static boolean checkTheEndGeneration(Entity player, Plugin deadChest) {
        return player.getWorld().getEnvironment().equals(World.Environment.THE_END) &&
                !deadChest.getConfig().getBoolean(ConfigKey.GENERATE_IN_THE_END.toString());
    }

    public static void computeChestType(Block b, Player p) {
        switch (config.getInt(ConfigKey.DROP_BLOCK)) {
            case 2:
                b.setType(Material.PLAYER_HEAD);
                b.setMetadata("deadchest", new FixedMetadataValue(plugin, true));
                BlockState state = b.getState();
                Skull skull = (Skull) state;
                if (p != null) {
                    skull.setOwningPlayer(p);
                }
                skull.update();
                break;
            case 3:
                b.setType(Material.BARREL);
                break;
            case 4:
                b.setType(Material.SHULKER_BOX);
                break;
            case 5:
                b.setType(Material.ENDER_CHEST);
                break;
            default:
                b.setType(Material.CHEST);
        }
    }
}

