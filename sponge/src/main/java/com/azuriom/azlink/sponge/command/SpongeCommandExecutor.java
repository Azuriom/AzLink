package com.azuriom.azlink.sponge.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.AzLinkCommand;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.Command.Raw;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeCommandExecutor extends AzLinkCommand implements Raw {

    public SpongeCommandExecutor(AzLinkPlugin plugin) {
        super(plugin);
    }

    @Override
    public CommandResult process(CommandCause cause, Mutable arguments) {
        this.execute(new SpongeCommandSender(cause), arguments.input().split(" ", -1));

        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, Mutable arguments) {
        String[] args = arguments.input().split(" ", -1);

        return this.tabComplete(new SpongeCommandSender(cause), args).stream()
                .map(CommandCompletion::of)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canExecute(CommandCause cause) {
        return cause.hasPermission("azlink.admin");
    }

    @Override
    public Optional<Component> shortDescription(CommandCause cause) {
        return Optional.of(Component.text("Manage the AzLink plugin."));
    }

    @Override
    public Optional<Component> extendedDescription(CommandCause cause) {
        return Optional.empty();
    }

    @Override
    public Optional<Component> help(@NonNull CommandCause cause) {
        return shortDescription(cause);
    }

    @Override
    public Component usage(CommandCause cause) {
        return Component.text(getUsage());
    }
}
