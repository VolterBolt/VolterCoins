package dev.coins.listeners;

import dev.coins.VolterCoins;
import dev.coins.economy.EconomyManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final VolterCoins plugin;
    private final EconomyManager economyManager;

    public PlayerListener(VolterCoins plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event == null || event.getPlayer() == null) {
            return;
        }

        economyManager.createAccount(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event == null || event.getPlayer() == null) {
            return;
        }

        plugin.getLogger().fine("Player left: " + event.getPlayer().getName());
    }
}
