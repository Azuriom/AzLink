package com.azuriom.azlink.bukkit.integrations;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import com.azuriom.azlink.common.integrations.BaseSkinsRestorer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SkinsRestorerIntegration
        extends BaseSkinsRestorer<Player> implements Listener {

    public SkinsRestorerIntegration(AzLinkBukkitPlugin plugin) {
        super(plugin.getPlugin(), Player.class);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        handleJoin(player.getName(), player);
    }
}
