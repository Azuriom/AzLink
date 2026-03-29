package com.azuriom.azlink.hytale.command;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.hytale.HytaleComponentAdapter;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

public class HytalePlayerWrapper implements com.azuriom.azlink.common.command.CommandSender {

    private final PlayerRef player;

    public HytalePlayerWrapper(PlayerRef player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return this.player.getUsername();
    }

    @Override
    public UUID getUuid() {
        return this.player.getUuid();
    }


    @Override
    public void sendMessage(String message) {
        sendMessage(TextComponent.text(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.player.sendMessage(HytaleComponentAdapter.toHytale(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }
}
