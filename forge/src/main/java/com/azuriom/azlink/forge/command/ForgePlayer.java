package com.azuriom.azlink.forge.command;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.forge.MinecraftComponentAdapter;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ForgePlayer implements CommandSender {

    private final ServerPlayer player;

    public ForgePlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return this.player.getName().getString();
    }

    @Override
    public UUID getUuid() {
        return this.player.getUUID();
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(TextComponent.text(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.player.sendSystemMessage(MinecraftComponentAdapter.toComponent(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermissions(3);
    }
}
