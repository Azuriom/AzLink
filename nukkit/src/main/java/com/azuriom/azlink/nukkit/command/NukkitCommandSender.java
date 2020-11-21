package com.azuriom.azlink.nukkit.command;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.azuriom.azlink.common.command.CommandSender;

import java.util.UUID;

public class NukkitCommandSender implements CommandSender {

    private final cn.nukkit.command.CommandSender commandSender;

    public NukkitCommandSender(cn.nukkit.command.CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    @Override
    public String getName() {
        return this.commandSender.getName();
    }

    @Override
    public UUID getUuid() {
        if (this.commandSender instanceof Player) {
            return ((Player) this.commandSender).getUniqueId();
        }

        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        this.commandSender.sendMessage(TextFormat.colorize(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.commandSender.hasPermission(permission);
    }
}
