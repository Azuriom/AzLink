package com.azuriom.azlink.velocity.integrations;

import com.azuriom.azlink.common.integrations.BaseNLogin;
import com.azuriom.azlink.velocity.AzLinkVelocityPlugin;
import com.nickuc.login.api.enums.TwoFactorType;
import com.nickuc.login.api.event.velocity.twofactor.TwoFactorAddEvent;
import com.nickuc.login.api.event.velocity.auth.RegisterEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;

public class NLoginIntegration extends BaseNLogin {

    public NLoginIntegration(AzLinkVelocityPlugin plugin) {
        super(plugin.getPlugin());
    }

    @Subscribe
    public void onEmailAdded(TwoFactorAddEvent event) {
        if (event.getType() != TwoFactorType.EMAIL) {
            return;
        }

        handleEmailUpdated(event.getPlayerId(), event.getPlayerName(), event.getAccount());
    }

    @Subscribe
    public void onRegister(RegisterEvent event) {
        Player player = event.getPlayer();

        handleRegister(player.getUniqueId(), player.getUsername(), event.getPassword(), player.getRemoteAddress().getAddress());
    }
}
