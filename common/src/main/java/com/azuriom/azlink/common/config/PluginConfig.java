package com.azuriom.azlink.common.config;

public class PluginConfig {

    private String siteKey;
    private String siteUrl;
    private boolean instantCommands;
    private int httpPort;

    public PluginConfig(String siteKey, String siteUrl, boolean instantCommands, int httpPort) {
        this.siteKey = siteKey;
        this.siteUrl = siteUrl;
        this.instantCommands = instantCommands;
        this.httpPort = httpPort;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public boolean hasInstantCommands() {
        return instantCommands;
    }

    public void setInstantCommands(boolean instantCommands) {
        this.instantCommands = instantCommands;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public boolean isValid() {
        return siteKey != null && !siteKey.isEmpty() && siteUrl != null && !siteUrl.isEmpty();
    }

    @Override
    public String toString() {
        return "PluginConfig{siteKey='" + siteKey + "', siteUrl='" + siteUrl + "', httpPort=" + httpPort + '}';
    }
}
