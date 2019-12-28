package com.azuriom.azlink.common.http.server;

import com.azuriom.azlink.common.AzLinkPlugin;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpServer {

    public static final int DEFAULT_PORT = 25588;

    private final AzLinkPlugin plugin;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private ChannelFuture channelFuture;

    public HttpServer(AzLinkPlugin plugin) {
        this.plugin = plugin;

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
    }

    public void start() throws Exception {
        int port = plugin.getConfig().getHttpPort();

        if (port < 1 || port > 65535) {
            port = DEFAULT_PORT;
        }

        ServerBootstrap bootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpChannelInitializer(plugin))
                .group(bossGroup, workerGroup);

        channelFuture = bootstrap.bind(port).sync();

        plugin.getLogger().info("HTTP server started on port " + port);
    }

    public void startSafe() {
        try {
            start();
        } catch (Exception e) {
            plugin.getLogger().error("An error occurred while starting HTTP server", e);
        }
    }

    public void stop() throws Exception {
        if (channelFuture != null) {
            channelFuture.channel().closeFuture().sync();
        }

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public void stopSafe() {
        try {
            stop();
        } catch (Exception e) {
            plugin.getLogger().error("An error occurred while stopping HTTP server", e);
        }
    }
}
