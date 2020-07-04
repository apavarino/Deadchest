package me.crylonz;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

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

}
