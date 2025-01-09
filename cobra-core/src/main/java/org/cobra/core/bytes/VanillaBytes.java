package org.cobra.core.bytes;

import java.nio.ByteBuffer;

public abstract class VanillaBytes implements Bytes {

    protected long position;
    protected BytesStore store;

    protected VanillaBytes(BytesStore store) {
        this.store = store;
    }

    private void checkWriteOffset(long pos) {
        if (pos >= this.store.size())
            this.store.attemptResizing(pos);
    }

    private void ensureOffsetInBound(long pos) {
        if (pos >= this.store.size())
            throw new IllegalArgumentException(("Required offset is out of bound; " +
                    "required = %d; size = %d").formatted(pos, this.store.size()));
    }

    @Override
    public void resizing(long toSize) {
        checkWriteOffset(toSize);
    }

    @Override
    public long position() {
        return this.position;
    }

    @Override
    public void position(long pos) {
        ensureOffsetInBound(pos);
        this.position = pos;
    }

    @Override
    public long size() {
        return this.store.size();
    }

    @Override
    public void writeAt(long pos, byte b) {
        checkWriteOffset(pos);
        this.store.writeAt(pos, b);
    }

    @Override
    public void writeAt(long pos, byte[] array) {
        final int len = array.length;
        checkWriteOffset(pos + len);

        writeAt(pos, array, 0, len);
    }

    @Override
    public void writeAt(long pos, byte[] array, int offset, int len) {
        checkWriteOffset(pos + len);
        this.store.writeAt(pos, array, offset, len);
    }

    @Override
    public void writeAt(long pos, ByteBuffer buffer) {
        checkWriteOffset(pos + buffer.remaining());
        this.store.writeAt(pos, buffer);
    }

    @Override
    public void writeAt(long pos, ByteBuffer buffer, int offset, int len) {
        checkWriteOffset(pos + len);
        this.store.writeAt(pos, buffer, offset, len);
    }

    @Override
    public void write(byte b) {
        checkWriteOffset(this.position);
        this.store.writeAt(this.position++, b);
    }

    @Override
    public void write(byte[] array) {
        final int len = array.length;
        checkWriteOffset(this.position + len);

        this.store.writeAt(position, array);
        this.position += len;
    }

    @Override
    public void write(byte[] array, int offset, int len) {
        checkWriteOffset(this.position + len);

        this.store.writeAt(position, array, offset, len);
        this.position += len;
    }

    @Override
    public void write(ByteBuffer buffer) {
        checkWriteOffset(this.position + buffer.remaining());

        this.store.writeAt(position, buffer);
        this.position += buffer.remaining();
    }

    @Override
    public void write(ByteBuffer buffer, int offset, int len) {
        checkWriteOffset(this.position + len);

        this.store.writeAt(position, buffer, offset, len);
        this.position += len;
    }

    @Override
    public byte readAt(long pos) {
        ensureOffsetInBound(pos);
        return this.store.readAt(pos);
    }

    @Override
    public int readAt(long pos, byte[] dest) {
        ensureOffsetInBound(pos + dest.length);
        return this.store.readAt(pos, dest);
    }

    @Override
    public int readAt(long pos, byte[] dest, int offset, int len) {
        ensureOffsetInBound(pos + len);
        return this.store.readAt(pos, dest, offset, len);
    }

    @Override
    public int readAt(long pos, ByteBuffer dest) {
        ensureOffsetInBound(pos + dest.remaining());
        return this.store.readAt(pos, dest);
    }

    @Override
    public int readAt(long pos, ByteBuffer dest, int offset, int len) {
        ensureOffsetInBound(pos + len);
        return this.store.readAt(pos, dest, offset, len);
    }

    @Override
    public byte read() {
        ensureOffsetInBound(this.position);
        return this.store.readAt(this.position++);
    }

    @Override
    public int read(byte[] array) {
        ensureOffsetInBound(this.position + array.length);
        int result = this.store.readAt(position, array);
        this.position += array.length;

        return result;
    }

    @Override
    public int read(byte[] array, int offset, int len) {
        ensureOffsetInBound(this.position + len);
        int result = this.store.readAt(position, array, offset, len);
        this.position += len;

        return result;
    }

    @Override
    public int read(ByteBuffer buffer) {
        ensureOffsetInBound(this.position + buffer.remaining());
        int result = this.store.readAt(position, buffer);
        this.position += buffer.remaining();

        return result;
    }

    @Override
    public int read(ByteBuffer buffer, int offset, int len) {
        ensureOffsetInBound(this.position + len);
        int result = this.store.readAt(position, buffer, offset, len);
        this.position += len;

        return result;
    }

    @Override
    public void rewind() {
        this.position = 0;
    }
}
