package com.azuriom.azlink.velocity.command;

import com.azuriom.azlink.common.chat.AdventureComponentAdapter;
import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.command.CommandSender;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public class VelocityCommandSender implements CommandSender {

    private final CommandSource source;

    public VelocityCommandSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public String getName() {
        return this.source instanceof Player ? ((Player) this.source).getUsername() : "Console";
    }

    @Override
    public UUID getUuid() {
        if (this.source instanceof Player) {
            return ((Player) this.source).getUniqueId();
        }

        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        this.source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.source.sendMessage(AdventureComponentAdapter.toAdventure(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.source.hasPermission(permission);
    }
}
