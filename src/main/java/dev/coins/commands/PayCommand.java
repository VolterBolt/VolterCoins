package dev.coins.commands;

import dev.coins.VolterCoins;
import dev.coins.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PayCommand implements CommandExecutor {

    private final VolterCoins plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, Long> cooldowns;

    public PayCommand(VolterCoins plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.cooldowns = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("voltercoin.pay.send")) {
            player.sendMessage("§cYou do not have permission to send money.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot pay yourself.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage("§cAmount must be positive.");
            return true;
        }

        if (!player.hasPermission("voltercoin.bypass.cooldown")) {
            long cooldown = plugin.getConfig().getLong("transactions.pay-cooldown-seconds", 5) * 1000L;
            long lastPay = cooldowns.getOrDefault(player.getUniqueId(), 0L);
            if (System.currentTimeMillis() - lastPay < cooldown) {
                long remaining = (cooldown - (System.currentTimeMillis() - lastPay)) / 1000;
                player.sendMessage("§cYou must wait §6" + remaining + "s §cbefore paying again.");
                return true;
            }
        }

        if (!economyManager.has(player.getUniqueId(), BigDecimal.valueOf(amount))) {
            player.sendMessage("§cInsufficient funds.");
            return true;
        }

        if (economyManager.transfer(player.getUniqueId(), target.getUniqueId(), BigDecimal.valueOf(amount))) {
            player.sendMessage("§aYou sent §6" + amount + " coins §ato §6" + target.getName());
            target.sendMessage("§aYou received §6" + amount + " coins §afrom §6" + player.getName());
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            player.sendMessage("§cTransaction failed.");
        }

        return true;
    }
}
