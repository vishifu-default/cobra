package org.cobra.producer.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import org.cobra.commons.errors.CobraException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ChannelContextFileTransfers {
    public static void zeroCopy(Path filepath, ChannelHandlerContext context) {

        RandomAccessFile raf = null;
        int len = -1;
        try {
            raf = new RandomAccessFile(filepath.toFile(), "r");
            len = (int) raf.length();
        } catch (IOException e) {
            throw new CobraException(e);
        } finally {
            if (len < 0 && raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    throw new CobraException(e);
                }
            }
        }

        ByteBuffer sizeBuffer = ByteBuffer.allocate(Integer.BYTES);
        sizeBuffer.putInt(len);

        // zero copy
        context.writeAndFlush(Unpooled.wrappedBuffer(sizeBuffer.rewind()));
        context.writeAndFlush(new DefaultFileRegion(raf.getChannel(), 0, len));
    }
}
