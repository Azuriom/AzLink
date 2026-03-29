package com.azuriom.azlink.fabric.command;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.fabric.MinecraftTextAdapter;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;

public class FabricCommandSource implements CommandSender {

    private static final Permission PERMISSION_LEVEL_OWNERS = new Permission.Level(PermissionLevel.OWNERS);

    private final ServerCommandSource source;

    public FabricCommandSource(ServerCommandSource source) {
        this.source = source;
    }

    @Override
    public String getName() {
        return this.source.getName();
    }

    @Override
    public UUID getUuid() {
        Entity entity = this.source.getEntity();

        return entity != null
                ? entity.getUuid()
                : UUID.nameUUIDFromBytes(this.source.getName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(TextComponent.text(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.source.sendMessage(MinecraftTextAdapter.toText(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.source.getPermissions().hasPermission(PERMISSION_LEVEL_OWNERS);
    }
}
