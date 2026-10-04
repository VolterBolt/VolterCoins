package dev.coins.economy;

import dev.coins.api.EconomyProvider;
import dev.coins.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EconomyManager implements EconomyProvider {

    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;

    public EconomyManager(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public String getName() {
        return "VolterCoins";
    }

    @Override
    public BigDecimal getBalance(UUID playerId) {
        return BigDecimal.valueOf(databaseManager.getBalance(playerId));
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        if (player == null) {
            return BigDecimal.ZERO;
        }
        return getBalance(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(UUID playerId) {
        return databaseManager.hasAccount(playerId);
    }

    @Override
    public boolean createAccount(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        if (hasAccount(playerId)) {
            return true;
        }

        databaseManager.setBalance(playerId, 0.0);
        return true;
    }

    @Override
    public boolean deposit(UUID playerId, BigDecimal amount) {
        if (amount == null || amount.doubleValue() <= 0) {
            return false;
        }

        double current = databaseManager.getBalance(playerId);
        databaseManager.setBalance(playerId, current + amount.doubleValue());
        databaseManager.logTransaction(playerId, "DEPOSIT", amount.doubleValue(), "Deposit");
        return true;
    }

    @Override
    public boolean withdraw(UUID playerId, BigDecimal amount) {
        if (amount == null || amount.doubleValue() <= 0) {
            return false;
        }

        double current = databaseManager.getBalance(playerId);
        if (current < amount.doubleValue()) {
            return false;
        }

        databaseManager.setBalance(playerId, current - amount.doubleValue());
        databaseManager.logTransaction(playerId, "WITHDRAW", amount.doubleValue(), "Withdraw");
        return true;
    }

    @Override
    public boolean setBalance(UUID playerId, BigDecimal amount) {
        if (amount == null) {
            return false;
        }

        databaseManager.setBalance(playerId, amount.doubleValue());
        databaseManager.logTransaction(playerId, "SET", amount.doubleValue(), "Set balance");
        return true;
    }

    @Override
    public boolean transfer(UUID fromPlayerId, UUID toPlayerId, BigDecimal amount) {
        if (fromPlayerId == null || toPlayerId == null || amount == null || amount.doubleValue() <= 0) {
            return false;
        }

        if (!withdraw(fromPlayerId, amount)) {
            return false;
        }

        if (!deposit(toPlayerId, amount)) {
            deposit(fromPlayerId, amount);
            return false;
        }

        databaseManager.logTransaction(fromPlayerId, "TRANSFER_OUT", amount.doubleValue(), "Sent to " + toPlayerId);
        databaseManager.logTransaction(toPlayerId, "TRANSFER_IN", amount.doubleValue(), "Received from " + fromPlayerId);
        return true;
    }

    @Override
    public boolean has(UUID playerId, BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return getBalance(playerId).compareTo(amount) >= 0;
    }

    @Override
    public List<Map<String, Object>> getTopBalances(int limit) {
        return databaseManager.getTopBalance(limit);
    }

    @Override
    public Map<String, Object> getPlayerStats(UUID playerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("uuid", playerId.toString());
        stats.put("balance", getBalance(playerId));
        stats.put("rank", getRank(playerId));
        return stats;
    }

    public int getRank(UUID playerId) {
        List<Map<String, Object>> top = getTopBalances(1000);
        for (int i = 0; i < top.size(); i++) {
            String uuid = (String) top.get(i).get("uuid");
            if (uuid.equals(playerId.toString())) {
                return i + 1;
            }
        }
        return top.size() + 1;
    }

    public void saveAll() {
        plugin.getLogger().fine("Saving economy data...");
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public void shutdown() {
        saveAll();
    }

    public Player getPlayer(UUID playerId) {
        return Bukkit.getPlayer(playerId);
    }

    public OfflinePlayer getOfflinePlayer(UUID playerId) {
        return Bukkit.getOfflinePlayer(playerId);
    }
}
