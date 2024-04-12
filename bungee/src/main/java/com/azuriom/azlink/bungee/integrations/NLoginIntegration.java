package com.azuriom.azlink.bungee.integrations;

import com.azuriom.azlink.bungee.AzLinkBungeePlugin;
import com.azuriom.azlink.common.integrations.BaseNLogin;
import com.nickuc.login.api.enums.TwoFactorType;
import com.nickuc.login.api.event.bungee.account.PasswordUpdateEvent;
import com.nickuc.login.api.event.bungee.auth.RegisterEvent;
import com.nickuc.login.api.event.bungee.twofactor.TwoFactorAddEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NLoginIntegration extends BaseNLogin implements Listener {

    public NLoginIntegration(AzLinkBungeePlugin plugin) {
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
        ProxiedPlayer player = event.getPlayer();
        SocketAddress socketAddress = player.getSocketAddress();
        InetAddress address = socketAddress instanceof InetSocketAddress
                ? ((InetSocketAddress) socketAddress).getAddress() : null;

        handleRegister(player.getUniqueId(), player.getName(), event.getPassword(), address);
    }

    @EventHandler
    public void onPasswordUpdate(PasswordUpdateEvent event) {
        handleUpdatePassword(event.getPlayerId(), event.getPlayerName(), event.getNewPassword());
    }

    public static void register(AzLinkBungeePlugin plugin) {
        if (ensureApiVersion(plugin)) {
            plugin.getProxy().getPluginManager().registerListener(plugin, new NLoginIntegration(plugin));
        }
    }
}
