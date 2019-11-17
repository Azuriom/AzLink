package com.azuriom.azlink.sponge.command;

import com.azuriom.azlink.common.command.CommandSender;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Identifiable;

import java.util.UUID;

public class SpongeCommandSender implements CommandSender {

    private final CommandSource source;

    public SpongeCommandSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public UUID getUuid() {
        if (source instanceof Identifiable) {
            return ((Identifiable) source).getUniqueId();
        }

        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        source.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message.replace('\u00A7', '&')));
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }
}
