package com.azuriom.azlink.nukkit.command;

import cn.nukkit.Player;
import com.azuriom.azlink.common.command.CommandSender;

import java.util.UUID;

public class NukkitCommandSender implements CommandSender {

    private final cn.nukkit.command.CommandSender commandSender;

    public NukkitCommandSender(cn.nukkit.command.CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    @Override
    public String getName() {
        return commandSender.getName();
    }

    @Override
    public UUID getUuid() {
        if (commandSender instanceof Player) {
            return ((Player) commandSender).getUniqueId();
        }

        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        commandSender.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return commandSender.hasPermission(permission);
    }
}
