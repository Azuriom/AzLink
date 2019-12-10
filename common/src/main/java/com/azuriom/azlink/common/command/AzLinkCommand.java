package com.azuriom.azlink.common.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.config.PluginConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AzLinkCommand {

    private static final String[] COMPLETIONS = {"status", "setup", "key", "site"};

    private final AzLinkPlugin plugin;

    public AzLinkCommand(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || !sender.hasPermission("azlink.admin")) {
            sendUsage(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("setup")) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /azlink setup <url> <key>");
                return;
            }

            setup(sender, args[1], args[2]);

            return;
        }

        if (args[0].equalsIgnoreCase("status")) {

            showStatus(sender);

            return;
        }

        if (args[0].equalsIgnoreCase("fetch")) {

            plugin.fetchNow();

            sender.sendMessage("§6Fetch done.");

            return;
        }

        sendUsage(sender);
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("azlink.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Stream.of(COMPLETIONS).filter(s -> startsWithIgnoreCase(args[0], s)).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private void sendUsage(CommandSender sender) {
        String version = plugin.getPlatform().getPluginVersion();
        sender.sendMessage("§9AzLink v" + version + "§7. Website: §9https://azuriom.com");
        sender.sendMessage("§8- /azlink setup <url> <key>");
        sender.sendMessage("§8- /azlink status");
    }

    private void setup(CommandSender sender, String url, String key) {
        plugin.setConfig(new PluginConfig(key, url));

        if (showStatus(sender)) {
            try {
                plugin.saveConfig();
            } catch (IOException e) {
                sender.sendMessage("§cError while saving config: " + e.getMessage() + " - " + e.getClass().getName());
                plugin.getPlatform().getLoggerAdapter().error("Error while saving config", e);
            }
        }
    }

    private boolean showStatus(CommandSender sender) {
        try {
            plugin.getHttpClient().verifyStatus();

            sender.sendMessage("§aStatus ok");

            plugin.fetchNow();

            return true;
        } catch (IOException e) {
            sender.sendMessage("§cUnable to connect to the server: " + e.getMessage());

            return false;
        }
    }

    private static boolean startsWithIgnoreCase(String string, String prefix) {
        return string.length() >= prefix.length() && string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
