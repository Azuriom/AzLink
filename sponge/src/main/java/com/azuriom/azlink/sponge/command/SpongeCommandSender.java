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
        return this.source.getName();
    }

    @Override
    public UUID getUuid() {
        if (this.source instanceof Identifiable) {
            return ((Identifiable) this.source).getUniqueId();
        }

        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        this.source.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.source.hasPermission(permission);
    }
}
