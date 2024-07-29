package com.azuriom.azlink.bukkit.injector;

import com.azuriom.azlink.bukkit.AzLinkBukkitPlugin;
import com.azuriom.azlink.common.http.server.HttpServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.NoSuchElementException;

public class InjectedHttpServer implements HttpServer {

    private final ChannelHandler serverChannelHandler = createChannelHandler();

    private final AzLinkBukkitPlugin plugin;

    private ChannelFuture serverChannel;

    public InjectedHttpServer(AzLinkBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        // Make sure Netty isn't relocated
        if (!HttpDecoder.class.getSuperclass().getName().startsWith("io.")) {
            plugin.getLoggerAdapter().error("Injecting HTTP server on server channel is not supported with AzLink legacy.");
            return;
        }

        if (!Bukkit.getServer().getClass().getSimpleName().equals("CraftServer")) {
            plugin.getLoggerAdapter().error("Injecting HTTP server on server channel is only supported on CraftBukkit based servers. You can use an other port for AzLink.");
            return;
        }

        try {
            inject();
        } catch (Exception e) {
            plugin.getLoggerAdapter().error("Unable to inject HTTP server. Try using a different port for AzLink", e);
        }
    }

    @Override
    public void stop() {
        try {
            uninject();
        } catch (Exception e) {
            this.plugin.getLoggerAdapter().error("An error occurred while removing HTTP server", e);
        }
    }

    private void inject() throws Exception {
        Object craftServer = Bukkit.getServer();
        Method serverGetHandle = craftServer.getClass().getMethod("getServer");

        Object minecraftServer = serverGetHandle.invoke(Bukkit.getServer());
        Method getServerConnection = getServerConnectionMethod(minecraftServer);

        Object serverConnection = getServerConnection.invoke(minecraftServer);

        for (Field field : serverConnection.getClass().getDeclaredFields()) {
            if (field.getType() != List.class) {
                continue;
            }

            field.setAccessible(true);

            List<?> list = (List<?>) field.get(serverConnection);

            for (Object item : list) {
                if (!(item instanceof ChannelFuture)) {
                    break; // Not the good list, try the next one
                }

                injectServerChannel((ChannelFuture) item);

                plugin.getLoggerAdapter().info("HTTP server successfully injected.");

                return;
            }
        }

        throw new IllegalStateException("Unable to find server channel, try disabling late bind");
    }

    public void uninject() {
        if (this.serverChannel == null) {
            return; // We are not injected
        }

        Channel channel = serverChannel.channel();

        channel.eventLoop().submit(() -> {
            try {
                channel.pipeline().remove(this.serverChannelHandler);
            } catch (NoSuchElementException e) {
                // ignore
            }
        });

        serverChannel = null;
    }

    private void injectServerChannel(ChannelFuture future) {
        future.channel().pipeline().addFirst(this.serverChannelHandler);

        this.serverChannel = future;
    }

    private Method getServerConnectionMethod(Object minecraftServer) throws NoSuchMethodException {
        Class<?> serverClass = minecraftServer.getClass();

        try {
            return serverClass.getMethod("getServerConnection");
        } catch (NoSuchMethodException e) {
            for (Method method : serverClass.getMethods()) {
                String type = method.getReturnType().getSimpleName();

                if (type.equals("ServerConnection") || type.equals("ServerConnectionListener")) {
                    return method;
                }
            }
        }

        throw new NoSuchMethodException("Unable to find server connection method in " + serverClass.getName());
    }

    private ChannelHandler createChannelHandler() {
        // Handle connected channels
        ChannelInboundHandler endInitProtocol = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                try {
                    channel.eventLoop().submit(() -> channel.pipeline().addFirst(new HttpDecoder(plugin.getPlugin())));
                } catch (Exception e) {
                    plugin.getLoggerAdapter().error("Unable to init channel", e);
                }
            }
        };

        // This is executed before Minecraft's channel handler
        ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                // Our only job is to add init protocol
                channel.pipeline().addLast(endInitProtocol);
            }
        };

        return new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                Channel channel = (Channel) msg;

                // Prepare to initialize this channel
                channel.pipeline().addFirst(beginInitProtocol);

                ctx.fireChannelRead(msg);
            }
        };
    }
}
