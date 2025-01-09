package org.cobra.core.memory.slab;

import org.cobra.commons.Jvm;
import org.cobra.core.encoding.Varint;
import org.cobra.core.memory.OSMemory;

import java.nio.ByteBuffer;

public class SlabMethods {

    private static final OSMemory memory = Jvm.osMemory();
    private static final Varint varint = Jvm.varint();

    static final int SLAB_CLASS_FOOTPRINT = Byte.BYTES;
    static final int SLAB_OFFSET_FOOTPRINT = Integer.BYTES;
    static final int SLAB_HASH_KEY_FOOTPRINT = Integer.BYTES;
    static final int SLAB_META_FOOTPRINT = SLAB_CLASS_FOOTPRINT + SLAB_OFFSET_FOOTPRINT + SLAB_HASH_KEY_FOOTPRINT;

    private final SlabArena arena;

    public SlabMethods(SlabArena arena) {
        this.arena = arena;
    }

    public byte[] get(SlabOffset offset) {
        SlabClass slab = arena.slab(offset.getClsid());
        SlabClass.SlabPage page = slab.page(offset.getPageId());
        long address = page.getBaseAddress() + ((long) slab.getChunkSize() * offset.getChunkId()) + SLAB_META_FOOTPRINT;

        int varintSize = varint.readVarInt(address);
        address += varint.sizeOfVarint(varintSize);
        byte[] ans = new byte[varintSize];

        memory.copyMemory(null, address, ans, OSMemory.ARRAY_BYTE_BASE_OFFSET, varintSize);

        return ans;
    }

    public byte[] get(long address) {
        long skipMetaAddress = address + SLAB_META_FOOTPRINT;

        int varintSize = varint.readVarInt(skipMetaAddress);
        skipMetaAddress += varint.sizeOfVarint(varintSize);
        byte[] ans = new byte[varintSize];

        memory.copyMemory(null, skipMetaAddress, ans, OSMemory.ARRAY_BYTE_BASE_OFFSET, varintSize);

        return ans;
    }

    public SlabOffset location(long address) {
        final byte[] meta = new byte[SLAB_META_FOOTPRINT];
        memory.copyMemory(null, address, meta, 0, SLAB_META_FOOTPRINT);
        final ByteBuffer buffer = ByteBuffer.wrap(meta);

        final int clsid = buffer.get();
        final int pageOffset = buffer.getInt();
        final int pageId = pageOffset / arena.slab(clsid).getChunksPerPage();
        final int chunkId = pageOffset & arena.slab(clsid).getChunkMasking();

        return new SlabOffset(clsid, pageId, chunkId);
    }

    public void put(SlabOffset offset, int hash, byte[] arr) {
        SlabClass slab = arena.slab(offset.getClsid());
        SlabClass.SlabPage page = slab.page(offset.getPageId());

        long address = page.getBaseAddress() + ((long) slab.getChunkSize() * offset.getChunkId());

        int varintSize = varint.sizeOfVarint(arr.length);
        // todo: poll
        int length = varintSize + arr.length + SLAB_META_FOOTPRINT;
        ByteBuffer buffer = ByteBuffer.allocate(length);

        buffer.rewind();
        // put clsid
        buffer.put((byte) offset.getClsid());

        // put page-offset
        int pageOffset = (offset.getPageId() * slab.getChunksPerPage()) + offset.getChunkId();
        buffer.putInt(pageOffset);

        // put hash
        buffer.putInt(hash);

        // put var_len
        varint.writeVarInt(buffer, arr.length);

        // put data
        buffer.put(arr);

        memory.copyMemory(address, buffer.array(), 0, length);
    }

    public long addressOf(SlabOffset offset) {
        final SlabClass slab = arena.slab(offset.getClsid());
        return slab.page(offset.getPageId()).getBaseAddress() + ((long) offset.getChunkId() * slab.getChunkSize());
    }
}
