package com.azuriom.azlink.neoforge.command;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.neoforge.MinecraftComponentAdapter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class NeoForgeCommandSource implements CommandSender {

    private static final Permission PERMISSION_LEVEL_OWNERS = new Permission.HasCommandLevel(PermissionLevel.OWNERS);

    private final CommandSourceStack source;

    public NeoForgeCommandSource(CommandSourceStack source) {
        this.source = source;
    }

    @Override
    public String getName() {
        return this.source.getTextName();
    }

    @Override
    public UUID getUuid() {
        Entity entity = this.source.getEntity();

        return entity != null
                ? entity.getUUID()
                : UUID.nameUUIDFromBytes(this.source.getTextName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(TextComponent.text(message));
    }

    @Override
    public void sendMessage(TextComponent message) {
        this.source.sendSystemMessage(MinecraftComponentAdapter.toComponent(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.source.permissions().hasPermission(PERMISSION_LEVEL_OWNERS);
    }
}
