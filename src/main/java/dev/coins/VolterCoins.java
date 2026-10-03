package dev.coins;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import dev.coins.commands.BalanceCommand;
import dev.coins.commands.PayCommand;
import dev.coins.commands.CoinsAdminCommand;
import dev.coins.economy.CoinsEconomy;
import dev.coins.database.DatabaseManager;

/**
 * VolterCoins - Minecraft Economy Plugin for Paper 1.20.4
 * Compatible with Volter_Shop through Vault API integration
 */
public class VolterCoins extends JavaPlugin {

    private static VolterCoins instance;
    private CoinsEconomy economy;
    private DatabaseManager database;

    @Override
    public void onEnable() {
        instance = this;

        // Log startup
        getLogger().info("====================================");
        getLogger().info("VolterCoins v" + getDescription().getVersion());
        getLogger().info("A Minecraft Economy Plugin for 1.20.4");
        getLogger().info("====================================");

        // Save default config
        saveDefaultConfig();

        // Initialize database
        try {
            database = new DatabaseManager(this);
            database.initialize();
            getLogger().info("Database initialized successfully.");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize economy
        economy = new CoinsEconomy(this, database);
        getLogger().info("Economy initialized: " + economy.getName());

        // Register with Vault
        if (!registerVault()) {
            getLogger().warning("Vault not found. Economy will run in standalone mode.");
        } else {
            getLogger().info("Successfully registered with Vault!");
            getLogger().info("Volter_Shop can now use VolterCoins as its economy provider.");
        }

        // Register commands
        getCommand("coins").setExecutor(new CoinsCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("coinsadmin").setExecutor(new CoinsAdminCommand(this));

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        // Register PlaceholderAPI expansion (if available)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CoinsPlaceholder(this).register();
            getLogger().info("PlaceholderAPI integration enabled.");
        }

        getLogger().info("VolterCoins successfully enabled!");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
        getLogger().info("VolterCoins disabled.");
    }

    /**
     * Register economy provider with Vault
     */
    private boolean registerVault() {
        try {
            RegisteredServiceProvider<Economy> rsp = 
                Bukkit.getServicesManager().getRegistration(Economy.class);
            
            if (rsp == null) {
                Bukkit.getServicesManager().register(
                    Economy.class, 
                    economy, 
                    this, 
                    org.bukkit.plugin.ServicePriority.Normal
                );
                return true;
            }
        } catch (Exception e) {
            getLogger().severe("Error registering with Vault: " + e.getMessage());
        }
        return false;
    }

    public static VolterCoins getInstance() {
        return instance;
    }

    public CoinsEconomy getEconomy() {
        return economy;
    }

    public DatabaseManager getDatabase() {
        return database;
    }
}
