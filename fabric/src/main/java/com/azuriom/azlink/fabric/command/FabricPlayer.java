package com.azuriom.azlink.fabric.command;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.fabric.MinecraftTextAdapter;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class FabricPlayer implements CommandSender {

    private static final Permission PERMISSION_LEVEL_OWNERS = new Permission.Level(PermissionLevel.OWNERS);

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
        sendMessage(TextComponent.text(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.player.sendMessage(MinecraftTextAdapter.toText(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.getPermissions().hasPermission(PERMISSION_LEVEL_OWNERS);
    }
}
