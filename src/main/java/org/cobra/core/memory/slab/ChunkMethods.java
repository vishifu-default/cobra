package org.cobra.core.memory.slab;

import org.cobra.commons.Jvm;
import org.cobra.core.encoding.Varint;
import org.cobra.core.memory.OSMemory;

public class ChunkMethods {

    private static final OSMemory memory = Jvm.osMemory();
    private static final Varint varint = Jvm.varint();

    public static byte[] getData(long address) {
        final long addressSkipMetaOffset = address + SlabLoc.FOOTPRINT;
        final int dataLen = varint.readVarInt(addressSkipMetaOffset);
        byte[] ans = new byte[dataLen];

        final long addressOfRawData = addressSkipMetaOffset + varint.sizeOfVarint(dataLen);
        memory.copyMemory(null, addressOfRawData, ans, OSMemory.ARRAY_BYTE_BASE_OFFSET, dataLen);

        return ans;
    }

    public static SlabLoc getLoc(long address) {
        final short locClsid = memory.readByte(address);
        final int locPageId = memory.readInt(address + SlabLoc.CLASS_FOOTPRINT);
        final int locChunkId = memory.readInt(address + SlabLoc.CLASS_FOOTPRINT + SlabLoc.PAGE_FOOTPRINT);
        return new SlabLoc(locClsid, locPageId, locChunkId);
    }

    public static void putData(long address, SlabLoc loc, byte[] arr) {
        memory.writeShort(address, loc.getClassIndex());
        address += SlabLoc.CLASS_FOOTPRINT;
        memory.writeInt(address, loc.getPageIndex());
        address += SlabLoc.PAGE_FOOTPRINT;
        memory.writeInt(address, loc.getChunkIndex());
        address += SlabLoc.CHUNK_FOOTPRINT;

        address = varint.writeVarInt(address, arr.length);
        memory.copyMemory(arr, 0, null, address, arr.length);
    }
}
