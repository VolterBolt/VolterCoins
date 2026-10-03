package dev.coins;

import dev.coins.bridge.VaultBridge;
import dev.coins.commands.BalanceCommand;
import dev.coins.commands.CoinsAdminCommand;
import dev.coins.commands.CoinsCommand;
import dev.coins.commands.PayCommand;
import dev.coins.database.DatabaseManager;
import dev.coins.economy.EconomyManager;
import dev.coins.placeholders.VolterCoinsExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class VolterCoins extends JavaPlugin {

    private static VolterCoins instance;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private VaultBridge vaultBridge;
    private VolterCoinsExpansion placeholderExpansion;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            getLogger().info("Database initialized successfully.");
        } catch (SQLException e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        economyManager = new EconomyManager(this, databaseManager);

        registerCommands();
        registerPlaceholders();
        registerVaultBridge();

        getLogger().info("VolterCoins enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("VolterCoins disabled.");
    }

    private void registerCommands() {
        PluginCommand balanceCommand = getCommand("balance");
        if (balanceCommand != null) {
            balanceCommand.setExecutor(new BalanceCommand(this));
        }

        PluginCommand payCommand = getCommand("pay");
        if (payCommand != null) {
            payCommand.setExecutor(new PayCommand(this));
        }

        PluginCommand coinsCommand = getCommand("coins");
        if (coinsCommand != null) {
            coinsCommand.setExecutor(new CoinsCommand(this));
        }

        PluginCommand coinsAdminCommand = getCommand("coinsadmin");
        if (coinsAdminCommand != null) {
            coinsAdminCommand.setExecutor(new CoinsAdminCommand(this));
        }
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new VolterCoinsExpansion(this, economyManager);
            placeholderExpansion.register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }
    }

    private void registerVaultBridge() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            vaultBridge = new VaultBridge(this, economyManager);
            vaultBridge.register();
            getLogger().info("Vault bridge registered.");
        }
    }

    public static VolterCoins getInstance() {
        return instance;
    }

    public DatabaseManager getDatabase() {
        return databaseManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public void reloadEconomy() {
        reloadConfig();
        if (economyManager != null) {
            economyManager.reload();
        }
    }

    public void reloadEconomy(org.bukkit.entity.Player player) {
        reloadEconomy();
        if (player != null) {
            player.sendMessage("§aVolterCoins reloaded successfully.");
        }
    }
}
