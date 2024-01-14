package com.azuriom.azlink.bukkit.integrations;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import com.azuriom.azlink.common.integrations.BaseNLogin;
import com.nickuc.login.api.enums.TwoFactorType;
import com.nickuc.login.api.event.bukkit.auth.RegisterEvent;
import com.nickuc.login.api.event.bukkit.twofactor.TwoFactorAddEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class NLoginIntegration
        extends BaseNLogin implements Listener {

    public NLoginIntegration(AzLinkBukkitPlugin plugin) {
        super(plugin.getPlugin());
    }

    @EventHandler
    public void onEmailAdded(TwoFactorAddEvent event) {
        if (event.getType() != TwoFactorType.EMAIL) {
            return;
        }

        handleEmailUpdated(event.getPlayerId(), event.getPlayerName(), event.getAccount());
    }

    @EventHandler
    public void onRegister(RegisterEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress socketAddress = player.getAddress();
        InetAddress address = socketAddress != null ? socketAddress.getAddress() : null;

        handleRegister(player.getUniqueId(), player.getName(), event.getPassword(), address);
    }

    public static void register(AzLinkBukkitPlugin plugin) {
        if (ensureApiVersion(plugin)) {
            plugin.getServer().getPluginManager().registerEvents(new NLoginIntegration(plugin), plugin);
        }
    }
}
