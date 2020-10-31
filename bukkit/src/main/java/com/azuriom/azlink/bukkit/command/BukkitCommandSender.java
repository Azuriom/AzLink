package com.azuriom.azlink.bukkit.command;

import com.azuriom.azlink.common.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class BukkitCommandSender implements CommandSender {

    private final org.bukkit.command.CommandSender sender;

    public BukkitCommandSender(org.bukkit.command.CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public UUID getUuid() {
        if (sender instanceof Entity) {
            return ((Entity) sender).getUniqueId();
        }

        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }
}
