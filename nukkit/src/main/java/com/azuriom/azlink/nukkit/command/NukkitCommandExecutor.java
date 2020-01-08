package com.azuriom.azlink.nukkit.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.AzLinkCommand;

public class NukkitCommandExecutor extends Command {

    private final AzLinkCommand azLinkCommand;

    public NukkitCommandExecutor(AzLinkPlugin azLinkPlugin) {

        super("azlink", "Manage the AzLink plugin");

        this.setAliases(new String[]{"azuriomlink"});

        this.setPermission("azlink.admin");

        this.azLinkCommand = new AzLinkCommand(azLinkPlugin);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        azLinkCommand.execute(new NukkitCommandSender(sender), args);

        return false;

    }
}
