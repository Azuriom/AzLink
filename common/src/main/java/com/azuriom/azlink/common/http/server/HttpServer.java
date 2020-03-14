package com.azuriom.azlink.common.http.server;

import com.azuriom.azlink.common.AzLinkPlugin;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class HttpServer {

    public static final int DEFAULT_PORT = 25588;

    private final AzLinkPlugin plugin;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private Channel channel;

    public HttpServer(AzLinkPlugin plugin) {
        this.plugin = plugin;

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
    }

    public void start() {
        plugin.getLogger().info("Starting HTTP server");

        int port = plugin.getConfig().getHttpPort();

        if (port < 1 || port > 65535) {
            port = DEFAULT_PORT;
        }

        InetSocketAddress address = new InetSocketAddress(port);

        new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(bossGroup, workerGroup)
                .childHandler(new HttpChannelInitializer(plugin))
                .bind(address)
                .addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        plugin.getLogger().error("Unable to start the HTTP server on " + address, future.cause());
                        return;
                    }

                    channel = future.channel();

                    plugin.getLogger().info("HTTP server started on " + future.channel().localAddress());
                });
    }

    public void stop() {
        try {
            if (channel != null) {
                channel.close().syncUninterruptibly();
            }
        } catch (Exception e) {
            plugin.getLogger().error("An error occurred while stopping HTTP server", e);
        }

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
