package me.crylonz.deadchest.db;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.utils.ItemBytes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.function.Consumer;

import static me.crylonz.deadchest.DeadChestLoader.db;
import static me.crylonz.deadchest.DeadChestLoader.sqlExecutor;

public class ChestDataRepository {

    public static void initTable() {
        sqlExecutor.runAsync(() -> {
            try (Statement st = db.connection().createStatement()) {
                st.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS chest_data (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "player_uuid TEXT NOT NULL," +
                                "player_name TEXT NOT NULL," +
                                "chest_world TEXT NOT NULL," +
                                "chest_x INTEGER NOT NULL," +
                                "chest_y INTEGER NOT NULL," +
                                "chest_z INTEGER NOT NULL," +
                                "chest_yaw REAL NOT NULL," +
                                "chest_pitch REAL NOT NULL," +
                                "chest_date BIGINT NOT NULL," +
                                "is_infinity BOOLEAN NOT NULL," +
                                "is_removed_block BOOLEAN NOT NULL," +
                                "holo_world TEXT NOT NULL," +
                                "holo_x INTEGER NOT NULL," +
                                "holo_y INTEGER NOT NULL," +
                                "holo_z INTEGER NOT NULL," +
                                "holo_yaw REAL NOT NULL," +
                                "holo_pitch REAL NOT NULL," +
                                "holographic_timer_id TEXT NOT NULL," +
                                "holographic_owner_id TEXT NOT NULL," +
                                "world_name TEXT NOT NULL," +
                                "xp_stored INTEGER NOT NULL," +
                                "inventory BLOB" +
                                ")"
                );
                st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_chest_player ON chest_data(player_uuid)");
                st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_chest_location ON chest_data(chest_world, chest_x, chest_z)");
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create chest_data schema", e);
            }

        });
    }

    public static void saveAllAsync(Collection<ChestData> chests) {
        sqlExecutor.runAsync(() -> {
            ChestDataRepository.saveAll(chests);
        });
    }


    /**
     * Usage :
     * ChestDataRepository.findAllAsync(data -> {
     * player.sendMessage("Loaded " + data.size() + " chests!");
     * }, plugin);
     */
    public static void findAllAsync(Consumer<List<ChestData>> callback, Plugin plugin) {
        sqlExecutor.runAsync(() -> {
            List<ChestData> result = findAll();

            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result));
        });
    }


    public static void saveAll(Collection<ChestData> chests) {
        String sql = "INSERT INTO chest_data (" +
                "player_uuid, player_name, chest_world, chest_x, chest_y, chest_z, chest_yaw, chest_pitch, " +
                "chest_date, is_infinity, is_removed_block, " +
                "holo_world, holo_x, holo_y, holo_z, holo_yaw, holo_pitch, " +
                "holographic_timer_id, holographic_owner_id, world_name, xp_stored, inventory" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.connection();
             Statement clear = conn.createStatement();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // On efface tout avant
            clear.executeUpdate("DELETE FROM chest_data");

            // On batch tous les nouveaux coffres
            for (ChestData chest : chests) {
                Location chestLoc = chest.getChestLocation();
                Location holoLoc = chest.getHolographicTimer();

                ps.setString(1, chest.getPlayerUUID());
                ps.setString(2, chest.getPlayerName());

                ps.setString(3, chestLoc.getWorld().getName());
                ps.setInt(4, chestLoc.getBlockX());
                ps.setInt(5, chestLoc.getBlockY());
                ps.setInt(6, chestLoc.getBlockZ());
                ps.setFloat(7, chestLoc.getYaw());
                ps.setFloat(8, chestLoc.getPitch());

                ps.setLong(9, chest.getChestDate().getTime());
                ps.setBoolean(10, chest.isInfinity());
                ps.setBoolean(11, chest.isRemovedBlock());

                ps.setString(12, holoLoc.getWorld().getName());
                ps.setInt(13, holoLoc.getBlockX());
                ps.setInt(14, holoLoc.getBlockY());
                ps.setInt(15, holoLoc.getBlockZ());
                ps.setFloat(16, holoLoc.getYaw());
                ps.setFloat(17, holoLoc.getPitch());

                ps.setString(18, chest.getHolographicTimerId().toString());
                ps.setString(19, chest.getHolographicOwnerId().toString());
                ps.setString(20, chest.getWorldName());
                ps.setInt(21, chest.getXpStored());
                ps.setBytes(22, ItemBytes.toBytesList(chest.getInventory()));


                ps.addBatch();
            }

            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static List<ChestData> findAll() {
        List<ChestData> list = new ArrayList<>();
        try (PreparedStatement ps = db.connection().prepareStatement("SELECT * FROM chest_data");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(deserializeChest(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private static ChestData deserializeChest(ResultSet rs) throws SQLException {
        Location chestLoc = new Location(
                Bukkit.getWorld(rs.getString("chest_world")),
                rs.getInt("chest_x"),
                rs.getInt("chest_y"),
                rs.getInt("chest_z"),
                rs.getFloat("chest_yaw"),
                rs.getFloat("chest_pitch")
        );

        Location holoLoc = new Location(
                Bukkit.getWorld(rs.getString("holo_world")),
                rs.getInt("holo_x"),
                rs.getInt("holo_y"),
                rs.getInt("holo_z"),
                rs.getFloat("holo_yaw"),
                rs.getFloat("holo_pitch")
        );

        return new ChestData(
                ItemBytes.fromBytesList(rs.getBytes("inventory")),
                chestLoc,
                rs.getString("player_name"),
                rs.getString("player_uuid"),
                new Date(rs.getLong("chest_date")),
                rs.getBoolean("is_infinity"),
                rs.getBoolean("is_removed_block"),
                holoLoc,
                UUID.fromString(rs.getString("holographic_timer_id")),
                UUID.fromString(rs.getString("holographic_owner_id")),
                rs.getString("world_name"),
                rs.getInt("xp_stored")
        );
    }
}
