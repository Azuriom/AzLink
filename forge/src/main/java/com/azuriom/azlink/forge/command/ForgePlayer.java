package com.azuriom.azlink.forge.command;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.forge.MinecraftComponentAdapter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.UUID;

public class ForgePlayer implements CommandSender {

    private static final Permission PERMISSION_LEVEL_OWNERS = new Permission.HasCommandLevel(PermissionLevel.OWNERS);

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
        sendMessage(TextComponent.text(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.player.sendSystemMessage(MinecraftComponentAdapter.toComponent(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.permissions().hasPermission(PERMISSION_LEVEL_OWNERS);
    }
}
