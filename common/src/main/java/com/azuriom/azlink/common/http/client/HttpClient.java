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

public class HttpClient {

    public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> chain.proceed(addHeadersToRequest(chain.request())))
            .build();

    private final AzLinkPlugin plugin;

    public HttpClient(AzLinkPlugin plugin) {
        this.plugin = plugin;
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
                .post(RequestBody.create(JSON_TYPE, AzLinkPlugin.getGson().toJson(data)))
                .build();

        try (Response response = makeCall(request)) {
            ResponseBody body = response.body();

            if (body == null) {
                throw new RuntimeException("No body in response");
            }

            try (BufferedReader reader = new BufferedReader(body.charStream())) {
                return AzLinkPlugin.getGson().fromJson(reader, WebsiteResponse.class);
            }
        }
    }

    public Response makeCall(Request request) throws IOException {
        Response response = this.httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            response.close();
            throw new IOException("Invalid response: " + response.code() + " (" + response.message() + ")");
        }

        return response;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    private Request addHeadersToRequest(Request request) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + this.plugin.getConfig().getSiteKey())
                .header("Azuriom-Link-Token", this.plugin.getConfig().getSiteKey())
                .header("User-Agent", "AzLink v" + this.plugin.getPlatform().getPluginVersion())
                .build();
    }

    private String getSiteUrl() {
        return this.plugin.getConfig().getSiteUrl() + "/api/azlink";
    }
}
