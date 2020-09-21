package com.azuriom.azlink.nukkit.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.AzLinkCommand;

public class NukkitCommandExecutor extends AzLinkCommand implements CommandExecutor {

    public NukkitCommandExecutor(AzLinkPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        execute(new NukkitCommandSender(sender), args);

        return true;
    }
}
