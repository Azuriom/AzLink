package com.azuriom.azlink.common.command;

import com.azuriom.azlink.common.AzLinkPlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AzLinkCommand {

    private static final List<String> COMPLETIONS = Arrays.asList("status", "setup", "fetch", "port");

    private final AzLinkPlugin plugin;

    public AzLinkCommand(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("azlink.admin")) {
            String version = this.plugin.getPlatform().getPluginVersion();
            sender.sendMessage("&9AzLink v" + version + "&7. Website: &9https://azuriom.com");
            sender.sendMessage("&cYou don't have the permission to use this command.");
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("setup")) {
            if (args.length < 3) {
                sender.sendMessage("&cUsage: /azlink setup <url> <key>");
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
            this.plugin.fetchNow();
            sender.sendMessage("&6Data has been fetched successfully.");
            return;
        }

        if (args[0].equalsIgnoreCase("port")) {
            if (args.length < 2) {
                sender.sendMessage("&cUsage: /azlink port <port>");
                return;
            }

            int port;

            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("&c'" + args[1] + "' is not a valid port !");
                return;
            }

            if (port < 1 || port > 65535) {
                sender.sendMessage("&cThe port must be between 1 and 65535");
                return;
            }

            this.plugin.getConfig().setHttpPort(port);

            try {
                this.plugin.restartHttpServer();

                sender.sendMessage("&aHTTP server started on port " + port);
            } catch (Exception e) {
                sender.sendMessage("&cAn error occurred while starting the HTTP server: " + e.getMessage() + " - " + e.getClass().getName());
                this.plugin.getLogger().error("Error while starting the HTTP server", e);
                return;
            }

            saveConfig(sender);

            return;
        }

        sendUsage(sender);
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("azlink.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return COMPLETIONS.stream()
                    .filter(s -> startsWithIgnoreCase(args[0], s))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public String getUsage() {
        return "Usage: /azlink [" + String.join("|", COMPLETIONS) + "]";
    }

    private void sendUsage(CommandSender sender) {
        String version = this.plugin.getPlatform().getPluginVersion();
        sender.sendMessage("&9AzLink v" + version + "&7. Website: &9https://azuriom.com");
        sender.sendMessage("&8- /azlink setup <url> <key>");
        sender.sendMessage("&8- /azlink port <port>");
        sender.sendMessage("&8- /azlink status");
    }

    private void setup(CommandSender sender, String url, String key) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        this.plugin.getConfig().setSiteKey(key);
        this.plugin.getConfig().setSiteUrl(url);

        if (showStatus(sender)) {
            if (startsWithIgnoreCase(url, "http://")) {
                sender.sendMessage("&6You should use https to improve security.");
            }

            saveConfig(sender);

            this.plugin.restartHttpServer();
        }
    }

    private void saveConfig(CommandSender sender) {
        try {
            this.plugin.saveConfig();
        } catch (IOException e) {
            sender.sendMessage("&cAn error occurred while saving config: " + e.getMessage() + " - " + e.getClass().getName());
            this.plugin.getLogger().error("Error while saving config", e);
        }
    }

    private boolean showStatus(CommandSender sender) {
        try {
            this.plugin.getHttpClient().verifyStatus();

            sender.sendMessage("&aLinked to the website successfully.");

            this.plugin.fetchNow();

            return true;
        } catch (Exception e) {
            sender.sendMessage("&cUnable to connect to the website: " + e.getMessage() + " - " + e.getClass().getName());

            return false;
        }
    }

    private static boolean startsWithIgnoreCase(String string, String prefix) {
        if (string.length() < prefix.length()) {
            return false;
        }

        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
