package me.crylonz.deadchest;

import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite {
    private final Plugin plugin;
    private Connection conn;

    public SQLite(Plugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            Path dbPath = plugin.getDataFolder().toPath().resolve("data.db");
            String url = "jdbc:sqlite:" + dbPath.toString().replace("\\", "/");

            conn = DriverManager.getConnection(url);

            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL");
                st.execute("PRAGMA synchronous=NORMAL");
                st.execute("PRAGMA foreign_keys=ON");
            }

            createSchema();
        } catch (SQLException e) {
            throw new RuntimeException("SQLite init failed", e);
        }
    }

    private void createSchema() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(
                    "CREATE TABLE IF NOT EXISTS ignore_items ("
                            + "slot INTEGER PRIMARY KEY, "
                            + "data BLOB NOT NULL"
                            + ")"
            );
        }
    }

    public synchronized Connection connection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            init();
        }
        return conn;
    }

    public synchronized void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
