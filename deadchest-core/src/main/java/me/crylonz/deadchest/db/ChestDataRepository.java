package me.crylonz.deadchest.db;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.utils.ItemBytes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.function.Consumer;

import static me.crylonz.deadchest.DeadChestLoader.db;
import static me.crylonz.deadchest.DeadChestLoader.sqlExecutor;

public class ChestDataRepository {

    public static void initTable(Runnable afterCreation) {
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
                //st.executeUpdate("DROP INDEX IF EXISTS idx_chest_location");
                ckeckIfUpdated();
                st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_chest_location ON chest_data(chest_world, chest_x, chest_y, chest_z)");
                afterCreation.run();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create chest_data schema", e);
            }

        });
    }

    public static void saveAllAsync(Collection<ChestData> chests) {
        sqlExecutor.runAsync(() -> {
            ChestDataRepository.batchSave(chests);
        });
    }

    public static void saveAsync(@Nonnull final ChestData chest, @Nonnull final Consumer<Boolean> containsChestOnLoc) {
        sqlExecutor.runAsync(() -> {
            containsChestOnLoc.accept(ChestDataRepository.save(chest));
        });
    }

    public static void updateAsync(@Nonnull final ChestData chest, @Nonnull final Consumer<Boolean> updateInsert) {
        sqlExecutor.runAsync(() -> {
            updateInsert.accept(ChestDataRepository.update(chest));
        });
    }

    public static void removeAsync(@Nonnull final ChestData chest) {
        sqlExecutor.runAsync(() -> {
            ChestDataRepository.remove(chest);
        });
    }

    public static void removeBatchAsync(@Nonnull final Collection<ChestData> chest) {
        sqlExecutor.runAsync(() -> {
            ChestDataRepository.remove(chest);
        });
    }


    public static void clearAsync() {
        sqlExecutor.runAsync(ChestDataRepository::clear);
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

                ps.setString(1, chest.getPlayerStringUUID());
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

    public static void batchSave(Collection<ChestData> chests) {
        final String checkDuplicate = "SELECT 1 FROM chest_data WHERE player_uuid = ? AND chest_world = ? AND chest_x = ? AND chest_y = ? AND chest_z = ? LIMIT 1";
        final String sqlUpdate = "UPDATE chest_data SET " +
                "chest_date = ?," +
                " is_infinity = ?, " +
                "is_removed_block = ?, " +
                "holo_world = ?, " +
                "holo_x = ?, " +
                "holo_y = ?, " +
                "holo_z = ?, " +
                "holo_yaw = ?, " +
                "holo_pitch = ?, " +
                "holographic_timer_id = ?, " +
                "holographic_owner_id = ?, " +
                "world_name = ?, " +
                "xp_stored = ?, " +
                "inventory = ? " +
                "WHERE player_uuid = ? AND chest_world = ? AND chest_x = ? AND chest_y = ? AND chest_z = ?";

        try (Connection conn = db.connection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCheck = conn.prepareStatement(checkDuplicate);
                 PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                boolean hasUpdates = false;
                for (ChestData chest : chests) {
                    Location chestLoc = chest.getChestLocation();
                    Location holoLoc = chest.getHolographicTimer();

                    // Check if row exists
                    psCheck.setString(1, chest.getPlayerStringUUID());
                    psCheck.setString(2, chestLoc.getWorld().getName());
                    psCheck.setInt(3, chestLoc.getBlockX());
                    psCheck.setInt(4, chestLoc.getBlockY());
                    psCheck.setInt(5, chestLoc.getBlockZ());

                    boolean exists;
                    try (ResultSet rs = psCheck.executeQuery()) {
                        exists = rs.next();
                    }
                    if (!exists) {
                        save(chest);
                    } else {
                        int i = 1;
                        psUpdate.setLong(i++, chest.getChestDate().getTime());
                        psUpdate.setBoolean(i++, chest.isInfinity());
                        psUpdate.setBoolean(i++, chest.isRemovedBlock());

                        psUpdate.setString(i++, holoLoc.getWorld().getName());
                        psUpdate.setInt(i++, holoLoc.getBlockX());
                        psUpdate.setInt(i++, holoLoc.getBlockY());
                        psUpdate.setInt(i++, holoLoc.getBlockZ());
                        psUpdate.setFloat(i++, holoLoc.getYaw());
                        psUpdate.setFloat(i++, holoLoc.getPitch());

                        psUpdate.setString(i++, chest.getHolographicTimerId().toString());
                        psUpdate.setString(i++, chest.getHolographicOwnerId().toString());
                        psUpdate.setString(i++, chest.getWorldName());
                        psUpdate.setInt(i++, chest.getXpStored());
                        psUpdate.setBytes(i++, ItemBytes.toBytesList(chest.getInventory()));

                        psUpdate.setString(i++, chest.getPlayerStringUUID());
                        psUpdate.setString(i++, chestLoc.getWorld().getName());
                        psUpdate.setInt(i++, chestLoc.getBlockX());
                        psUpdate.setInt(i++, chestLoc.getBlockY());
                        psUpdate.setInt(i + 1, chestLoc.getBlockZ());
                        psUpdate.addBatch();
                        hasUpdates = true;
                    }
                }
                if (hasUpdates) {
                    psUpdate.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean update(@Nonnull final ChestData chest) {
        final String sqlUpdate = "UPDATE chest_data SET " +
                "chest_date = ?, " +
                "is_infinity = ?, " +
                "is_removed_block = ?, " +
                "holo_world = ?, " +
                "holo_x = ?, " +
                "holo_y = ?, " +
                "holo_z = ?, " +
                "holo_yaw = ?, " +
                "holo_pitch = ?, " +
                "holographic_timer_id = ?, " +
                "holographic_owner_id = ?, " +
                "world_name = ?, " +
                "xp_stored = ?, " +
                "inventory = ? " +
                "WHERE player_uuid = ? AND chest_world = ? AND chest_x = ? AND chest_y = ? AND chest_z = ?";

        final String checkDublicate = "SELECT player_uuid, " +
                "chest_world, " +
                "chest_x, " +
                "chest_y, " +
                "chest_z " +
                "FROM chest_data WHERE player_uuid = ? AND chest_world = ? AND chest_x = ? AND chest_y = ? AND chest_z = ? LIMIT 1";

        try (Connection conn = db.connection();
             PreparedStatement psDublicate = conn.prepareStatement(checkDublicate);
             PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
            Location chestLoc = chest.getChestLocation();
            Location holoLoc = chest.getHolographicTimer();

            psDublicate.setString(1, chest.getPlayerStringUUID());
            psDublicate.setString(2, chestLoc.getWorld().getName());
            psDublicate.setInt(3, chestLoc.getBlockX());
            psDublicate.setInt(4, chestLoc.getBlockY());
            psDublicate.setInt(5, chestLoc.getBlockZ());
            try (ResultSet rs = psDublicate.executeQuery()) {
                if (!rs.next()) {
                    save(chest);
                    return true;
                }
            }

            ps.setLong(1, chest.getChestDate().getTime());
            ps.setBoolean(2, chest.isInfinity());
            ps.setBoolean(3, chest.isRemovedBlock());

            ps.setString(4, holoLoc.getWorld().getName());
            ps.setInt(5, holoLoc.getBlockX());
            ps.setInt(6, holoLoc.getBlockY());
            ps.setInt(7, holoLoc.getBlockZ());
            ps.setFloat(8, holoLoc.getYaw());
            ps.setFloat(9, holoLoc.getPitch());

            ps.setString(10, chest.getHolographicTimerId().toString());
            ps.setString(11, chest.getHolographicOwnerId().toString());
            ps.setString(12, chest.getWorldName());
            ps.setInt(13, chest.getXpStored());
            ps.setBytes(14, ItemBytes.toBytesList(chest.getInventory()));

            ps.setString(15, chest.getPlayerStringUUID());
            ps.setString(16, chestLoc.getWorld().getName());
            ps.setInt(17, chestLoc.getBlockX());
            ps.setInt(18, chestLoc.getBlockY());
            ps.setInt(19, chestLoc.getBlockZ());
            ps.setFloat(20, chestLoc.getYaw());
            ps.setFloat(21, chestLoc.getPitch());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }


    public static boolean save(@Nonnull final ChestData chest) {
        final String sql = "INSERT INTO chest_data (" +
                "player_uuid, player_name, chest_world, chest_x, chest_y, chest_z, chest_yaw, chest_pitch, " +
                "chest_date, is_infinity, is_removed_block, " +
                "holo_world, holo_x, holo_y, holo_z, holo_yaw, holo_pitch, " +
                "holographic_timer_id, holographic_owner_id, world_name, xp_stored, inventory" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        final String checkDublicate = "SELECT player_uuid, " +
                "chest_world, " +
                "chest_x, " +
                "chest_y, " +
                "chest_z " +
                "FROM chest_data WHERE player_uuid = ? AND chest_world = ? AND chest_x = ? AND chest_y = ? AND chest_z = ? LIMIT 1";

        try (Connection conn = db.connection();
             PreparedStatement psDublicate = conn.prepareStatement(checkDublicate);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Location chestLoc = chest.getChestLocation();
            Location holoLoc = chest.getHolographicTimer();

            psDublicate.setString(1, chest.getPlayerStringUUID());
            psDublicate.setString(2, chestLoc.getWorld().getName());
            psDublicate.setInt(3, chestLoc.getBlockX());
            psDublicate.setInt(4, chestLoc.getBlockY());
            psDublicate.setInt(5, chestLoc.getBlockZ());
            try (ResultSet rs = psDublicate.executeQuery()) {
                if (rs.next())
                    return true;
            }

            ps.setString(1, chest.getPlayerStringUUID());
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

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static void remove(@Nonnull final Collection<ChestData> chests) {
        String sql = "DELETE FROM chest_data WHERE player_uuid = ? AND chest_world = ? AND chest_x = ? AND chest_y = ? AND chest_z = ?";

        try (Connection conn = db.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            boolean batchEmpty = true;
            for (ChestData chest : chests) {
                Location chestLoc = chest.getChestLocation();
                ps.setString(1, chest.getPlayerStringUUID());
                ps.setString(2, chestLoc.getWorld().getName());
                ps.setInt(3, chestLoc.getBlockX());
                ps.setInt(4, chestLoc.getBlockY());
                ps.setInt(5, chestLoc.getBlockZ());
                ps.addBatch();
                batchEmpty = false;
            }
            if (!batchEmpty)
                ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void remove(@Nonnull final ChestData chest) {
        String sql = "DELETE FROM chest_data WHERE player_uuid = ? AND chest_world = ? AND chest_x = ? AND chest_y = ? AND chest_z = ?";

        try (Connection conn = db.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Location chestLoc = chest.getChestLocation();
            ps.setString(1, chest.getPlayerStringUUID());
            ps.setString(2, chestLoc.getWorld().getName());
            ps.setInt(3, chestLoc.getBlockX());
            ps.setInt(4, chestLoc.getBlockY());
            ps.setInt(5, chestLoc.getBlockZ());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clear() {
        String sql = "DELETE FROM chest_data";

        try (Connection conn = db.connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
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
                UUID.fromString(rs.getString("player_uuid")),
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

    private static void ckeckIfUpdated() {
        try (Connection connection = db.connection();
             Statement st = connection.createStatement()) {
            final List<String> columns = new ArrayList<>();

            try (ResultSet rs = st.executeQuery("PRAGMA index_info('idx_chest_location')")) {
                while (rs.next()) {
                    columns.add(rs.getString("name"));
                }
            }

            final boolean hasCorrectIndex =
                    columns.size() == 4 &&
                            columns.get(0).equals("chest_world") &&
                            columns.get(1).equals("chest_x") &&
                            columns.get(2).equals("chest_y") &&
                            columns.get(3).equals("chest_z");

            if (!hasCorrectIndex) {
                try (Statement createIndex = connection.createStatement()) {
                    createIndex.execute("DROP INDEX IF EXISTS idx_chest_location");
                    createIndex.execute(
                            "CREATE INDEX idx_chest_location " +
                                    "ON chest_data (chest_world, chest_x, chest_y, chest_z)"
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
