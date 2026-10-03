package dev.coins.placeholders;

import dev.coins.economy.EconomyManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.UUID;

public class VolterCoinsExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final EconomyManager economyManager;

    public VolterCoinsExpansion(JavaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @Override
    public String getIdentifier() {
        return "voltercoin";
    }

    @Override
    public String getAuthor() {
        return "VolterBolt";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || params == null) {
            return "";
        }

        UUID uuid = player.getUniqueId();

        switch (params.toLowerCase()) {
            case "balance":
                return economyManager.getBalance(uuid).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
            case "balance_formatted":
                return economyManager.getBalance(uuid).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " coins";
            case "rank":
                return String.valueOf(economyManager.getRank(uuid));
            case "currency_symbol":
                return "ⓒ";
            case "total_economy":
                return "0";
            default:
                if (params.toLowerCase().startsWith("top_")) {
                    String suffix = params.substring(4);
                    try {
                        int position = Integer.parseInt(suffix);
                        if (position <= 0 || position > 10) {
                            return "0";
                        }
                        return economyManager.getTopBalances(10).stream()
                            .filter(entry -> (int) entry.getOrDefault("rank", 0) == position)
                            .findFirst()
                            .map(entry -> entry.getOrDefault("balance", "0").toString())
                            .orElse("0");
                    } catch (NumberFormatException ignored) {
                        return "0";
                    }
                }

                if (params.toLowerCase().startsWith("top_player_")) {
                    String suffix = params.substring(11);
                    try {
                        int position = Integer.parseInt(suffix);
                        if (position <= 0 || position > 10) {
                            return "0";
                        }
                        return economyManager.getTopBalances(10).stream()
                            .filter(entry -> (int) entry.getOrDefault("rank", 0) == position)
                            .findFirst()
                            .map(entry -> (String) entry.getOrDefault("uuid", ""))
                            .orElse("0");
                    } catch (NumberFormatException ignored) {
                        return "0";
                    }
                }
                break;
        }

        return "";
    }
}
