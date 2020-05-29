package com.azuriom.azlink.common.http.client;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.data.ServerData;
import com.azuriom.azlink.common.data.WebsiteResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HttpClient {

    public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> chain.proceed(addHeadersToRequest(chain.request())))
            .build();

    private final AzLinkPlugin plugin;
    private final String userAgent;

    public HttpClient(AzLinkPlugin plugin) {
        this.plugin = plugin;
        this.userAgent = "AzLink v" + plugin.getPlatform().getPluginVersion();
    }

    public void verifyStatus() throws IOException {
        try (Response response = getStatus()) {
            // success
        }
    }

    public Response getStatus() throws IOException {
        return makeCall(new Request.Builder().url(getSiteUrl()).build());
    }

    public WebsiteResponse postData(ServerData data) throws IOException {
        Request request = new Request.Builder().url(getSiteUrl())
                .post(RequestBody.create(JSON_TYPE, plugin.getGson().toJson(data)))
                .build();

        try (Response response = makeCall(request)) {
            try (ResponseBody body = response.body()) {
                if (body == null) {
                    throw new RuntimeException("No body in response");
                }

                try (InputStream in = body.byteStream()) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                        return plugin.getGson().fromJson(reader, WebsiteResponse.class);
                    }
                }
            }
        }
    }

    public Response makeCall(Request request) throws IOException {
        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Invalid response: " + response.code() + " (" + response.message() + ")");
        }

        return response;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    private Request addHeadersToRequest(Request request) {
        byte[] key = plugin.getConfig().getSiteKey().getBytes(StandardCharsets.UTF_8);
        String keyEncoded = Base64.getEncoder().encodeToString(key);

        return request.newBuilder()
                .header("Authorization", "Bearer " + plugin.getConfig().getSiteKey())
                .header("User-Agent", this.userAgent)
                .build();
    }

    private String getSiteUrl() {
        return plugin.getConfig().getSiteUrl() + "/api/azlink";
    }
}
