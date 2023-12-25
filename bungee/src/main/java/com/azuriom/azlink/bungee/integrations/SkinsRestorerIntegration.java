package com.azuriom.azlink.bungee.integrations;

import com.azuriom.azlink.bungee.AzLinkBungeePlugin;
import com.azuriom.azlink.common.integrations.BaseSkinsRestorer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class SkinsRestorerIntegration
        extends BaseSkinsRestorer<ProxiedPlayer> implements Listener {

    public SkinsRestorerIntegration(AzLinkBungeePlugin plugin) {
        super(plugin.getPlugin(), ProxiedPlayer.class);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        handleJoin(player.getName(), player);
    }
}
