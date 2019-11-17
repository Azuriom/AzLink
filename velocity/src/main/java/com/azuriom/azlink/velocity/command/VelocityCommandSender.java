package com.azuriom.azlink.velocity.command;

import com.azuriom.azlink.common.command.CommandSender;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public class VelocityCommandSender implements CommandSender {

    private final CommandSource source;

    public VelocityCommandSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public String getName() {
        return source instanceof Player ? ((Player) source).getUsername() : "Console";
    }

    @Override
    public UUID getUuid() {
        if (source instanceof Player) {
            return ((Player) source).getUniqueId();
        }

        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        source.sendMessage(LegacyComponentSerializer.legacyLinking().deserialize(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }
}
