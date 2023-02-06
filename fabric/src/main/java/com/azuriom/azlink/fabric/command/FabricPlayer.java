package com.azuriom.azlink.fabric.command;

import com.azuriom.azlink.common.command.CommandSender;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class FabricPlayer implements CommandSender {

    private final ServerPlayerEntity player;

    public FabricPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return this.player.getName().getString();
    }

    @Override
    public UUID getUuid() {
        return this.player.getUuid();
    }

    @Override
    public void sendMessage(String message) {
        this.player.sendMessage(TextAdapter.toText(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermissionLevel(3);
    }
}
