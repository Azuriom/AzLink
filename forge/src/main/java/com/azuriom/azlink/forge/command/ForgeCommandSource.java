package com.azuriom.azlink.forge.command;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.forge.MinecraftComponentAdapter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class ForgeCommandSource implements CommandSender {

    private final CommandSourceStack source;

    public ForgeCommandSource(CommandSourceStack source) {
        this.source = source;
    }

    @Override
    public String getName() {
        return this.source.getTextName();
    }

    @Override
    public UUID getUuid() {
        Entity entity = this.source.getEntity();

        return entity != null
                ? entity.getUUID()
                : UUID.nameUUIDFromBytes(this.source.getTextName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(TextComponent.text(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.source.sendSystemMessage(MinecraftComponentAdapter.toComponent(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.source.hasPermission(3);
    }
}
