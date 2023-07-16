package com.azuriom.azlink.velocity.integrations;

import com.azuriom.azlink.velocity.AzLinkVelocityPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import net.elytrium.limboauth.event.PostRegisterEvent;

import java.net.InetAddress;

public class LimboAuthIntegration {

    private final AzLinkVelocityPlugin plugin;

    public LimboAuthIntegration(AzLinkVelocityPlugin plugin) {
        this.plugin = plugin;

        this.plugin.getLoggerAdapter().info("LimboAuth integration enabled.");
    }

    @Subscribe
    public void onPostRegister(PostRegisterEvent event) {
        Player player = event.getPlayer().getProxyPlayer();
        String password = event.getPassword();
        InetAddress ip = player.getRemoteAddress().getAddress();

        this.plugin.getPlugin()
                .getHttpClient()
                .registerUser(player.getUsername(), null, player.getUniqueId(), password, ip)
                .exceptionally(ex -> {
                    this.plugin.getLoggerAdapter().error("Unable to register " + player.getUsername(), ex);

                    return null;
                });
    }
}
