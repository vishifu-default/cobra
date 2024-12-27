package org.cobra.core.encoding;

import org.cobra.commons.Jvm;
import org.cobra.commons.errors.CobraException;
import org.cobra.core.bytes.RandomBytes;
import org.cobra.core.bytes.SequencedBytes;
import org.cobra.core.memory.OSMemory;

import java.nio.ByteBuffer;

@SuppressWarnings("DuplicatedCode")
public class Varint {

    private static final OSMemory memory = Jvm.osMemory();

    public static final Varint INSTANCE = new Varint();

    public static final String ATTEMPT_TO_READ_NULL_AS_INTEGER = "Attempt to read null value as Integer";
    public static final String ATTEMPT_TO_READ_NULL_AS_LONG = "Attempt to read null as Long";

    /* Prevents construct from outside */
    private Varint() {
    }

    public int writeNull(byte[] data, int pos) {
        data[pos++] = (byte) 0x80;
        return pos;
    }

    public void writeNull(ByteBuffer buffer) {
        buffer.put((byte) 0x80);
    }

    public long writeNull(RandomBytes bytes, long pos) {
        bytes.writeAt(pos, (byte) 0x80);
        return pos + 1;
    }

    public long writeNull(SequencedBytes bytes) {
        bytes.write((byte) 0x80);
        return bytes.position();
    }

    public boolean readNull(byte[] data, int pos) {
        return data[pos] == (byte) 0x80;
    }

    public boolean readNull(ByteBuffer buffer) {
        return buffer.get() == (byte) 0x80;
    }

    public boolean readNull(RandomBytes bytes, long pos) {
        return bytes.readAt(pos) == (byte) 0x80;
    }

    public boolean readNull(SequencedBytes bytes) {
        return bytes.read() == (byte) 0x80;
    }

    public int writeVarInt(byte[] data, int pos, int val) {
        if (val > 0xfffffff || val < 0) data[pos++] = (byte) (0x80 | val >>> 28);
        if (val > 0x1fffff || val < 0)  data[pos++] = (byte) (0x80 | (val >>> 21) & 0x7f);
        if (val > 0x3fff || val < 0)    data[pos++] = (byte) (0x80 | (val >>> 14) & 0x7f);
        if (val > 0x7f || val < 0)      data[pos++] = (byte) (0x80 | (val >>> 7) & 0x7f);

        data[pos++] = (byte) (val & 0x7f);

        return pos;
    }

    public void writeVarInt(ByteBuffer buffer, int val) {
        if (val > 0xfffffff || val < 0) buffer.put((byte) (val >>> 28));
        if (val > 0x1fffff || val < 0) buffer.put((byte) (0x80 | (val >>> 21) & 0x7f));
        if (val > 0x3fff || val < 0) buffer.put((byte) (0x80 | (val >>> 14) & 0x7f));
        if (val > 0x7f || val < 0) buffer.put((byte) (0x80 | (val >>> 7) & 0x7f));

        buffer.put((byte) (val & 0x7f));
    }

    public long writeVarInt(long address, int val) {
        if (val > 0xfffffff || val < 0) memory.writeByte(address++, (byte) (val >>> 28));
        if (val > 0x1fffff || val < 0) memory.writeByte(address++, (byte) (0x80 | (val >>> 21) & 0x7f));
        if (val > 0x3fff || val < 0) memory.writeByte(address++, (byte) (0x80 | (val >>> 14) & 0x7f));
        if (val > 0x7f || val < 0) memory.writeByte(address++, (byte) (0x80 | (val >>> 7) & 0x7f));

        memory.writeByte(address++, (byte) (val & 0x7f));
        return address;
    }

    public long writeVarInt(RandomBytes bytes, long pos, int val) {
        int sizeOfVarint = sizeOfVarint(val);
        byte[] varint = new byte[sizeOfVarint];
        writeVarInt(varint, 0, val);

        bytes.writeAt(pos, varint);

        return pos + sizeOfVarint;
    }

    public long writeVarInt(SequencedBytes bytes, int val) {
        long oldPosition = bytes.position();
        int sizeOfVarint = sizeOfVarint(val);
        byte[] varint = new byte[sizeOfVarint];
        writeVarInt(varint, 0, val);

        bytes.write(varint);

        return oldPosition + sizeOfVarint;
    }

    public int readVarInt(byte[] data, int pos) {
        byte b = data[pos++];
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_INTEGER);

        int result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = data[pos++];
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public int readVarInt(ByteBuffer buffer) {
        byte b = buffer.get();
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_INTEGER);

        int result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = buffer.get();
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public int readVarInt(long address) {
        byte b = memory.readByte(address++);
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_INTEGER);

        int result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = memory.readByte(address++);
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public int readVarInt(RandomBytes bytes, long pos) {
        byte b = bytes.readAt(pos++);
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_INTEGER);

        int result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = bytes.readAt(pos++);
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public int readVarInt(SequencedBytes bytes) {
        byte b = bytes.read();
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_INTEGER);

        int result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = bytes.read();
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public int writeVarLong(byte[] data, int pos, long val) {
        if (val < 0) data[pos++] = (byte) 0x81;
        if (val > 0xffffffffffffffL || val < 0) data[pos++] = ((byte) (0x80 | ((val >>> 56) & 0x7fL)));
        if (val > 0x1ffffffffffffL || val < 0)  data[pos++] = ((byte) (0x80 | ((val >>> 49) & 0x7fL)));
        if (val > 0x3ffffffffffL || val < 0)    data[pos++] = ((byte) (0x80 | ((val >>> 42) & 0x7fL)));
        if (val > 0x7ffffffffL || val < 0)      data[pos++] = ((byte) (0x80 | ((val >>> 35) & 0x7fL)));
        if (val > 0xfffffffL || val < 0)        data[pos++] = ((byte) (0x80 | ((val >>> 28) & 0x7fL)));
        if (val > 0x1fffffL || val < 0)         data[pos++] = ((byte) (0x80 | ((val >>> 21) & 0x7fL)));
        if (val > 0x3fffL || val < 0)           data[pos++] = ((byte) (0x80 | ((val >>> 14) & 0x7fL)));
        if (val > 0x7fL || val < 0)             data[pos++] = ((byte) (0x80 | ((val >>> 7) & 0x7fL)));

        data[pos] = (byte) (val & 0x7FL);

        return pos;
    }

    public void writeVarLong(ByteBuffer buffer, long val) {
        if (val < 0) buffer.put((byte) 0x81);
        if (val > 0xffffffffffffffL || val < 0) buffer.put((byte) (0x80 | ((val >>> 56) & 0x7fL)));
        if (val > 0x1ffffffffffffL || val < 0)  buffer.put((byte) (0x80 | ((val >>> 49) & 0x7fL)));
        if (val > 0x3ffffffffffL || val < 0)    buffer.put((byte) (0x80 | ((val >>> 42) & 0x7fL)));
        if (val > 0x7ffffffffL || val < 0)      buffer.put((byte) (0x80 | ((val >>> 35) & 0x7fL)));
        if (val > 0xfffffffL || val < 0)        buffer.put((byte) (0x80 | ((val >>> 28) & 0x7fL)));
        if (val > 0x1fffffL || val < 0)         buffer.put((byte) (0x80 | ((val >>> 21) & 0x7fL)));
        if (val > 0x3fffL || val < 0)           buffer.put((byte) (0x80 | ((val >>> 14) & 0x7fL)));
        if (val > 0x7fL || val < 0)             buffer.put((byte) (0x80 | ((val >>> 7) & 0x7fL)));

        buffer.put((byte) (val & 0x7fL));
    }

    public long writeVarLong(long address, long val) {
        if (val < 0) memory.writeByte(address++, (byte) 0x81);
        if (val > 0xffffffffffffffL || val < 0) memory.writeByte(address++, (byte) (0x80 | ((val >>> 56) & 0x7fL)));
        if (val > 0x1ffffffffffffL || val < 0)  memory.writeByte(address++, (byte) (0x80 | ((val >>> 49) & 0x7fL)));
        if (val > 0x3ffffffffffL || val < 0)    memory.writeByte(address++, (byte) (0x80 | ((val >>> 42) & 0x7fL)));
        if (val > 0x7ffffffffL || val < 0)      memory.writeByte(address++, (byte) (0x80 | ((val >>> 35) & 0x7fL)));
        if (val > 0xfffffffL || val < 0)        memory.writeByte(address++, (byte) (0x80 | ((val >>> 28) & 0x7fL)));
        if (val > 0x1fffffL || val < 0)         memory.writeByte(address++, (byte) (0x80 | ((val >>> 21) & 0x7fL)));
        if (val > 0x3fffL || val < 0)           memory.writeByte(address++, (byte) (0x80 | ((val >>> 14) & 0x7fL)));
        if (val > 0x7fL || val < 0)             memory.writeByte(address++, (byte) (0x80 | ((val >>> 7) & 0x7fL)));

        memory.writeByte(address++, (byte) (val & 0x7fL));
        return address;
    }

    public long writeVarLong(RandomBytes bytes, long pos, long val) {
        int sizeof = sizeOfVarint(val);
        byte[] varint = new byte[sizeof];
        writeVarLong(varint, 0, val);

        bytes.writeAt(pos, varint);

        return pos + sizeof;
    }

    public long writeVarLong(SequencedBytes bytes, long val) {
        long oldPosition = bytes.position();
        int sizeof = sizeOfVarint(val);
        byte[] varint = new byte[sizeof];
        writeVarLong(varint, 0, val);
        bytes.write(varint);

        return oldPosition + sizeof;
    }

    public long readVarLong(byte[] data, int pos) {
        byte b = data[pos++];
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_LONG);

        long result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = data[pos++];
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public long readVarLong(ByteBuffer buffer) {
        byte b = buffer.get();
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_LONG);

        long result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = buffer.get();
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public long readVarLong(long address) {
        byte b = memory.readByte(address++);
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_LONG);

        long result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = memory.readByte(address++);
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public long readVarLong(RandomBytes bytes, long pos) {
        byte b = bytes.readAt(pos++);
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_LONG);

        long result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = bytes.readAt(pos++);
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public long readVarLong(SequencedBytes bytes) {
        byte b = bytes.read();
        if (b == (byte) 0x80)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_LONG);

        long result = b & 0x7f;
        while ((b & 0x80) != 0) {
            b = bytes.read();
            result <<= 7;
            result |= (b & 0x7f);
        }

        return result;
    }

    public int sizeOfVarint(int i32) {
        if (i32 < 0)
            return 5;
        if (i32 < 0x80)
            return 1;
        if (i32 < 0x4000)
            return 2;
        if (i32 < 0x200000)
            return 3;
        if (i32 < 0x10000000)
            return 4;
        return 5;
    }

    public int sizeOfVarint(long i64) {
        if (i64 < 0)
            return 10;
        if (i64 < 0x80L)
            return 1;
        if (i64 < 0x4000L)
            return 2;
        if (i64 < 0x200000L)
            return 3;
        if (i64 < 0x10000000L)
            return 4;
        if (i64 < 0x800000000L)
            return 5;
        if (i64 < 0x40000000000L)
            return 6;
        if (i64 < 0x2000000000000L)
            return 7;
        if (i64 < 0x100000000000000L)
            return 8;
        return 9;
    }
}
