package com.azuriom.azlink.common.command;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.data.PlayerData;

import java.util.UUID;

public interface CommandSender {

    String getName();

    UUID getUuid();

    void sendMessage(String message);

    default void sendMessage(TextComponent message) {
        sendMessage(message.toMinecraftLegacy());
    }

    boolean hasPermission(String permission);

    default PlayerData toData() {
        return new PlayerData(getName(), getUuid());
    }
}
