package me.laggy.simplevanish.manager;

import me.laggy.simplevanish.SimpleVanish;
import me.laggy.simplevanish.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class VanishManager {

    private final SimpleVanish plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishManager(SimpleVanish plugin) {
        this.plugin = plugin;
    }

    public boolean toggle(Player player) {
        if (isVanished(player)) {
            disableVanish(player);
            return false;
        }

        enableVanish(player);
        return true;
    }

    public void enableVanish(Player player) {
        if (isVanished(player)) {
            return;
        }

        vanishedPlayers.add(player.getUniqueId());
        applyVisibility(player);
        sendFakeQuit(player);

        MessageUtil.send(plugin, player, "vanish-enabled");
        sendToggleActionbar(player, true);
        playConfiguredSound(player, true);
    }

    public void disableVanish(Player player) {
        if (!isVanished(player)) {
            return;
        }

        vanishedPlayers.remove(player.getUniqueId());
        removeVisibility(player);
        sendFakeJoin(player);

        MessageUtil.send(plugin, player, "vanish-disabled");
        sendToggleActionbar(player, false);
        playConfiguredSound(player, false);
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }

    public Set<String> getVanishedNames() {
        return vanishedPlayers.stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .map(Player::getName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void applyVisibility(Player vanished) {
        boolean hideFromSeePermission = plugin.getConfig()
                .getBoolean("settings.hide-from-players-with-see-permission", false);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(vanished.getUniqueId())) {
                continue;
            }

            if (!hideFromSeePermission && online.hasPermission("simplevanish.see")) {
                continue;
            }

            online.hidePlayer(plugin, vanished);
        }
    }

    public void removeVisibility(Player vanished) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(vanished.getUniqueId())) {
                continue;
            }

            online.showPlayer(plugin, vanished);
        }
    }

    public void handleJoin(Player joinedPlayer) {
        for (UUID uuid : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(uuid);

            if (vanished == null || !vanished.isOnline()) {
                continue;
            }

            if (joinedPlayer.getUniqueId().equals(vanished.getUniqueId())) {
                continue;
            }

            boolean hideFromSeePermission = plugin.getConfig()
                    .getBoolean("settings.hide-from-players-with-see-permission", false);

            if (!hideFromSeePermission && joinedPlayer.hasPermission("simplevanish.see")) {
                continue;
            }

            joinedPlayer.hidePlayer(plugin, vanished);
        }

        if (isVanished(joinedPlayer)) {
            applyVisibility(joinedPlayer);
        }
    }

    public void handleQuit(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
    }

    public void refreshAllVisibility() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            for (UUID uuid : vanishedPlayers) {
                Player vanished = Bukkit.getPlayer(uuid);

                if (vanished == null || !vanished.isOnline()) {
                    continue;
                }

                if (online.getUniqueId().equals(vanished.getUniqueId())) {
                    continue;
                }

                boolean hideFromSeePermission = plugin.getConfig()
                        .getBoolean("settings.hide-from-players-with-see-permission", false);

                if (!hideFromSeePermission && online.hasPermission("simplevanish.see")) {
                    online.showPlayer(plugin, vanished);
                } else {
                    online.hidePlayer(plugin, vanished);
                }
            }
        }
    }

    public void clearAll() {
        for (UUID uuid : new HashSet<>(vanishedPlayers)) {
            Player vanished = Bukkit.getPlayer(uuid);

            if (vanished != null && vanished.isOnline()) {
                removeVisibility(vanished);
            }
        }

        vanishedPlayers.clear();
    }

    private void sendFakeQuit(Player player) {
        if (!plugin.getConfig().getBoolean("settings.fake-quit-message-on-vanish", true)) {
            return;
        }

        String message = MessageUtil.getRaw(plugin, "fake-quit")
                .replace("%player%", player.getName());

        Bukkit.broadcastMessage(message);
    }

    private void sendFakeJoin(Player player) {
        if (!plugin.getConfig().getBoolean("settings.fake-join-message-on-unvanish", true)) {
            return;
        }

        String message = MessageUtil.getRaw(plugin, "fake-join")
                .replace("%player%", player.getName());

        Bukkit.broadcastMessage(message);
    }

    private void sendToggleActionbar(Player player, boolean enabled) {
        if (!plugin.getConfig().getBoolean("settings.send-actionbar-on-toggle", true)) {
            return;
        }

        MessageUtil.sendActionbar(plugin, player, enabled ? "actionbar-enabled" : "actionbar-disabled");
    }

    @SuppressWarnings("deprecation")
    private void playConfiguredSound(Player player, boolean enabled) {
        if (!plugin.getConfig().getBoolean("sounds.enabled", true)) {
            return;
        }

        String path = enabled ? "sounds.vanish" : "sounds.unvanish";
        String soundName = plugin.getConfig().getString(path, "ENTITY_EXPERIENCE_ORB_PICKUP");

        if (soundName == null || soundName.isBlank()) {
            return;
        }

        try {
            Sound sound = Sound.valueOf(soundName.trim().toUpperCase());
            float volume = (float) plugin.getConfig().getDouble("sounds.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("sounds.pitch", 1.0);

            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception ignored) {
            plugin.getLogger().warning("Invalid sound in config.yml: " + soundName);
        }
    }
}