package com.azuriom.azlink.velocity.integrations;


import com.azuriom.azlink.common.integrations.BaseSkinsRestorer;
import com.azuriom.azlink.velocity.AzLinkVelocityPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;

public class SkinsRestorerIntegration
        extends BaseSkinsRestorer<Player> {

    public SkinsRestorerIntegration(AzLinkVelocityPlugin plugin) {
        super(plugin.getPlugin(), Player.class);
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        handleJoin(event.getPlayer().getUsername(), event.getPlayer());
    }
}
