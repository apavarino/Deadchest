package me.crylonz;


import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
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

import static me.crylonz.DeadChest.enableWorldGuardDetection;

public class Utils {

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

    public static boolean worldGuardChecker(Player p) {

        if (!enableWorldGuardDetection) {
            return true;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(p.getLocation().getWorld()));

            if (regions != null) {
                BlockVector3 position = BlockVector3.at(p.getLocation().getX(),
                        p.getLocation().getY(), p.getLocation().getZ());
                ApplicableRegionSet set = regions.getApplicableRegions(position);

                if (set.size() != 0) {
                    for (ProtectedRegion pr : set.getRegions()) {
                        if (pr.getMembers().contains(p.getUniqueId())
                                || pr.getOwners().contains(p.getUniqueId())
                                || p.isOp()) {
                            return true;
                        }
                    }

                    generateLog("Player [" + p.getName() + "] died without [ Worldguard] region permission : No Deadchest generated");
                    return false;
                }
            }
            return true;
        } catch (NoClassDefFoundError e) {
            return true;
        }


    }

}
