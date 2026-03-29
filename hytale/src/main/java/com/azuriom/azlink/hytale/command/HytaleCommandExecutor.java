package com.azuriom.azlink.hytale.command;

import com.azuriom.azlink.common.command.AzLinkCommand;
import com.azuriom.azlink.hytale.AzLinkHytalePlugin;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import java.util.Arrays;

public class HytaleCommandExecutor extends CommandBase {

    private final AzLinkCommand command;

    public HytaleCommandExecutor(AzLinkHytalePlugin plugin) {
        super("azlink", "AzLink plugin management");
        this.command = new AzLinkCommand(plugin.getPlugin());

        requirePermission("azlink.admin");
        registerSubCommands();
    }

    private void registerSubCommands() {
        addSubCommand(new AzLinkSubCommand("status", "View Azuriom connection status"));
        addSubCommand(new AzLinkSubCommand("setup", "Configure Azuriom website connection").allowExtraArgs());
        addSubCommand(new AzLinkSubCommand("fetch", "Refresh data from Azuriom"));
        addSubCommand(new AzLinkSubCommand("money", "Manage player website balance") {{
            addMoneyAction(this, "add", "Add to");
            addMoneyAction(this, "remove", "Subtract from");
            addMoneyAction(this, "set", "Set");
        }});
        addSubCommand(new AzLinkSubCommand("port", "Update instant commands port")
                .requiresArg("port", "Port number", ArgTypes.INTEGER));
    }

    private void addMoneyAction(AbstractCommand parentCommand, String actionName, String actionVerb) {
        parentCommand.addSubCommand(new AzLinkSubCommand(actionName, actionVerb + " player balance")
                .requiresArg("player", "Target player", ArgTypes.STRING)
                .requiresArg("amount", "Amount", ArgTypes.DOUBLE)
        );
    }

    @Override
    protected void executeSync(CommandContext context) {
        String[] parts = context.getInputString().split(" ", -1);
        String[] args = parts.length > 1
                ? Arrays.copyOfRange(parts, 1, parts.length)
                : new String[0];

        HytaleCommandSender sender = new HytaleCommandSender(context.sender());
        this.command.execute(sender, args);
    }

    class AzLinkSubCommand extends CommandBase {

        public AzLinkSubCommand(String name, String description) {
            super(name, description);
        }

        @Override
        protected void executeSync(CommandContext context) {
            HytaleCommandExecutor.this.executeSync(context);
        }

        public AzLinkSubCommand allowExtraArgs() {
            setAllowsExtraArguments(true);
            return this;
        }

        public <D> AzLinkSubCommand requiresArg(String name, String description, ArgumentType<D> type) {
            withRequiredArg(name, description, type);
            return this;
        }
    }
}
