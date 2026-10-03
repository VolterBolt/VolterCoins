package dev.coins.api;

import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.UUID;

public interface VolterCoinsAPI {

    String getPluginName();

    BigDecimal getBalance(UUID playerId);

    BigDecimal getBalance(OfflinePlayer player);

    boolean hasAccount(UUID playerId);

    boolean createAccount(UUID playerId);

    boolean deposit(UUID playerId, BigDecimal amount);

    boolean withdraw(UUID playerId, BigDecimal amount);

    boolean setBalance(UUID playerId, BigDecimal amount);

    boolean transfer(UUID fromPlayerId, UUID toPlayerId, BigDecimal amount);

    boolean has(UUID playerId, BigDecimal amount);

    int getRank(UUID playerId);

    BigDecimal getTopBalance(int rank);

    String format(BigDecimal amount);

    default double getBalanceDouble(UUID playerId) {
        return getBalance(playerId).doubleValue();
    }

    default boolean addBalance(UUID playerId, BigDecimal amount) {
        return deposit(playerId, amount);
    }

    default boolean removeBalance(UUID playerId, BigDecimal amount) {
        return withdraw(playerId, amount);
    }
}
