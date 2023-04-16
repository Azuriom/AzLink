package com.azuriom.azlink.common.http.client;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.data.ServerData;
import com.azuriom.azlink.common.data.UserInfo;
import com.azuriom.azlink.common.data.WebsiteResponse;
import com.azuriom.azlink.common.users.EditMoneyResult;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class HttpClient {

    private static final int CONNECT_TIMEOUT = 5000; // 5 seconds
    private static final int REQUEST_TIMEOUT = 1000; // 1 second

    private final AzLinkPlugin plugin;

    public HttpClient(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> verifyStatus() {
        return request(RequestMethod.GET, "/azlink", null);
    }

    public CompletableFuture<Void> registerUser(String name, String email, UUID uuid, String password, InetAddress address) {
        JsonObject params = new JsonObject();
        params.addProperty("name", name);
        params.addProperty("email", email);
        params.addProperty("game_id", uuid.toString());
        params.addProperty("password", password);
        params.addProperty("ip", address != null ? address.getHostAddress() : null);

        return request(RequestMethod.POST, "/azlink/register", params);
    }

    public CompletableFuture<Void> updateEmail(UUID uuid, String email) {
        JsonObject params = new JsonObject();
        params.addProperty("game_id", uuid.toString());
        params.addProperty("email", email);

        return request(RequestMethod.POST, "/azlink/email", params);
    }

    public CompletableFuture<EditMoneyResult> editMoney(UserInfo user, String action, double amount) {
        String endpoint = "/azlink/user/" + user.getId() + "/money/" + action;
        JsonObject params = new JsonObject();
        params.addProperty("amount", amount);

        return request(RequestMethod.POST, endpoint, params, EditMoneyResult.class);
    }

    public CompletableFuture<WebsiteResponse> postData(ServerData data) {
        return request(RequestMethod.POST, "/azlink", data, WebsiteResponse.class);
    }

    public CompletableFuture<Void> request(RequestMethod method, String endpoint, Object params) {
        return request(method, endpoint, params, Void.class);
    }

    public <T> CompletableFuture<T> request(RequestMethod method, String endpoint, Object params, Class<T> clazz) {
        String body = AzLinkPlugin.getGson().toJson(params);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return rawRequest(method, endpoint, body, clazz);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, this.plugin.getScheduler().asyncExecutor());
    }

    private <T> T rawRequest(RequestMethod method, String endpoint, String body, Class<T> clazz)
            throws IOException {
        HttpURLConnection conn = prepareConnection(method, endpoint);

        if (method != RequestMethod.GET && body != null && !body.isEmpty()) {
            conn.setDoOutput(true);

            try (OutputStream out = conn.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        if (conn.getResponseCode() >= 400) {
            int status = conn.getResponseCode();
            String message = conn.getResponseMessage();

            throw new IOException("Invalid response code (" + status + " - " + message + ")");
        }

        if (clazz == null || clazz == Void.class) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            T response = AzLinkPlugin.getGson().fromJson(reader, clazz);

            if (response == null) {
                throw new IllegalStateException("Empty JSON response from API.");
            }

            return response;
        }
    }

    private HttpURLConnection prepareConnection(RequestMethod method, String endpoint) throws IOException {
        String baseUrl = this.plugin.getConfig().getSiteUrl();
        String version = this.plugin.getPlatform().getPluginVersion();
        String token = this.plugin.getConfig().getSiteKey();
        URL url = new URL(baseUrl + "/api" + endpoint);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(REQUEST_TIMEOUT);
        conn.setRequestMethod(method.name());
        conn.addRequestProperty("Azuriom-Link-Token", token);
        conn.addRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.addRequestProperty("User-Agent", "AzLink java v" + version);

        return conn;
    }

    public enum RequestMethod {
        GET, POST, PATCH, PUT, DELETE
    }
}
