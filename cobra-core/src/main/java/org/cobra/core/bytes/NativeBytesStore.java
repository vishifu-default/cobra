package org.cobra.core.bytes;

import org.cobra.commons.Jvm;
import org.cobra.commons.utils.Bytex;
import org.cobra.core.memory.OSMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class NativeBytesStore implements BytesStore {

    private static final Logger log = LoggerFactory.getLogger(NativeBytesStore.class);
    private static final OSMemory memory = Jvm.osMemory();

    private static final boolean SKIP_ASSERT = Jvm.SKIP_ASSERTION;
    private static final long NATIVE_LIMIT_SIZE = 1L << 60;
    private static final int DEFAULT_NATIVE_BLOCK_SIZE_LOG2 = 30; // 1Gib

    private long size;
    private long[] arenaAddress;
    private final int bitshiftAlign;
    private final int bitmaskAlign;

    private NativeBytesStore(int log2) {
        this.bitshiftAlign = log2;
        this.bitmaskAlign = (1 << log2) - 1;
        this.arenaAddress = new long[2];

        /* allocate address(0) */
        this.arenaAddress[0] = memory.allocate(1L << bitshiftAlign);
        this.size = 1L << bitshiftAlign;
    }

    public static NativeBytesStore create() {
        return create(DEFAULT_NATIVE_BLOCK_SIZE_LOG2);
    }

    public static NativeBytesStore create(int log2) {
        return new NativeBytesStore(log2);
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public void attemptResizing(long toSize) {
        if (toSize > NATIVE_LIMIT_SIZE)
            throw new IllegalArgumentException("Native limit exceeded");

        if (toSize < size())
            return;

        final long startWatchMs = System.currentTimeMillis();
        final int needToAdd = (int) ((toSize >>> this.bitshiftAlign) - this.arenaAddress.length) + 1;
        final int nowAddressNum = this.arenaAddress.length;
        while (this.arenaAddress.length < nowAddressNum + needToAdd) {
            this.arenaAddress = Arrays.copyOf(this.arenaAddress, this.arenaAddress.length * 3 / 2);
        }

        final long allocated = 1L << this.bitshiftAlign;
        for (int i = 0; i < nowAddressNum + needToAdd; i++) {
            if (this.arenaAddress[i] != 0)
                continue;

            this.arenaAddress[i] = memory.allocate(allocated);
            this.size += allocated;
        }

        final long elapsed = System.currentTimeMillis() - startWatchMs;
        log.debug("grow native-store to size {} in {}ms; addresses = {}", size, elapsed,
                Arrays.toString(this.arenaAddress));
    }

    @Override
    public void destroy() {
        this.size = 0;
        int size = 1 << this.bitshiftAlign;
        for (long addr : this.arenaAddress) {
            if (addr != 0)
                memory.freeMemory(addr, size);
        }

        this.arenaAddress = null;
    }

    @Override
    public void writeAt(long pos, byte b) {
        assert SKIP_ASSERT || pos >= 0;

        ensureNotOutBound(pos);

        memory.writeByte(translate(pos), b);
    }

    @Override
    public void writeAt(long pos, byte[] array) {
        assert SKIP_ASSERT || pos >= 0;

        writeAt(pos, array, 0, array.length);
    }

    @Override
    public void writeAt(long pos, byte[] array, int offset, int len) {
        assert SKIP_ASSERT || pos >= 0;
        assert SKIP_ASSERT || (offset >= 0 && len >= 0);

        ensureNotOutBound(pos + len);
        if (offset + len > array.length)
            throw new IllegalArgumentException("Required len out of bound array");

        final int addressLen = 1 << this.bitshiftAlign;
        while (len > 0) {
            final int addressOffset = (int) (pos & this.bitmaskAlign);
            final int copiesLoop = Math.min(len, addressLen - addressOffset);

            memory.copyMemory(translate(pos), array, offset, copiesLoop);

            len -= copiesLoop;
            pos += addressOffset;
            offset += copiesLoop;
        }
    }

    @Override
    public void writeAt(long pos, ByteBuffer buffer) {
        assert SKIP_ASSERT || pos >= 0;

        writeAt(pos, buffer, 0, buffer.remaining());
    }

    @Override
    public void writeAt(long pos, ByteBuffer buffer, int offset, int len) {
        assert SKIP_ASSERT || pos >= 0;
        assert SKIP_ASSERT || (offset >= 0 && len >= 0);

        ensureNotOutBound(pos + len);
        byte[] array = Bytex.toArray(buffer, offset, len);
        writeAt(pos, array, 0, array.length);
    }

    @Override
    public byte readAt(long pos) {
        assert SKIP_ASSERT || pos >= 0;

        ensureNotOutBound(pos);
        return memory.readByte(translate(pos));
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

        ensureNotOutBound(pos + len);

        final int addressLen = 1 << this.bitshiftAlign;
        final int mustReads = Math.min(len, (int) (size() - pos));

        if (offset + mustReads > dest.length)
            return -1;

        int i = 0;
        while (i < mustReads) {
            final int addressOffset = (int) (pos & this.bitmaskAlign);
            final int copiesLoop = Math.min(len, addressLen - addressOffset);

            memory.copyMemory(null, translate(pos),
                    dest, OSMemory.ARRAY_BYTE_BASE_OFFSET + offset, copiesLoop);

            i += copiesLoop;
            pos += addressOffset;
        }

        return mustReads;
    }

    @Override
    public int readAt(long pos, ByteBuffer dest) {
        assert SKIP_ASSERT || pos >= 0;

        return readAt(pos, dest, 0, dest.remaining());
    }

    @Override
    public int readAt(long pos, ByteBuffer dest, int offset, int len) {
        assert SKIP_ASSERT || pos >= 0;
        assert SKIP_ASSERT || (offset >= 0 && len >= 0);

        ensureNotOutBound(pos + len);
        byte[] array = Bytex.toArray(dest, offset, len);
        int result = readAt(pos, array, 0, array.length);
        dest.put(array, offset, len);
        return result;
    }

    private void ensureNotOutBound(long pos) {
        if (pos > this.size)
            throw new IllegalArgumentException("Required index out of bound; required=%d; size=%d".formatted(pos, size));
    }

    private long translate(long pos) {
        final int whichAddress = (int) (pos >>> this.bitshiftAlign);
        final int offset = (int) (pos & this.bitmaskAlign);
        final long address = this.arenaAddress[whichAddress];

        return address + offset;
    }
}
