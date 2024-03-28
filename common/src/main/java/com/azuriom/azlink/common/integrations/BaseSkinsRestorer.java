package com.azuriom.azlink.common.integrations;

import com.azuriom.azlink.common.AzLinkPlugin;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;

public class BaseSkinsRestorer<P> {

    private final Class<P> playerClass;
    protected final AzLinkPlugin plugin;

    public BaseSkinsRestorer(AzLinkPlugin plugin, Class<P> playerClass) {
        this.plugin = plugin;
        this.playerClass = playerClass;

        this.plugin.getLogger().info("SkinsRestorer integration enabled.");
    }

    protected void handleJoin(String playerName, P player) {
        String baseUrl = this.plugin.getConfig().getSiteUrl();

        if (baseUrl == null) {
            return;
        }

        this.plugin.getScheduler().executeAsync(() -> {
            try {
                String url = baseUrl + "/api/skin-api/skins/" + playerName;
                SkinsRestorer skins = SkinsRestorerProvider.get();
                MineSkinResponse res = skins.getMineSkinAPI().genSkin(url, null);

                skins.getSkinApplier(this.playerClass).applySkin(player, res.getProperty());
            } catch (DataRequestException | MineSkinException e) {
                this.plugin.getLogger().warn("Unable to apply skin for " + playerName + ": " + e.getMessage());
            }
        });
    }
}
