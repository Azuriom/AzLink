package com.azuriom.azlink.common.config;

public class PluginConfig {

    private final String siteKey;
    private final String siteUrl;

    public PluginConfig(String siteKey, String siteUrl) {
        this.siteKey = siteKey;
        this.siteUrl = siteUrl;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public boolean isValid() {
        return siteKey != null && !siteKey.isEmpty() && siteUrl != null && !siteUrl.isEmpty();
    }
}
