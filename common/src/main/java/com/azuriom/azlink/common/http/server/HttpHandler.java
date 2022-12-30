package com.azuriom.azlink.common.http.server;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.utils.Hash;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final AzLinkPlugin plugin;

    public HttpHandler(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation") // Request#uri and Request#method don't exits on old Netty versions
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.getUri();
        HttpMethod method = request.getMethod();

        if (!uri.equals("/")) {
            close(ctx, writeResponse(HttpResponseStatus.NOT_FOUND, "Error: Not Found"));
            return;
        }

        if (method == HttpMethod.GET) {
            close(ctx, writeResponse(HttpResponseStatus.OK, "Status: OK"));
            return;
        }

        if (method == HttpMethod.POST) {
            if (!this.plugin.getConfig().isValid()) {
                close(ctx, writeResponse(HttpResponseStatus.SERVICE_UNAVAILABLE, "Error: Invalid configuration"));
                return;
            }

            String siteKeyHash = Hash.SHA_256.hash(this.plugin.getConfig().getSiteKey());

            if (!siteKeyHash.equals(request.headers().get("Authorization"))) {
                close(ctx, writeResponse(HttpResponseStatus.FORBIDDEN, "Error: Invalid authorization"));
                return;
            }

            this.plugin.fetch();

            close(ctx, writeResponse(HttpResponseStatus.OK, "Status: OK"));

            return;
        }

        close(ctx, writeResponse(HttpResponseStatus.METHOD_NOT_ALLOWED, "Error: Method Not Allowed"));
    }

    private FullHttpResponse writeResponse(HttpResponseStatus status, String content) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.content().writeBytes(content.getBytes(StandardCharsets.UTF_8));
        return response;
    }

    private void close(ChannelHandlerContext ctx, FullHttpResponse response) {
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
