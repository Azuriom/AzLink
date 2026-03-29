package com.azuriom.azlink.sponge.command;

import com.azuriom.azlink.common.chat.AdventureComponentAdapter;
import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.command.CommandSender;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.util.Nameable;

import java.util.UUID;

public class SpongeCommandSender implements CommandSender {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .extractUrls()
            .build();

    private final Subject subject;
    private final Audience audience;

    public SpongeCommandSender(CommandCause commandCause) {
        this(commandCause.subject(), commandCause.audience());
    }

    public SpongeCommandSender(Subject subject, Audience audience) {
        this.subject = subject;
        this.audience = audience;
    }

    @Override
    public String getName() {
        if (this.subject instanceof Nameable) {
            return ((Nameable) this.subject).name();
        }

        return this.subject.friendlyIdentifier().orElse(this.subject.identifier());
    }

    @Override
    public UUID getUuid() {
        if (this.subject instanceof Identifiable) {
            return ((Identifiable) this.subject).uniqueId();
        }

        return UUID.nameUUIDFromBytes(this.subject.identifier().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        this.audience.sendMessage(SERIALIZER.deserialize(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.audience.sendMessage(AdventureComponentAdapter.toAdventure(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.subject.hasPermission(permission);
    }
}
