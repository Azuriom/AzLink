package com.azuriom.azlink.common.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.chat.TextColor;
import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.data.UserInfo;
import com.azuriom.azlink.common.users.MoneyAction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AzLinkCommand {

    private static final List<String> COMPLETIONS = Arrays.asList("status", "setup", "fetch", "money", "port");

    protected final AzLinkPlugin plugin;

    public AzLinkCommand(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("azlink.admin")) {
            String version = this.plugin.getPlatform().getPluginVersion();
            sender.sendMessage(
                    TextComponent.text("AzLink v" + version, TextColor.BLUE)
                            .append(TextComponent.text(". Website: ", TextColor.GRAY))
                            .append(TextComponent.link("https://azuriom.com").color(TextColor.BLUE))
            );
            sender.sendMessage(TextComponent.text("You don't have the permission to use this command.", TextColor.RED));
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("setup")) {
            if (args.length < 3) {
                sender.sendMessage(
                        TextComponent.text("You must first add this server in your Azuriom admin dashboard, in the 'Servers' section.", TextColor.GOLD)
                );
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
                sender.sendMessage(TextComponent.text("'" + args[3] + "' is not a valid number !", TextColor.RED));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("fetch")) {
            this.plugin.fetch()
                    .thenRun(() -> sender.sendMessage(TextComponent.text("Data has been fetched successfully.", TextColor.GOLD)))
                    .exceptionally(ex -> {
                        sender.sendMessage(TextComponent.text("Unable to fetch data: " + ex.getMessage(), TextColor.RED));
                        return null;
                    });
            return;
        }

        if (args[0].equalsIgnoreCase("port")) {
            if (args.length < 2) {
                sender.sendMessage(TextComponent.text("Usage: /azlink port <port>", TextColor.RED));
                return;
            }

            int port;

            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TextComponent.text("'" + args[1] + "' is not a valid port !", TextColor.RED));
                return;
            }

            if (port < 1 || port > 65535) {
                sender.sendMessage(TextComponent.text("The port must be between 1 and 65535", TextColor.RED));
                return;
            }

            this.plugin.getConfig().setHttpPort(port);

            try {
                this.plugin.restartHttpServer();

                sender.sendMessage(TextComponent.text("HTTP server started on port " + port, TextColor.GREEN));
            } catch (Exception e) {
                String info = e.getMessage() + " - " + e.getClass().getName();
                sender.sendMessage(TextComponent.text("An error occurred while starting the HTTP server: " + info, TextColor.RED));
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
            return Arrays.stream(MoneyAction.values())
                    .map(Enum::toString)
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
        MoneyAction action;

        if (args.length < 4 || (action = MoneyAction.fromString(args[1])) == null) {
            sender.sendMessage(TextComponent.text("Usage: /azlink money <add|remove|set> <player> <amount>", TextColor.RED));
            return;
        }

        double amount = Double.parseDouble(args[3]);
        Optional<UserInfo> user = this.plugin.getUserManager().getUserByName(args[2]);

        if (amount <= 0) {
            sender.sendMessage(TextComponent.text("The amount must be positive.", TextColor.RED));
            return;
        }

        if (!user.isPresent()) {
            sender.sendMessage(TextComponent.text("Unable to find player '" + args[2] + "', please try again in few seconds or use '/azlink fetch'.", TextColor.RED));
            return;
        }

        this.plugin.getUserManager().editMoney(user.get(), action, amount)
                .thenAccept(u -> {
                    sender.sendMessage(TextComponent.text("Money has been edited successfully.", TextColor.GREEN));
                    sender.sendMessage(TextComponent.text("New balance: " + u.getMoney(), TextColor.GREEN));
                })
                .exceptionally(ex -> {
                    sender.sendMessage(TextComponent.text("Unable to edit money: " + ex.getMessage(), TextColor.RED));
                    return null;
                });
    }

    public String getUsage() {
        return "Usage: /azlink [" + String.join("|", COMPLETIONS) + "]";
    }

    private void sendUsage(CommandSender sender) {
        String version = this.plugin.getPlatform().getPluginVersion();
        sender.sendMessage(
                TextComponent.text("AzLink v" + version, TextColor.BLUE)
                        .append(TextComponent.text(". Website: ", TextColor.GRAY))
                        .append(TextComponent.link("https://azuriom.com").color(TextColor.BLUE))
        );
        sender.sendMessage(TextComponent.text("- /azlink setup", TextColor.DARK_GRAY));
        sender.sendMessage(TextComponent.text("- /azlink port <port>", TextColor.DARK_GRAY));
        sender.sendMessage(TextComponent.text("- /azlink status", TextColor.DARK_GRAY));
        sender.sendMessage(TextComponent.text("- /azlink money <add|remove|set> <player> <amount>", TextColor.DARK_GRAY));
    }

    private void setup(CommandSender sender, String url, String key) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (startsWithIgnoreCase(url, "http:")) {
            sender.sendMessage(TextComponent.text("You should use https to improve security!", TextColor.GOLD));
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
            sender.sendMessage(TextComponent.text("An error occurred while saving config: " + info, TextColor.RED));
            this.plugin.getLogger().error("Error while saving config", e);
        }
    }

    private CompletableFuture<Void> showStatus(CommandSender sender) {
        return this.plugin.getHttpClient()
                .verifyStatus()
                .thenRun(() -> sender.sendMessage(TextComponent.text("Linked to the website successfully.", TextColor.GREEN)))
                .thenRun(this.plugin::fetch)
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        sender.sendMessage(TextComponent.text("Unable to connect to the website: " + ex.getMessage(), TextColor.RED));
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
