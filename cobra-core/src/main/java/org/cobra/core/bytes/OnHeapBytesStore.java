package org.cobra.core.bytes;

import org.cobra.commons.Jvm;
import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.utils.Bytex;
import org.cobra.core.memory.OSMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class OnHeapBytesStore implements BytesStore {

    private static final Logger log = LoggerFactory.getLogger(OnHeapBytesStore.class);
    private static final OSMemory memory = Jvm.osMemory();

    private static final boolean SKIP_ASSERT = Jvm.SKIP_ASSERTION;
    private static final int DEFAULT_LOG2_OF_SEGMENT = 14;
    private static final long HEAP_LIMIT_SIZE = 1L << 60;

    private static final String INDEX_OUT_OF_BOUND = "Required an index out of bound";


    private byte[][] arena;
    private long size;

    private final int bitshiftAlign;
    private final int bitmaskAlign;
    private final BytesPool bytesPool;

    private OnHeapBytesStore(int log2Align, BytesPool bytesPool) {
        this.bitshiftAlign = log2Align;
        this.bitmaskAlign = (1 << log2Align) - 1;
        this.bytesPool = bytesPool;

        this.arena = new byte[2][];

        /* allocate for segment(0) */
        final int arraySize = 1 << log2Align;
        this.arena[0] = bytesPool.allocateArray(arraySize);
        this.size += arraySize;
    }

    public static OnHeapBytesStore create() {
        return create(DEFAULT_LOG2_OF_SEGMENT, BytesPool.NONE);
    }

    public static OnHeapBytesStore create(BytesPool bytesPool) {
        return create(DEFAULT_LOG2_OF_SEGMENT, bytesPool);
    }

    public static OnHeapBytesStore create(int log2Align) {
        return create(log2Align, BytesPool.NONE);
    }

    public static OnHeapBytesStore create(int log2Align, BytesPool bytesPool) {
        return new OnHeapBytesStore(log2Align, bytesPool);
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public void attemptResizing(long toSize) {
        if (toSize > HEAP_LIMIT_SIZE)
            throw new IllegalArgumentException("Exceed HEAP_LIMIT_SIZE, which is 2^60");

        if (toSize < size())
            return;

        final TranslatedAddress address = translate(toSize);

        final long startWatchMs = System.currentTimeMillis();
        while (this.arena.length <= address.index()) {
            this.arena = Arrays.copyOf(this.arena, this.arena.length * 3 / 2);
        }

        for (int i = 0; i <= address.index(); i++) {
            if (this.arena[i] != null)
                continue;

            this.arena[i] = this.bytesPool.allocateArray(1 << this.bitshiftAlign);
            this.size += this.arena[i].length;
        }

        final long elapsedMs = System.currentTimeMillis() - startWatchMs;
        log.debug("grow on-heap store to {} in {}ms; arena_segs={}", this.size, elapsedMs, this.arena.length);
    }

    @Override
    public void destroy() {
        this.size = 0;
        for (byte[] seg : this.arena) {
            if (seg == null)
                continue;
            this.bytesPool.free(seg);
        }
        this.arena = null;
    }

    @Override
    public void writeAt(long pos, byte b) {
        ensureNotOutBound(pos);
        final TranslatedAddress address = translate(pos);
        memory.writeByte(this.arena[address.index()], translateOffset(address.offset()), b);
    }

    @Override
    public void writeAt(long pos, byte[] array) {
        writeAt(pos, array, 0, array.length);
    }

    @Override
    public void writeAt(long pos, byte[] array, int offset, int len) {
        assert SKIP_ASSERT || pos >= 0;
        assert SKIP_ASSERT || (offset >= 0 && len >= 0);

        if (len == 0)
            return;

        ensureNotOutBound(pos + len);
        if (offset + len > array.length)
            throw new IndexOutOfBoundsException(INDEX_OUT_OF_BOUND +
                    "; [%d : %d + %d) out of bound array len %d".formatted(offset, offset, len, array.length));

        while (len > 0) {
            final TranslatedAddress address = translate(pos);
            final byte[] segment = this.arena[address.index()];
            final int copiesLoop = Math.min(len, (segment.length - address.offset()));

            memory.copyMemory(array, offset, segment, address.offset(), copiesLoop);


            len -= copiesLoop;
            pos += copiesLoop;
            offset += copiesLoop;
        }
    }

    @Override
    public void writeAt(long pos, ByteBuffer buffer) {
        assert SKIP_ASSERT || pos >= 0;
        assert SKIP_ASSERT || buffer != null;

        writeAt(pos, buffer, 0, buffer.remaining());
    }

    @Override
    public void writeAt(long pos, ByteBuffer buffer, int offset, int len) {
        assert SKIP_ASSERT || pos >= 0;
        assert SKIP_ASSERT || (offset >= 0 && len >= 0);

        if (offset + len > buffer.remaining())
            throw new IndexOutOfBoundsException(INDEX_OUT_OF_BOUND +
                    "; [%d : %d + %d) out bound of %d".formatted(offset, offset, len, buffer.remaining()));

        byte[] array = Bytex.toArray(buffer, offset, len);
        writeAt(pos, array, 0, array.length);
    }

    @Override
    public byte readAt(long pos) {
        assert SKIP_ASSERT || pos >= 0;

        ensureNotOutBound(pos);

        final TranslatedAddress address = translate(pos);
        return memory.readByte(this.arena[address.index()], translateOffset(address.offset()));
    }

    @Override
    public int readAt(long pos, byte[] dest) {
        assert SKIP_ASSERT || pos >= 0;

        return readAt(pos, dest, 0, dest.length);
    }

    @Override
    public int readAt(long pos, byte[] dest, int offset, int len) {
        assert SKIP_ASSERT || pos >= 0;
        assert SKIP_ASSERT || (offset >= 0 && len >= 0);

        if (len == 0)
            return 0;

        final int needReads = Math.min((int) (size() - pos), len);

        if (offset + needReads > dest.length)
            return -1;

        int i = 0;
        while (i < needReads) {
            final TranslatedAddress address = translate(pos);
            final byte[] segment = this.arena[address.index()];
            final int copiesLoop = Math.min(needReads, segment.length - address.offset());

            memory.copyMemory(segment, address.offset(), dest, i, copiesLoop);

            i += copiesLoop;
            pos += copiesLoop;
        }

        return needReads;
    }

    @Override
    public int readAt(long pos, ByteBuffer dest) {
        assert SKIP_ASSERT || pos >= 0;
        assert SKIP_ASSERT || dest != null;

        return readAt(pos, dest, 0, dest.remaining());
    }

    @Override
    public int readAt(long pos, ByteBuffer dest, int offset, int len) {
        assert SKIP_ASSERT || pos >= 0;
        assert SKIP_ASSERT || (offset >= 0 && len >= 0);

        ensureNotOutBound(pos + len);

        byte[] array = Bytex.toArray(dest, offset, len);
        int reads = readAt(pos, array, 0, array.length);

        dest.put(array, offset, array.length);

        return reads;
    }

    private void ensureNotOutBound(long pos) {
        if (pos > this.size)
            throw new IndexOutOfBoundsException(INDEX_OUT_OF_BOUND +
                    "; required=%d; size=%d".formatted(pos, this.size));
    }

    private int translateOffset(int offset) {
        return OSMemory.ARRAY_BYTE_BASE_OFFSET + offset;
    }

    private TranslatedAddress translate(long pos) {
        int segmentIndex = (int) (pos >>> this.bitshiftAlign);
        int offset = (int) (pos & this.bitmaskAlign);
        return new TranslatedAddress(segmentIndex, offset);
    }
}
