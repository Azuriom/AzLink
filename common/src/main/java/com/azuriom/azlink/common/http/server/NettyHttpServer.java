package com.azuriom.azlink.common.http.server;

import com.azuriom.azlink.common.AzLinkPlugin;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class NettyHttpServer implements HttpServer {

    private final AzLinkPlugin plugin;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private Channel channel;

    public NettyHttpServer(AzLinkPlugin plugin) {
        this.plugin = plugin;

        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }

    @Override
    public void start() {
        this.plugin.getLogger().info("Starting HTTP server");

        int port = this.plugin.getConfig().getHttpPort();

        if (port < 1 || port > 65535) {
            port = HttpServer.DEFAULT_PORT;
        }

        InetSocketAddress address = new InetSocketAddress(port);

        new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(this.bossGroup, this.workerGroup)
                .childHandler(new HttpChannelInitializer(this.plugin))
                .bind(address)
                .addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        this.plugin.getLogger().error("Unable to start the HTTP server on " + address, future.cause());
                        return;
                    }

                    this.channel = future.channel();

                    this.plugin.getLogger().info("HTTP server started on " + future.channel().localAddress());
                });
    }

    @Override
    public void stop() {
        try {
            if (this.channel != null) {
                this.channel.close().syncUninterruptibly();
            }
        } catch (Exception e) {
            this.plugin.getLogger().error("An error occurred while stopping HTTP server", e);
        }

        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
    }
}
