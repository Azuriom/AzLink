package com.azuriom.azlink.common.utils;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.util.Objects;

public class UpdateChecker {

    private static final String RELEASE_URL = "https://api.github.com/repos/Azuriom/AzLink/releases/latest";

    private final AzLinkPlugin plugin;

    public UpdateChecker(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkUpdates() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(RELEASE_URL).build();

            try (Response response = client.newCall(request).execute()) {
                ResponseBody body = response.body();

                if (!response.isSuccessful() || body == null) {
                    return;
                }

                try (BufferedReader reader = new BufferedReader(body.charStream())) {
                    JsonObject json = AzLinkPlugin.getGson().fromJson(reader, JsonObject.class);

                    JsonElement lastVersionJson = json.get("tag_name");

                    if (lastVersionJson == null) {
                        return;
                    }

                    String currentVersion = this.plugin.getPlatform().getPluginVersion();
                    String lastVersion = lastVersionJson.getAsString();

                    if (compareVersions(lastVersion, currentVersion) > 0) {
                        this.plugin.getLogger().warn("A new update of AzLink is available: " + lastVersion);
                        this.plugin.getLogger().warn("You can download it on https://azuriom.com/azlink");
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public static int compareVersions(String version1, String version2) {
        Objects.requireNonNull(version1, "version1");
        Objects.requireNonNull(version2, "version2");

        String[] version1Parts = parseVersion(version1).split("\\.");
        String[] version2Parts = parseVersion(version2).split("\\.");
        int maxLength = Math.max(version1Parts.length, version2Parts.length);

        for (int i = 0; i < maxLength; i++) {
            int v1 = i < version1Parts.length ? Integer.parseInt(version1Parts[i]) : 0;
            int v2 = i < version2Parts.length ? Integer.parseInt(version2Parts[i]) : 0;
            int compare = Integer.compare(v1, v2);

            if (compare != 0) {
                return compare;
            }
        }

        return 0;
    }

    private static String parseVersion(String version) {
        return version.replace("v", "").replace("-SNAPSHOT", "");
    }
}
