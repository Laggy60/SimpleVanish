package me.laggy.simplevanish.util;

import me.laggy.simplevanish.SimpleVanish;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class MessageUtil {

    private MessageUtil() {
    }

    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }

    public static String prefix(SimpleVanish plugin) {
        return color(plugin.getConfig().getString("format.prefix", "&8[&bSimplevanish&8] &7"));
    }

    public static String get(SimpleVanish plugin, String path) {
        FileConfiguration messages = plugin.getConfigHandler().getMessages();
        String raw = messages.getString("messages." + path, "&cMissing message: " + path);
        return color(prefix(plugin) + raw);
    }

    public static String getRaw(SimpleVanish plugin, String path) {
        FileConfiguration messages = plugin.getConfigHandler().getMessages();
        return color(messages.getString("messages." + path, "&cMissing message: " + path));
    }

    public static List<String> getList(SimpleVanish plugin, String path) {
        FileConfiguration messages = plugin.getConfigHandler().getMessages();
        List<String> raw = messages.getStringList("messages." + path);
        List<String> colored = new ArrayList<>();

        for (String line : raw) {
            colored.add(color(line));
        }

        return colored;
    }

    public static void send(SimpleVanish plugin, CommandSender sender, String path) {
        sender.sendMessage(get(plugin, path));
    }

    public static void sendActionbar(SimpleVanish plugin, Player player, String path) {
        String message = plugin.getConfigHandler().getMessages().getString("messages." + path, "");
        player.sendActionBar(color(message));
    }
}