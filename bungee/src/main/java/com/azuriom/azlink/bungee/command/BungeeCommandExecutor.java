package com.azuriom.azlink.bungee.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.AzLinkCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommandExecutor extends Command implements TabExecutor {

    private final AzLinkCommand command;

    public BungeeCommandExecutor(AzLinkPlugin plugin) {
        super("azlink", "azlink.admin", "azuriomlink");

        this.command = new AzLinkCommand(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.command.execute(new BungeeCommandSender(sender), args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return this.command.tabComplete(new BungeeCommandSender(sender), args);
    }
}
