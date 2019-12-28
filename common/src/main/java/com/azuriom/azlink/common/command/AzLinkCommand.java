package com.azuriom.azlink.common.command;

import com.azuriom.azlink.common.AzLinkPlugin;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AzLinkCommand {

    private static final String[] COMPLETIONS = {"status", "setup", "fetch", "port"};

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

        if (args[0].equalsIgnoreCase("port")) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /azlink port <port>");
                return;
            }

            int port;

            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c'" + args[1] + "' is not a valid port !");
                return;
            }

            if (port < 1 || port > 65535) {
                sender.sendMessage("§cThe port must be between 1 and 65535");
                return;
            }

            plugin.getConfig().setHttpPort(port);

            plugin.getPlatform().executeAsync(() -> {
                try {
                    plugin.restartHttpServer();

                    sender.sendMessage("§aHTTP server started on port " + port);
                } catch (Exception e) {
                    sender.sendMessage("§cAn error occurred while starting the HTTP server: " + e.getMessage() + " - " + e.getClass().getName());
                    plugin.getLogger().error("Error while starting the HTTP server", e);
                }
            });

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

    public String getUsage() {
        return "Usage: /azlink [" + String.join("|", COMPLETIONS) + "]";
    }

    private void sendUsage(CommandSender sender) {
        String version = plugin.getPlatform().getPluginVersion();
        sender.sendMessage("§9AzLink v" + version + "§7. Website: §9https://azuriom.com");
        sender.sendMessage("§8- /azlink setup <url> <key>");
        sender.sendMessage("§8- /azlink port <port>");
        sender.sendMessage("§8- /azlink status");
    }

    private void setup(CommandSender sender, String url, String key) {
        plugin.getConfig().setSiteKey(key);
        plugin.getConfig().setSiteUrl(url);

        if (showStatus(sender)) {
            try {
                plugin.saveConfig();
            } catch (IOException e) {
                sender.sendMessage("§cAn error occurred while saving config: " + e.getMessage() + " - " + e.getClass().getName());
                plugin.getLogger().error("Error while saving config", e);
            }
        }
    }

    private boolean showStatus(CommandSender sender) {
        try {
            plugin.getHttpClient().verifyStatus();

            sender.sendMessage("§aStatus ok");

            plugin.fetchNow();

            return true;
        } catch (Exception e) {
            sender.sendMessage("§cUnable to connect to the website: " + e.getMessage() + " - " + e.getClass().getName());

            return false;
        }
    }

    private static boolean startsWithIgnoreCase(String string, String prefix) {
        return string.length() >= prefix.length() && string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
