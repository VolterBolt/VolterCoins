package dev.coins.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private final File dataFolder;
    private final String databaseType;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.databaseType = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();
    }

    public void initialize() throws SQLException {
        if ("MYSQL".equals(databaseType)) {
            connectMySQL();
        } else {
            connectSQLite();
        }

        initializeSchema();
    }

    private void connectSQLite() throws SQLException {
        File dbFile = new File(dataFolder, plugin.getConfig().getString("database.sqlite.file", "data.db"));
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);
        plugin.getLogger().info("Connected to SQLite database: " + dbFile.getAbsolutePath());
    }

    private void connectMySQL() throws SQLException {
        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "voltercoin");
        String username = plugin.getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "password");
        String ssl = plugin.getConfig().getBoolean("database.mysql.use-ssl", false) ? "true" : "false";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ssl + "&autoReconnect=true&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        connection = DriverManager.getConnection(url, username, password);
        plugin.getLogger().info("Connected to MySQL database: " + database);
    }

    private void initializeSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if ("MYSQL".equals(databaseType)) {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS player_balances (" +
                                "uuid VARCHAR(36) PRIMARY KEY, " +
                                "balance DECIMAL(18,2) NOT NULL DEFAULT 0.00, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );

                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS transaction_history (" +
                                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                                "uuid VARCHAR(36) NOT NULL, " +
                                "type VARCHAR(32) NOT NULL, " +
                                "amount DECIMAL(18,2) NOT NULL, " +
                                "description TEXT, " +
                                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );
            } else {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS player_balances (" +
                                "uuid TEXT PRIMARY KEY, " +
                                "balance DECIMAL(18,2) NOT NULL DEFAULT 0.00, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );

                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS transaction_history (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "uuid TEXT NOT NULL, " +
                                "type TEXT NOT NULL, " +
                                "amount DECIMAL(18,2) NOT NULL, " +
                                "description TEXT, " +
                                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean hasAccount(UUID playerUuid) {
        String sql = "SELECT 1 FROM player_balances WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check account existence for " + playerUuid + ": " + e.getMessage());
            return false;
        }
    }

    public double getBalance(UUID playerUuid) {
        String sql = "SELECT balance FROM player_balances WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to fetch balance for " + playerUuid + ": " + e.getMessage());
        }
        return 0.0;
    }

    public void setBalance(UUID playerUuid, double amount) {
        String sql = "INSERT INTO player_balances(uuid, balance, updated_at) VALUES(?, ?, CURRENT_TIMESTAMP) " +
                "ON DUPLICATE KEY UPDATE balance = VALUES(balance), updated_at = CURRENT_TIMESTAMP";

        if ("SQLITE".equals(databaseType)) {
            sql = "INSERT INTO player_balances(uuid, balance, updated_at) VALUES(?, ?, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance, updated_at = CURRENT_TIMESTAMP";
        }

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            statement.setDouble(2, amount);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set balance for " + playerUuid + ": " + e.getMessage());
        }
    }

    public void addBalance(UUID playerUuid, double amount) {
        double current = getBalance(playerUuid);
        setBalance(playerUuid, current + amount);
    }

    public void removeBalance(UUID playerUuid, double amount) {
        double current = getBalance(playerUuid);
        setBalance(playerUuid, current - amount);
    }

    public void logTransaction(UUID playerUuid, String type, double amount, String description) {
        String sql = "INSERT INTO transaction_history(uuid, type, amount, description) VALUES(?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, type);
            statement.setDouble(3, amount);
            statement.setString(4, description);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to log transaction: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getTopBalance(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT uuid, balance FROM player_balances ORDER BY balance DESC LIMIT ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            ResultSet resultSet = statement.executeQuery();
            int rank = 1;
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("rank", rank++);
                row.put("uuid", resultSet.getString("uuid"));
                row.put("balance", resultSet.getDouble("balance"));
                result.add(row);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to fetch top balances: " + e.getMessage());
        }
        return result;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }
}
