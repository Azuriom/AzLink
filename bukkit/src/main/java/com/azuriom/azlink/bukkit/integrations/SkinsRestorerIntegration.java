package com.azuriom.azlink.bukkit.integrations;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SkinsRestorerIntegration implements Listener {

    private final AzLinkBukkitPlugin plugin;

    public SkinsRestorerIntegration(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;

        this.plugin.getLoggerAdapter().info("SkinsRestorer integration enabled.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String baseUrl = this.plugin.getPlugin().getConfig().getSiteUrl();


        if (baseUrl == null) {
            return;
        }

        try {
            String url = baseUrl + "/api/skin-api/skins/" + player.getName();
            SkinsRestorer skins = SkinsRestorerProvider.get();
            MineSkinResponse res = skins.getMineSkinAPI().genSkin(url, null);

            skins.getSkinApplier(Player.class).applySkin(player, res.getProperty());
        } catch (DataRequestException | MineSkinException ex) {
            this.plugin.getLoggerAdapter().warn("Unable to apply skin for " + player.getName() + ": " + ex.getMessage());
        }
    }
}
