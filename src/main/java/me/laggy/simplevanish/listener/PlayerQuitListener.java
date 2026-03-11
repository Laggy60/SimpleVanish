package me.laggy.simplevanish.listener;

import me.laggy.simplevanish.SimpleVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final SimpleVanish plugin;

    public PlayerQuitListener(SimpleVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getVanishManager().isVanished(player)) {
            event.quitMessage(null);
        }

        plugin.getVanishManager().handleQuit(player);
    }
}