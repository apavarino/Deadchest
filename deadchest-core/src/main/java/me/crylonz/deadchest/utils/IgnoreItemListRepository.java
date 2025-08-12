package me.crylonz.deadchest.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.*;

import static me.crylonz.deadchest.DeadChestLoader.db;
import static me.crylonz.deadchest.DeadChestLoader.log;


public class IgnoreItemListRepository {

    public static void loadIgnoreIntoInventory(Inventory inv) {
        try {
            boolean any = false;
            ItemStack[] contents = new ItemStack[inv.getSize()];

            try (Connection connection = db.connection();
                 PreparedStatement request = connection.prepareStatement("SELECT slot, data FROM ignore_items");
                 ResultSet result = request.executeQuery()) {

                while (result.next()) {
                    int slot = result.getInt("slot");
                    if (slot < 0 || slot >= contents.length) continue;
                    byte[] data = result.getBytes("data");
                    contents[slot] = ItemBytes.fromBytes(data);
                    any = true;
                }
            }

            if (any) {
                inv.setContents(contents);
                log.info("[DeadChest] Ignore list loaded from SQLite.");
                return;
            }

            log.info("[DeadChest] Ignore list empty (no SQLite row)");

        } catch (SQLException e) {
            log.severe("[DeadChest] Failed to load ignore list from SQLite: " + e.getMessage());
        }
    }

    public static void saveIgnoreIntoInventory(Inventory inv) {

        try {
            try (Connection c = db.connection();
                 Statement clear = c.createStatement()) {
                clear.executeUpdate("DELETE FROM ignore_items");
            }
            String sql = "INSERT INTO ignore_items(slot, data) VALUES(?, ?)";
            try (Connection connection = db.connection();
                 PreparedStatement request = connection.prepareStatement(sql)) {

                ItemStack[] contents = inv.getContents();
                for (int slot = 0; slot < contents.length; slot++) {
                    byte[] bytes = ItemBytes.toBytes(contents[slot]);
                    if (bytes == null || bytes.length == 0) continue;
                    request.setInt(1, slot);
                    request.setBytes(2, bytes);
                    request.addBatch();
                }
                request.executeBatch();
            }
            log.info("[DeadChest] Ignore list saved to SQLite.");
        } catch (SQLException e) {
            log.severe("[DeadChest] Failed to save ignore list to SQLite: " + e.getMessage());
        }
    }
}
