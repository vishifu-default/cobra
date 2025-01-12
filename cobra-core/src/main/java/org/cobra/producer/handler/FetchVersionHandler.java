package org.cobra.producer.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.cobra.networks.Apikey;
import org.cobra.producer.AbstractProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

@ChannelHandler.Sharable
public class FetchVersionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(FetchVersionHandler.class);

    private final AbstractProducer baseProducer;

    public FetchVersionHandler(AbstractProducer baseProducer) {
        this.baseProducer = baseProducer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("activate channel");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuffer buffer = ((ByteBuf) msg).nioBuffer();
        if (buffer.hasRemaining() && buffer.get(0) == Apikey.FETCH_VERSION.id()) {

            while (!baseProducer.isBootstrapped()) {
                if (baseProducer.isBootstrapped())
                    break;
            }

            long version = baseProducer.currentVersion();

            ByteBuffer buf = ByteBuffer.allocate(4 + 8);
            buf.putInt(Long.BYTES);
            buf.putLong(version)
                    .flip();

            ctx.writeAndFlush(Unpooled.wrappedBuffer(buf));

            ((ByteBuf) msg).release();
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("exception caught", cause);
    }
}
