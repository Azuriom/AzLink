package com.azuriom.azlink.bukkit.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.AzLinkCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class BukkitCommandExecutor extends AzLinkCommand implements TabExecutor {

    public BukkitCommandExecutor(AzLinkPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        execute(new BukkitCommandSender(sender), args);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return tabComplete(new BukkitCommandSender(sender), args);
    }
}
