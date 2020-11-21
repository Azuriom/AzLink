package com.azuriom.azlink.common.http.server;

import com.azuriom.azlink.common.AzLinkPlugin;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final AzLinkPlugin plugin;

    public HttpChannelInitializer(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("handler", new HttpHandler(this.plugin));
    }
}
