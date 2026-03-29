package com.azuriom.azlink.hytale.command;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.hytale.HytaleComponentAdapter;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import java.util.UUID;

public class HytaleCommandSender implements com.azuriom.azlink.common.command.CommandSender {

    private final CommandSender sender;

    public HytaleCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String getName() {
        return this.sender.getDisplayName();
    }

    @Override
    public UUID getUuid() {
        return this.sender.getUuid();
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(TextComponent.text(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.sender.sendMessage(HytaleComponentAdapter.toHytale(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.sender.hasPermission(permission);
    }
}
