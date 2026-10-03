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

public class BalanceCommand implements CommandExecutor {

    private final VolterCoins plugin;
    private final EconomyManager economyManager;

    public BalanceCommand(VolterCoins plugin) {
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
            BigDecimal balance = economyManager.getBalance(player.getUniqueId());
            player.sendMessage("§aYour balance: §6" + balance.setScale(2, java.math.RoundingMode.HALF_UP));
            return true;
        }

        if (!player.hasPermission("voltercoin.balance.check.others")) {
            player.sendMessage("§cYou do not have permission to view other balances.");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        BigDecimal balance = economyManager.getBalance(target.getUniqueId());
        player.sendMessage("§a" + target.getName() + "'s balance: §6" + balance.setScale(2, java.math.RoundingMode.HALF_UP));
        return true;
    }
}
