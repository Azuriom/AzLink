package com.azuriom.azlink.bukkit.integrations;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import net.skinsrestorer.api.PlayerWrapper;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.exception.SkinRequestException;
import net.skinsrestorer.api.property.IProperty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SkinRestorerIntegration implements Listener {

    private final AzLinkBukkitPlugin plugin;

    public SkinRestorerIntegration(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;

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
            IProperty skin = SkinsRestorerAPI.getApi().genSkinUrl(url, null);

            SkinsRestorerAPI.getApi().applySkin(new PlayerWrapper(player), skin);
        } catch (SkinRequestException ex) {
            this.plugin.getLoggerAdapter().warn("Unable to apply skin for " + player.getName() + ": " + ex.getMessage());
        }
    }
}
