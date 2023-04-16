package com.azuriom.azlink.common.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.data.UserInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AzLinkCommand {

    private static final List<String> COMPLETIONS = Arrays.asList("status", "setup", "fetch", "money", "port");
    private static final List<String> MONEY_ACTIONS = Arrays.asList("add", "remove", "set");

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

            plugin.getScheduler().executeAsync(() -> setup(sender, args[1], args[2]));
            return;
        }

        if (args[0].equalsIgnoreCase("status")) {
            showStatus(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("money")) {
            try {
                editMoney(sender, args);
            } catch (NumberFormatException e) {
                sender.sendMessage("&c'" + args[3] + "' is not a valid number !");
            }
            return;
        }

        if (args[0].equalsIgnoreCase("fetch")) {
            this.plugin.fetch()
                    .thenRun(() -> sender.sendMessage("&6Data has been fetched successfully."))
                    .exceptionally(ex -> {
                        sender.sendMessage("&cUnable to fetch data: " + ex.getMessage());

                        return null;
                    });
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
                String info = e.getMessage() + " - " + e.getClass().getName();
                sender.sendMessage("&cAn error occurred while starting the HTTP server: " + info);
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
                    .filter(s -> startsWithIgnoreCase(s, args[0]))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("money")) {
            return MONEY_ACTIONS.stream()
                    .filter(s -> startsWithIgnoreCase(s, args[1]))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("money")) {
            return this.plugin.getPlatform().getOnlinePlayers()
                    .map(CommandSender::getName)
                    .filter(name -> startsWithIgnoreCase(name, args[2]))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public void editMoney(CommandSender sender, String[] args) throws NumberFormatException {
        if (args.length < 4 || !MONEY_ACTIONS.contains(args[1].toLowerCase())) {
            sender.sendMessage("&cUsage: /azlink money <add|remove|set> <player> <amount>");
            return;
        }

        String action = args[1].toLowerCase();
        double amount = Double.parseDouble(args[3]);
        Optional<UserInfo> user = this.plugin.getUserManager().getUserByName(args[2]);

        if (amount <= 0) {
            sender.sendMessage("&cThe amount must be positive.");
            return;
        }

        if (!user.isPresent()) {
            sender.sendMessage("&cUnable to find player '" + args[2] + "', please try again in few seconds or use '/azlink fetch'.");
            return;
        }

        this.plugin.getUserManager().editMoney(user.get(), action, amount)
                .thenAccept(u -> {
                    sender.sendMessage("&aMoney has been edited successfully.");
                    sender.sendMessage("&aNew balance: " + u.getMoney());
                })
                .exceptionally(ex -> {
                    sender.sendMessage("&cUnable to edit money: " + ex.getMessage());

                    return null;
                });
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

        if (startsWithIgnoreCase(url, "http:")) {
            sender.sendMessage("&6You should use https to improve security!");
        }

        this.plugin.getConfig().setSiteKey(key);
        this.plugin.getConfig().setSiteUrl(url);

        showStatus(sender)
                .thenRun(() -> saveConfig(sender))
                .thenRun(this.plugin::restartHttpServer);
    }

    private void saveConfig(CommandSender sender) {
        try {
            this.plugin.saveConfig();
        } catch (IOException e) {
            String info = e.getMessage() + " - " + e.getClass().getName();
            sender.sendMessage("&cAn error occurred while saving config: " + info);
            this.plugin.getLogger().error("Error while saving config", e);
        }
    }

    private CompletableFuture<Void> showStatus(CommandSender sender) {
        return this.plugin.getHttpClient()
                .verifyStatus()
                .thenRun(() -> sender.sendMessage("&aLinked to the website successfully."))
                .thenRun(this.plugin::fetch)
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        sender.sendMessage("&cUnable to connect to the website: " + ex.getMessage());
                    }
                });
    }

    private static boolean startsWithIgnoreCase(String string, String prefix) {
        if (string.length() < prefix.length()) {
            return false;
        }

        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
