package com.azuriom.azlink.common.config;

import com.azuriom.azlink.common.http.server.HttpServer;

public class PluginConfig {

    private String siteKey;
    private String siteUrl;
    private boolean instantCommands = true;
    private int httpPort = HttpServer.DEFAULT_PORT;
    private boolean checkUpdates = true;

    public PluginConfig() {
        this(null, null);
    }

    public PluginConfig(String siteKey, String siteUrl) {
        this.siteKey = siteKey;
        this.siteUrl = siteUrl;
    }

    public String getSiteKey() {
        return this.siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getSiteUrl() {
        return this.siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public boolean hasInstantCommands() {
        return this.instantCommands;
    }

    public int getHttpPort() {
        return this.httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public boolean hasUpdatesCheck() {
        return this.checkUpdates;
    }

    public boolean isValid() {
        return this.siteKey != null && !this.siteKey.isEmpty() && this.siteUrl != null && !this.siteUrl.isEmpty();
    }

    @Override
    public String toString() {
        return "PluginConfig{siteKey='" + this.siteKey +
                "', siteUrl='" + this.siteUrl +
                "', httpPort=" + this.httpPort + '}';
    }
}
