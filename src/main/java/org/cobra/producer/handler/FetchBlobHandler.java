package org.cobra.producer.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.cobra.core.objects.BlobType;
import org.cobra.networks.Apikey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.file.Path;

@ChannelHandler.Sharable
public class FetchBlobHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(FetchBlobHandler.class);

    private final Path blobStorePath;

    public FetchBlobHandler(Path blobStorePath) {
        this.blobStorePath = blobStorePath;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuffer buffer = ((ByteBuf) msg).nioBuffer();
        if (buffer.hasRemaining() && buffer.get(0) == Apikey.FETCH_BLOB.id()) {
            buffer.position(1); // skip first apikey
            final long fromVersion = buffer.getLong();
            final long toVersion = buffer.getLong();

            BlobType blobType = fromVersion < toVersion ? BlobType.DELTA_BLOB : BlobType.REVERSED_DELTA_BLOB;
            Path filepath = blobStorePath.resolve("%s-%d-%d".formatted(blobType.prefix(), fromVersion, toVersion));

            log.debug("transfer blob {}; channel: {}", filepath.toAbsolutePath(), ctx.channel());

            ChannelContextFileTransfers.zeroCopy(filepath, ctx);
            ((ByteBuf) msg).release();
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}
