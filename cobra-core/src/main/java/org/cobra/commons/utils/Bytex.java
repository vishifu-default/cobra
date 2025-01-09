package org.cobra.commons.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Bytex {

    public static byte[] toArray(ByteBuffer buffer) {
        return toArray(buffer, buffer.remaining());
    }

    public static byte[] toArray(ByteBuffer buffer, int len) {
        return toArray(buffer, 0, len);
    }

    public static byte[] toArray(ByteBuffer buffer, int offset, int len) {
        byte[] dest = new byte[len];

        if (buffer.hasArray())
            System.arraycopy(buffer.array(), buffer.position() + offset, dest, 0, len);
        else {
            int pos = buffer.position();
            buffer.position(pos + offset);
            buffer.get(dest);
            buffer.position(pos); // reset to position before reading
        }

        return dest;
    }

    public static String utf8(ByteBuffer buffer) {
        return utf8(buffer, buffer.remaining());
    }

    public static String utf8(ByteBuffer buffer, int len) {
        return utf8(buffer, 0, len);
    }

    public static String utf8(ByteBuffer buffer, int offset, int len) {
        if (buffer.hasArray())
            return new String(buffer.array(), buffer.arrayOffset() + buffer.position() + offset, len,
                    StandardCharsets.UTF_8);

        return utf8(toArray(buffer, offset, len));
    }

    public static byte[] utf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String utf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String readUtf8(ByteBuffer buffer, int len) {
        byte[] dest = new byte[len];
        buffer.get(dest);
        return utf8(dest);
    }
}
