package dev.coins.commands;

import dev.coins.VolterCoins;
import dev.coins.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public class CoinsAdminCommand implements CommandExecutor {

    private final VolterCoins plugin;
    private final EconomyManager economyManager;

    public CoinsAdminCommand(VolterCoins plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("voltercoin.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            printHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "give" -> {
                if (!sender.hasPermission("voltercoin.admin.give")) {
                    sender.sendMessage("§cYou do not have permission to give coins.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /coinsadmin give <player> <amount>");
                    return true;
                }
                handleGive(sender, args[1], args[2]);
            }
            case "take" -> {
                if (!sender.hasPermission("voltercoin.admin.take")) {
                    sender.sendMessage("§cYou do not have permission to take coins.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /coinsadmin take <player> <amount>");
                    return true;
                }
                handleTake(sender, args[1], args[2]);
            }
            case "set" -> {
                if (!sender.hasPermission("voltercoin.admin.set")) {
                    sender.sendMessage("§cYou do not have permission to set balances.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /coinsadmin set <player> <amount>");
                    return true;
                }
                handleSet(sender, args[1], args[2]);
            }
            case "reset" -> {
                if (!sender.hasPermission("voltercoin.admin.reset")) {
                    sender.sendMessage("§cYou do not have permission to reset balances.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /coinsadmin reset <player>");
                    return true;
                }
                handleReset(sender, args[1]);
            }
            case "check" -> {
                if (!sender.hasPermission("voltercoin.admin.check")) {
                    sender.sendMessage("§cYou do not have permission to check balances.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /coinsadmin check <player>");
                    return true;
                }
                handleCheck(sender, args[1]);
            }
            case "help" -> printHelp(sender);
            default -> sender.sendMessage("§cUnknown subcommand. Use /coinsadmin help");
        }

        return true;
    }

    private void handleGive(CommandSender sender, String playerName, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                sender.sendMessage("§cAmount must be positive.");
                return;
            }

            var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            UUID uuid = offlinePlayer.getUniqueId();

            economyManager.deposit(uuid, BigDecimal.valueOf(amount));
            sender.sendMessage("§aGave §6" + amount + " coins §ato §6" + playerName);

            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                onlinePlayer.sendMessage("§aAn admin gave you §6" + amount + " coins");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount.");
        }
    }

    private void handleTake(CommandSender sender, String playerName, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                sender.sendMessage("§cAmount must be positive.");
                return;
            }

            var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            UUID uuid = offlinePlayer.getUniqueId();

            if (!economyManager.withdraw(uuid, BigDecimal.valueOf(amount))) {
                sender.sendMessage("§cPlayer does not have enough coins.");
                return;
            }

            sender.sendMessage("§aTook §6" + amount + " coins §afrom §6" + playerName);

            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                onlinePlayer.sendMessage("§cAn admin took §6" + amount + " coins §cfrom you");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount.");
        }
    }

    private void handleSet(CommandSender sender, String playerName, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 0) {
                sender.sendMessage("§cAmount cannot be negative.");
                return;
            }

            var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            UUID uuid = offlinePlayer.getUniqueId();

            economyManager.setBalance(uuid, BigDecimal.valueOf(amount));
            sender.sendMessage("§aSet §6" + playerName + "'s §abalance to §6" + amount);

            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                onlinePlayer.sendMessage("§cAn admin set your balance to §6" + amount + " coins");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount.");
        }
    }

    private void handleReset(CommandSender sender, String playerName) {
        var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = offlinePlayer.getUniqueId();

        economyManager.setBalance(uuid, BigDecimal.ZERO);
        sender.sendMessage("§aReset §6" + playerName + "'s §abalance to 0");

        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            onlinePlayer.sendMessage("§cAn admin reset your balance to 0");
        }
    }

    private void handleCheck(CommandSender sender, String playerName) {
        var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = offlinePlayer.getUniqueId();

        BigDecimal balance = economyManager.getBalance(uuid);
        int rank = economyManager.getRank(uuid);

        sender.sendMessage("§6" + playerName + "'s Stats:");
        sender.sendMessage("§7Balance: §a" + balance.setScale(2, java.math.RoundingMode.HALF_UP));
        sender.sendMessage("§7Rank: §a#" + rank);
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage("§6VolterCoins Admin Commands");
        sender.sendMessage("§e/coinsadmin give <player> <amount> §7(§6voltercoin.admin.give§7)");
        sender.sendMessage("§e/coinsadmin take <player> <amount> §7(§6voltercoin.admin.take§7)");
        sender.sendMessage("§e/coinsadmin set <player> <amount> §7(§6voltercoin.admin.set§7)");
        sender.sendMessage("§e/coinsadmin reset <player> §7(§6voltercoin.admin.reset§7)");
        sender.sendMessage("§e/coinsadmin check <player> §7(§6voltercoin.admin.check§7)");
        sender.sendMessage("§e/coinsadmin help §7(§6voltercoin.admin§7)");
    }
}
