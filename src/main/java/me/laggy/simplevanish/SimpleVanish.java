package me.laggy.simplevanish;

import me.laggy.simplevanish.command.VanishCommand;
import me.laggy.simplevanish.config.ConfigHandler;
import me.laggy.simplevanish.listener.PlayerJoinListener;
import me.laggy.simplevanish.listener.PlayerQuitListener;
import me.laggy.simplevanish.manager.VanishManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SimpleVanish extends JavaPlugin {

    private ConfigHandler configHandler;
    private VanishManager vanishManager;

    @Override
    public void onEnable() {
        this.configHandler = new ConfigHandler(this);
        this.configHandler.load();

        this.vanishManager = new VanishManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("SimpleVanish has been enabled.");
    }

    @Override
    public void onDisable() {
        if (vanishManager != null) {
            vanishManager.clearAll();
        }

        getLogger().info("SimpleVanish has been disabled.");
    }

    private void registerCommands() {
        VanishCommand vanishCommand = new VanishCommand(this);

        PluginCommand command = Objects.requireNonNull(getCommand("vanish"), "Command 'vanish' not found in plugin.yml");
        command.setExecutor(vanishCommand);
        command.setTabCompleter(vanishCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
    }

    public void reloadPlugin() {
        boolean resetOnReload = getConfig().getBoolean("settings.vanish-state-reset-on-reload", false);

        if (resetOnReload) {
            vanishManager.clearAll();
        }

        reloadConfig();
        configHandler.reloadMessages();

        if (!resetOnReload) {
            vanishManager.refreshAllVisibility();
        }
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }
}