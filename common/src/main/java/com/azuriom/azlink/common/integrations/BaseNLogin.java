package com.azuriom.azlink.common.integrations;

import com.azuriom.azlink.common.AzLinkPlugin;

import java.net.InetAddress;
import java.util.UUID;

public class BaseNLogin {

    protected final AzLinkPlugin plugin;

    public BaseNLogin(AzLinkPlugin plugin) {
        this.plugin = plugin;

        this.plugin.getLogger().info("nLogin integration enabled.");
    }

    protected void handleEmailUpdated(UUID playerId, String playerName, String newEmail) {
        this.plugin
                .getHttpClient()
                .updateEmail(playerId, newEmail)
                .exceptionally(ex -> {
                    this.plugin.getLogger().error("Unable to update email for " + playerName, ex);

                    return null;
                });
    }

    protected void handleRegister(UUID playerId, String playerName, String password, InetAddress ip) {
        this.plugin
                .getHttpClient()
                .registerUser(playerName, null, playerId, password, ip)
                .exceptionally(ex -> {
                    this.plugin.getLogger().error("Unable to register " + playerName, ex);

                    return null;
                });
    }
}
