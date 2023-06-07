package com.azuriom.azlink.fabric.command;

import com.azuriom.azlink.common.command.CommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;

public class FabricCommandSource implements CommandSender {

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
        this.source.sendMessage(TextAdapter.toText(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.source.hasPermissionLevel(3);
    }
}
