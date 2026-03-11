package me.laggy.simplevanish.listener;

import me.laggy.simplevanish.SimpleVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final SimpleVanish plugin;

    public PlayerJoinListener(SimpleVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getVanishManager().handleJoin(player);

        if (plugin.getVanishManager().isVanished(player)) {
            event.joinMessage(null);
        }
    }
}