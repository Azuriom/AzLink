package com.azuriom.azlink.velocity.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.AzLinkCommand;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;

public class VelocityCommandExecutor extends AzLinkCommand implements SimpleCommand {

    public VelocityCommandExecutor(AzLinkPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(Invocation invocation) {
        execute(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return tabComplete(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }
}
