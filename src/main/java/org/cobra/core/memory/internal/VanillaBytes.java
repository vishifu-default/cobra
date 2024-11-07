package org.cobra.core.memory.internal;

import org.cobra.commons.Jvm;
import org.cobra.core.memory.BytesStore;

import java.nio.ByteBuffer;

public abstract class VanillaBytes implements Bytes {

    private static final boolean SKIP_ASSERT = Jvm.SKIP_ASSERTION;

    protected long position = 0L;
    protected final BytesStore bytesStore;

    protected VanillaBytes(BytesStore bytesStore) {
        this.bytesStore = bytesStore;
    }

    @Override
    public void write(byte[] arr) {
        write(arr, 0, arr.length);
    }

    @Override
    public void write(byte[] arr, int off, int len) {
        write(this.position, arr, off, len);
        this.position += len;
    }

    @Override
    public void write(long pos, byte[] array) {
        write(pos, array, 0, array.length);
    }

    @Override
    public void write(long pos, byte[] array, int offset, int len) {
        checkWriteOffset(pos, len);
        this.bytesStore.putBytes(pos, array, offset, len);
    }

    @Override
    public void write(ByteBuffer buffer) {
        write(this.position, buffer);
        this.position += buffer.remaining();
    }

    @Override
    public void write(long pos, ByteBuffer buffer) {
        checkWriteOffset(pos, buffer.remaining());
        byte[] arr = buffer.slice(buffer.position(), buffer.remaining()).array();
        write(pos, arr);
    }

    @Override
    public int read(byte[] arr) {
        return read(this.position, arr, 0, arr.length);
    }

    @Override
    public int read(byte[] arr, int off, int len) {
        int result = read(this.position, arr, off, len);
        this.position += len;

        return result;
    }

    @Override
    public int read(long pos, byte[] array) {
        return read(pos, array, 0, array.length);
    }

    @Override
    public int read(long pos, byte[] array, int offset, int len) {
        ensureOffsetInBound(pos, len);
        return this.bytesStore.getBytes(pos, array, offset, len);
    }

    @Override
    public int read(ByteBuffer buffer) {
        int result = read(this.position, buffer);
        this.position += buffer.remaining();
        return result;
    }

    @Override
    public int read(long pos, ByteBuffer buffer) {
        ensureOffsetInBound(pos, buffer.remaining());
        byte[] arr = new byte[buffer.remaining()];
        int result = read(pos, arr);

        buffer.put(arr);
        return result;
    }

    @Override
    public long capacity() {
        return this.bytesStore.capacity();
    }

    public long position() {
        return this.position;
    }

    public void position(long pos) {
        assert SKIP_ASSERT || (pos >= 0 && pos < capacity());
        this.position = pos;
    }

    public void checkResize(long requiredPos) {
        this.bytesStore.checkResizing(requiredPos);
    }

    private void checkWriteOffset(long offset, int len) {
        assert SKIP_ASSERT || (offset >= 0 && len >= 0);

        if ((offset + len) <= capacity())
            return;

        checkResize(offset + len);
        ensureOffsetInBound(offset, len);
    }

    private void ensureOffsetInBound(long offset, int len) {
        assert SKIP_ASSERT || (offset >= 0 && len >= 0);

        if ((offset + len) > capacity())
            throw new IllegalArgumentException(String.format("[%d:%d+%d) out of bound capacity=%d",
                    offset, offset, len, capacity()));
    }
}
