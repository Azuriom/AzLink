package com.azuriom.azlink.common.integrations;

import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.nickuc.login.api.nLoginAPI;

import java.net.InetAddress;
import java.util.UUID;

public class BaseNLogin {

    protected final AzLinkPlugin plugin;

    public BaseNLogin(AzLinkPlugin plugin) {
        this.plugin = plugin;

        this.plugin.getLogger().info("nLogin integration enabled.");
    }

    protected void handleEmailUpdated(UUID uuid, String name, String email) {
        this.plugin.getHttpClient()
                .updateEmail(uuid, email)
                .exceptionally(ex -> {
                    this.plugin.getLogger().error("Unable to update email for " + name, ex);

                    return null;
                });
    }

    protected void handleRegister(UUID uuid, String name, String password, InetAddress address) {
        this.plugin.getHttpClient()
                .registerUser(name, null, uuid, password, address)
                .exceptionally(ex -> {
                    this.plugin.getLogger().error("Unable to register " + name, ex);

                    return null;
                });
    }

    protected static boolean ensureApiVersion(AzLinkPlatform platform) {
        if (nLoginAPI.getApi().getApiVersion() < 5) {
            platform.getPlugin().getLogger().warn("nLogin integration requires API v5 or higher");
            return false;
        }

        return true;
    }
}
