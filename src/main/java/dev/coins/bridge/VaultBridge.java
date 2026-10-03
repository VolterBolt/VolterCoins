package dev.coins.bridge;

import dev.coins.economy.EconomyManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class VaultBridge implements Economy {

    private final JavaPlugin plugin;
    private final EconomyManager economyManager;

    public VaultBridge(JavaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    public boolean register() {
        try {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (provider == null) {
                Bukkit.getServicesManager().register(Economy.class, this, plugin, org.bukkit.plugin.ServicePriority.Normal);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Vault bridge registration failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "VolterCoins";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public String currencyNamePlural() {
        return "coins";
    }

    @Override
    public String currencyNameSingular() {
        return "coin";
    }

    @Override
    public boolean hasAccount(String playerName) {
        return Bukkit.getOfflinePlayer(playerName).hasPlayedBefore() || Bukkit.getPlayer(playerName) != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return player != null;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return economyManager.getBalance(player.getUniqueId()).doubleValue();
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economyManager.getBalance(player.getUniqueId()).doubleValue();
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Amount cannot be negative");
        }
        if (!economyManager.withdraw(player.getUniqueId(), BigDecimal.valueOf(amount))) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Amount cannot be negative");
        }
        if (!economyManager.withdraw(player.getUniqueId(), BigDecimal.valueOf(amount))) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Amount cannot be negative");
        }
        if (!economyManager.deposit(player.getUniqueId(), BigDecimal.valueOf(amount))) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Deposit failed");
        }
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Amount cannot be negative");
        }
        if (!economyManager.deposit(player.getUniqueId(), BigDecimal.valueOf(amount))) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Deposit failed");
        }
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not implemented");
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount, String reason) {
        return withdrawPlayer(playerName, worldName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount, String reason) {
        return withdrawPlayer(player, worldName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount, String reason) {
        return depositPlayer(playerName, worldName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount, String reason) {
        return depositPlayer(player, worldName, amount);
    }

    @Override
    public boolean hasAccount(UUID playerId) {
        return true;
    }

    @Override
    public double getBalance(UUID playerId) {
        return economyManager.getBalance(playerId).doubleValue();
    }

    @Override
    public boolean has(UUID playerId, double amount) {
        return getBalance(playerId) >= amount;
    }

    @Override
    public boolean withdraw(UUID playerId, double amount) {
        return economyManager.withdraw(playerId, BigDecimal.valueOf(amount));
    }

    @Override
    public boolean deposit(UUID playerId, double amount) {
        return economyManager.deposit(playerId, BigDecimal.valueOf(amount));
    }
}
