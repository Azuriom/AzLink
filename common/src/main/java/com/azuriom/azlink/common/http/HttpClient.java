package com.azuriom.azlink.common.http;

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
import java.util.Base64;

public class HttpClient {

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> chain.proceed(addHeadersToRequest(chain.request())))
            .build();

    private final AzLinkPlugin plugin;

    public HttpClient(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public Response getStatus() throws IOException {
        return makeCall(new Request.Builder().url(getSiteUrl()).build());
    }

    public WebsiteResponse postData(ServerData data) throws IOException {
        Request request = new Request.Builder().url(getSiteUrl())
                .post(RequestBody.create(JSON_TYPE, data.toString()))
                .build();

        try (Response response = makeCall(request)) {
            try (ResponseBody body = response.body()) {
                if (body == null) {
                    throw new RuntimeException("No body in response");
                }

                try (InputStream is = body.byteStream()) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
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

    private Request addHeadersToRequest(Request request) {
        byte[] key = plugin.getConfig().getSiteKey().getBytes(StandardCharsets.UTF_8);
        String keyEncoded = Base64.getEncoder().encodeToString(key);

        return request.newBuilder()
                .header("Authorization", "Basic " + keyEncoded)
                .header("User-Agent", "AzLink v" + plugin.getPlatform().getPluginVersion())
                .build();
    }

    private String getSiteUrl() {
        return plugin.getConfig().getSiteUrl() + "/api/v1/azlink";
    }
}
