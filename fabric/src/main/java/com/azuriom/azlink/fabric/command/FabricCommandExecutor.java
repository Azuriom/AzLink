package com.azuriom.azlink.fabric.command;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.command.AzLinkCommand;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class FabricCommandExecutor<S extends ServerCommandSource>
        extends AzLinkCommand implements Command<S>, SuggestionProvider<S> {

    public FabricCommandExecutor(AzLinkPlugin plugin) {
        super(plugin);
    }

    @Override
    public int run(CommandContext<S> context) {
        String args = context.getArgument("args", String.class);
        FabricCommandSource source = new FabricCommandSource(context.getSource());

        this.execute(source, args.split(" ", -1));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String args = context.getArgument("args", String.class);
        FabricCommandSource source = new FabricCommandSource(context.getSource());

        this.tabComplete(source, args.split(" ", -1)).forEach(builder::suggest);

        return builder.buildFuture();
    }

    public void register(CommandDispatcher<S> dispatcher) {
        LiteralArgumentBuilder<S> command = LiteralArgumentBuilder.<S>literal("")
                .then(RequiredArgumentBuilder
                        .<S, String>argument("args", StringArgumentType.greedyString())
                        .executes(this)
                        .suggests(this)
                )
                .executes(this);

        dispatcher.register(command);
    }
}
