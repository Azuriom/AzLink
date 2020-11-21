package com.azuriom.azlink.sponge.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.AzLinkCommand;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SpongeCommandExecutor extends AzLinkCommand implements CommandCallable {

    public SpongeCommandExecutor(AzLinkPlugin plugin) {
        super(plugin);
    }

    @Override
    @Nonnull
    public CommandResult process(@Nonnull CommandSource source, String arguments) {
        execute(new SpongeCommandSender(source), arguments.split(" ", -1));

        return CommandResult.success();
    }

    @Override
    @Nonnull
    public List<String> getSuggestions(@Nonnull CommandSource source, String arguments, @Nullable Location<World> targetPosition) {
        return tabComplete(new SpongeCommandSender(source), arguments.split(" ", -1));
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("azlink.admin");
    }

    @Override
    @Nonnull
    public Optional<Text> getShortDescription(@Nonnull CommandSource source) {
        return Optional.of(Text.of("Manage the AzLink plugin."));
    }

    @Override
    @Nonnull
    public Optional<Text> getHelp(@Nonnull CommandSource source) {
        return Optional.empty();
    }

    @Override
    @Nonnull
    public Text getUsage(@Nonnull CommandSource source) {
        return Text.of(getUsage());
    }
}
