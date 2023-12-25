package com.azuriom.azlink.sponge.integrations;

import com.azuriom.azlink.common.integrations.BaseSkinsRestorer;
import com.azuriom.azlink.sponge.AzLinkSpongePlugin;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class SkinsRestorerIntegration
        extends BaseSkinsRestorer<ServerPlayer> {

    public SkinsRestorerIntegration(AzLinkSpongePlugin plugin) {
        super(plugin.getPlugin(), ServerPlayer.class);
    }

    @Listener
    public void onPlayerJoin(ServerSideConnectionEvent.Join event) {
        handleJoin(event.player().name(), event.player());
    }
}
