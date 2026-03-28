package com.azuriom.azlink.forge.command;

import com.azuriom.azlink.common.command.CommandSender;
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
        this.player.sendSystemMessage(ComponentAdapter.toComponent(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermissions(4); // su-level
    }
}
