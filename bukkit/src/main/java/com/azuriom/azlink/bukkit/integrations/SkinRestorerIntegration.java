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

public class SkinRestorerIntegration implements Listener {

    private final SkinsRestorer skinsRestorer;
    private final AzLinkBukkitPlugin plugin;

    public SkinRestorerIntegration(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;
        this.skinsRestorer = SkinsRestorerProvider.get();

        this.plugin.getLoggerAdapter().info("SkinRestorer integration enabled.");
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
            MineSkinResponse skin = this.skinsRestorer.getMineSkinAPI().genSkin(url, null);

            this.skinsRestorer.getSkinApplier(Player.class).applySkin(player, skin.getProperty());
        } catch (DataRequestException | MineSkinException ex) {
            this.plugin.getLoggerAdapter().warn("Unable to apply skin for " + player.getName() + ": " + ex.getMessage());
        }
    }
}
