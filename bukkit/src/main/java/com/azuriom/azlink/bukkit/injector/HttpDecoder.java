package com.azuriom.azlink.bukkit.injector;

import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.http.server.HttpHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * This file is based on JSONAPI by Alec Gorge, under the MIT license.
 *
 * https://github.com/alecgorge/jsonapi/blob/master/src/main/java/com/alecgorge/minecraft/jsonapi/packets/netty/JSONAPIChannelDecoder.java
 */
public class HttpDecoder extends ByteToMessageDecoder {

    // Since Spigot 1.19, netty-codec-http is no longer included in SpigotMC.
    // PaperMC must be used instead to have instant commands.
    private final boolean supported = isSupported();

    private final AzLinkPlugin plugin;

    public HttpDecoder(AzLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // use 4 bytes to detect HTTP or abort
        if (in.readableBytes() < 4) {
            return;
        }

        in.retain(2);
        in.retain(2);

        int magic1 = in.getUnsignedByte(in.readerIndex());
        int magic2 = in.getUnsignedByte(in.readerIndex() + 1);
        int magic3 = in.getUnsignedByte(in.readerIndex() + 2);
        int magic4 = in.getUnsignedByte(in.readerIndex() + 3);
        ChannelPipeline pipeline = ctx.channel().pipeline();

        if (!isHttp(magic1, magic2, magic3, magic4)) {
            try {
                pipeline.remove(this);
            } catch (NoSuchElementException e) {
                // probably okay, it just needs to be off
            }

            in.release();
            in.release();

            return;
        }

        ByteBuf copy = in.copy();
        ctx.channel().config().setOption(ChannelOption.TCP_NODELAY, true);

        try {
            while (pipeline.removeLast() != null);
        } catch (NoSuchElementException e) {
            // ignore
        }

        if (!supported) {
            logUnsupported();
            return;
        }

        pipeline.addLast("codec-http", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("handler", new HttpHandler(this.plugin));

        pipeline.fireChannelRead(copy);
        in.release();
        in.release();
    }

    private void logUnsupported() {
        this.plugin.getLogger().error("AzLink is not compatible with your server software, please use Paper instead!");
        this.plugin.getLogger().error("Paper offers significant performance improvements, bug fixes, security");
        this.plugin.getLogger().error("enhancements and optional features for server owners to enhance their server.");
        this.plugin.getLogger().error("");
        this.plugin.getLogger().error("Download: https://papermc.io/downloads");
    }

    private boolean isHttp(int magic1, int magic2, int magic3, int magic4) {
        return magic1 == 'G' && magic2 == 'E' && magic3 == 'T' && magic4 == ' ' || // GET
                magic1 == 'P' && magic2 == 'O' && magic3 == 'S' && magic4 == 'T'; // POST
    }

    private boolean isSupported() {
        try {
            Class.forName("io.netty.handler.codec.http.HttpServerCodec");

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
