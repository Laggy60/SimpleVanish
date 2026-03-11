package me.laggy.simplevanish.command;

import me.laggy.simplevanish.SimpleVanish;
import me.laggy.simplevanish.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class VanishCommand implements CommandExecutor, TabCompleter {

    private final SimpleVanish plugin;

    public VanishCommand(SimpleVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(plugin, sender, "player-only");
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("simplevanish.use")) {
                MessageUtil.send(plugin, player, "no-permission");
                return true;
            }

            plugin.getVanishManager().toggle(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> {
                if (!player.hasPermission("simplevanish.help")) {
                    MessageUtil.send(plugin, player, "no-permission");
                    return true;
                }

                for (String line : MessageUtil.getList(plugin, "help")) {
                    player.sendMessage(line);
                }
                return true;
            }

            case "list" -> {
                if (!player.hasPermission("simplevanish.list")) {
                    MessageUtil.send(plugin, player, "no-permission");
                    return true;
                }

                Set<String> vanishedNames = plugin.getVanishManager().getVanishedNames();

                if (vanishedNames.isEmpty()) {
                    MessageUtil.send(plugin, player, "list.empty");
                    return true;
                }

                String separator = MessageUtil.color(plugin.getConfig().getString("list.separator", "&7, "));
                String players = vanishedNames.stream()
                        .sorted(Comparator.naturalOrder())
                        .reduce((a, b) -> a + separator + b)
                        .orElse("");

                String message = MessageUtil.get(plugin, "list.header")
                        .replace("%amount%", String.valueOf(vanishedNames.size()))
                        .replace("%players%", players);

                player.sendMessage(message);
                return true;
            }

            case "reload" -> {
                if (!player.hasPermission("simplevanish.reload")) {
                    MessageUtil.send(plugin, player, "no-permission");
                    return true;
                }

                plugin.reloadPlugin();
                MessageUtil.send(plugin, player, "reloaded");
                return true;
            }

            default -> {
                if (!player.hasPermission("simplevanish.help")) {
                    MessageUtil.send(plugin, player, "no-permission");
                    return true;
                }

                for (String line : MessageUtil.getList(plugin, "help")) {
                    player.sendMessage(line);
                }
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            String input = args[0].toLowerCase();

            addIfMatches(sender, suggestions, "help", "simplevanish.help", input);
            addIfMatches(sender, suggestions, "list", "simplevanish.list", input);
            addIfMatches(sender, suggestions, "reload", "simplevanish.reload", input);

            return suggestions;
        }

        return List.of();
    }

    private void addIfMatches(CommandSender sender, List<String> suggestions, String value, String permission, String input) {
        if (sender.hasPermission(permission) && value.startsWith(input)) {
            suggestions.add(value);
        }
    }
}