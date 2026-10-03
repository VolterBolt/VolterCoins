package dev.coins.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EconomyProvider {

    String getName();

    BigDecimal getBalance(UUID playerId);

    BigDecimal getBalance(OfflinePlayer player);

    boolean hasAccount(UUID playerId);

    boolean createAccount(UUID playerId);

    boolean deposit(UUID playerId, BigDecimal amount);

    boolean withdraw(UUID playerId, BigDecimal amount);

    boolean setBalance(UUID playerId, BigDecimal amount);

    boolean transfer(UUID fromPlayerId, UUID toPlayerId, BigDecimal amount);

    boolean has(UUID playerId, BigDecimal amount);

    List<Map<String, Object>> getTopBalances(int limit);

    Map<String, Object> getPlayerStats(UUID playerId);

    default String format(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }

    default BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    default double getBalanceDouble(UUID playerId) {
        return getBalance(playerId).doubleValue();
    }

    default double getBalanceDouble(OfflinePlayer player) {
        return getBalance(player).doubleValue();
    }

    default boolean addBalance(UUID playerId, BigDecimal amount) {
        return deposit(playerId, amount);
    }

    default boolean removeBalance(UUID playerId, BigDecimal amount) {
        return withdraw(playerId, amount);
    }

    default String getDisplayName(Player player) {
        return player != null ? player.getName() : "Unknown";
    }
}
