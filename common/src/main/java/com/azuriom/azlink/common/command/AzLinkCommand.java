package com.azuriom.azlink.common.command;

import com.azuriom.azlink.common.AzLinkPlugin;

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
            // TODO
            return;
        }

        if (args[0].equalsIgnoreCase("status")) {
            // TODO
            return;
        }

        if (args[0].equalsIgnoreCase("key")) {
            // TODO
            return;
        }

        if (args[0].equalsIgnoreCase("site")) {
            // TODO
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
        // TODO
    }

    private static boolean startsWithIgnoreCase(String string, String prefix) {
        return string.length() >= prefix.length() && string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
