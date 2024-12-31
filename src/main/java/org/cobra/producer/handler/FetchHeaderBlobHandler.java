package org.cobra.producer.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import org.cobra.networks.Apikey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class FetchHeaderBlobHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(FetchHeaderBlobHandler.class);

    private final Path blobStorePath;

    public FetchHeaderBlobHandler(Path blobStorePath) {
        this.blobStorePath = blobStorePath;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuffer buffer = ((ByteBuf) msg).nioBuffer();
        if (buffer.hasRemaining() && buffer.get(0) == Apikey.FETCH_HEADER.id()) {
            buffer.position(1); // skip first apikey
            final long desiredVersion = buffer.getLong();
            Path filepath = blobStorePath.resolve("header-%d".formatted(desiredVersion));

            log.debug("transfer header blob {}", filepath.toAbsolutePath());

            ChannelContextFileTransfers.zeroCopy(filepath, ctx);
            ((ByteBuf) msg).release();
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel read complete for fetch header-blob");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("exception caught", cause);
    }
}
