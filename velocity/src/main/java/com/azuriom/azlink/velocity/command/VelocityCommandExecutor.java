package com.azuriom.azlink.velocity.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.AzLinkCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class VelocityCommandExecutor extends AzLinkCommand implements Command {

    public VelocityCommandExecutor(AzLinkPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSource source, @NonNull String[] args) {
        execute(new VelocityCommandSender(source), args);
    }

    @Override
    public List<String> suggest(CommandSource source, @NonNull String[] currentArgs) {
        return tabComplete(new VelocityCommandSender(source), currentArgs);
    }
}
