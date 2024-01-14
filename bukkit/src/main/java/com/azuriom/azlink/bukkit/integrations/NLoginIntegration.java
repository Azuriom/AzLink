package com.azuriom.azlink.bukkit.integrations;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import com.azuriom.azlink.common.integrations.BaseNLogin;
import com.nickuc.login.api.enums.TwoFactorType;
import com.nickuc.login.api.event.bukkit.auth.RegisterEvent;
import com.nickuc.login.api.event.bukkit.twofactor.TwoFactorAddEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
        InetSocketAddress ip = player.getAddress();

        handleRegister(player.getUniqueId(), player.getName(), event.getPassword(), ip != null ? ip.getAddress() : null);
    }
}
