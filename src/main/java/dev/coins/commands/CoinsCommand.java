package dev.coins.commands;

import dev.coins.VolterCoins;
import dev.coins.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CoinsCommand implements CommandExecutor {

    private final VolterCoins plugin;
    private final EconomyManager economyManager;

    public CoinsCommand(VolterCoins plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§6VolterCoins §7- Economy commands");
            player.sendMessage("§7Usage:");
            player.sendMessage("§e/coins help §7- Show commands");
            player.sendMessage("§e/balance [player] §7- View balance");
            player.sendMessage("§e/pay <player> <amount> §7- Send money");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> printHelp(player);
            case "reload" -> {
                if (!player.hasPermission("voltercoin.admin.reload")) {
                    player.sendMessage("§cNo permission.");
                    return true;
                }
                plugin.reloadEconomy(player);
            }
            case "top" -> {
                if (!player.hasPermission("voltercoin.top")) {
                    player.sendMessage("§cNo permission.");
                    return true;
                }
                player.sendMessage("§eTop balances:");
                economyManager.getTopBalances(10).forEach(entry -> {
                    String uuid = (String) entry.get("uuid");
                    String name = Bukkit.getOfflinePlayer(java.util.UUID.fromString(uuid)).getName();
                    if (name == null) {
                        name = uuid;
                    }
                    player.sendMessage("§7#" + entry.get("rank") + " §f" + name + " §8- §6" + entry.get("balance"));
                });
            }
            default -> printHelp(player);
        }

        return true;
    }

    private void printHelp(Player player) {
        player.sendMessage("§6VolterCoins Commands");
        player.sendMessage("§7/balance [player]");
        player.sendMessage("§7/pay <player> <amount>");
        player.sendMessage("§7/coins help");
        player.sendMessage("§7/coins top");
        player.sendMessage("§7/coins reload");
    }
}
