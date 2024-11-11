package org.cobra.core.encoding;

import org.cobra.commons.errors.CobraException;
import org.cobra.core.bytes.RandomBytes;
import org.cobra.core.bytes.SequencedBytes;

@SuppressWarnings("DuplicatedCode")
public class VarLenHandles {

    private static final byte NULL_BYTE = (byte) 0x80;
    private static final byte MASKING_BYTE = (byte) 0x7f;

    public static final VarLenHandles INSTANCE = new VarLenHandles();
    public static final String ATTEMPT_TO_READ_NULL_AS_INTEGER = "Attempt to read null value as Integer";
    public static final String ATTEMPT_TO_READ_NULL_AS_LONG = "Attempt to read null as Long";

    /* Prevents construct from outside */
    private VarLenHandles() {
    }

    public int writeNull(byte[] data, int pos) {
        data[pos++] = NULL_BYTE;
        return pos;
    }

    public long writeNull(RandomBytes bytes, long pos) {
        bytes.writeAt(pos, NULL_BYTE);
        return pos + 1;
    }

    public long writeNull(SequencedBytes bytes) {
        bytes.write(NULL_BYTE);
        return bytes.position();
    }

    public boolean readNull(byte[] data, int pos) {
        return data[pos] == NULL_BYTE;
    }

    public boolean readNull(RandomBytes bytes, long pos) {
        return bytes.readAt(pos) == NULL_BYTE;
    }

    public boolean readNull(SequencedBytes bytes) {
        return bytes.read() == NULL_BYTE;
    }

    public int writeVarInt(byte[] data, int pos, int val) {
        if (val > 0xfffffff || val < 0)
            data[pos++] = (byte) (NULL_BYTE | val >>> 28);
        if (val > 0x1fffff || val < 0)
            data[pos++] = (byte) (NULL_BYTE | (val >>> 21) & MASKING_BYTE);
        if (val > 0x3fff || val < 0)
            data[pos++] = (byte) (NULL_BYTE | (val >>> 14) & MASKING_BYTE);
        if (val > 0x7f || val < 0)
            data[pos++] = (byte) (NULL_BYTE | (val >>> 7) & MASKING_BYTE);
        data[pos++] = (byte) (val & MASKING_BYTE);

        return pos;
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
        if (b == NULL_BYTE)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_INTEGER);

        int result = b & MASKING_BYTE;
        while ((b & NULL_BYTE) != 0) {
            b = data[pos++];
            result <<= 7;
            result |= (b & MASKING_BYTE);
        }

        return result;
    }

    public int readVarInt(RandomBytes bytes, long pos) {
        byte b = bytes.readAt(pos++);
        if (b == NULL_BYTE)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_INTEGER);

        int result = b & MASKING_BYTE;
        while ((b & NULL_BYTE) != 0) {
            b = bytes.readAt(pos++);
            result <<= 7;
            result |= (b & MASKING_BYTE);
        }

        return result;
    }

    public int readVarInt(SequencedBytes bytes) {
        byte b = bytes.read();
        if (b == NULL_BYTE)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_INTEGER);

        int result = b & MASKING_BYTE;
        while ((b & NULL_BYTE) != 0) {
            b = bytes.read();
            result <<= 7;
            result |= (b & MASKING_BYTE);
        }

        return result;
    }

    public int writeVarLong(byte[] data, int pos, long val) {
        if (val < 0)
            data[pos++] = (byte) 0x81;
        if (val > 0xffffffffffffffL || val < 0)
            data[pos++] = ((byte) (NULL_BYTE | ((val >>> 56) & MASKING_BYTE)));
        if (val > 0x1ffffffffffffL || val < 0)
            data[pos++] = ((byte) (NULL_BYTE | ((val >>> 49) & MASKING_BYTE)));
        if (val > 0x3ffffffffffL || val < 0)
            data[pos++] = ((byte) (NULL_BYTE | ((val >>> 42) & MASKING_BYTE)));
        if (val > 0x7ffffffffL || val < 0)
            data[pos++] = ((byte) (NULL_BYTE | ((val >>> 35) & MASKING_BYTE)));
        if (val > 0xfffffffL || val < 0)
            data[pos++] = ((byte) (NULL_BYTE | ((val >>> 28) & MASKING_BYTE)));
        if (val > 0x1fffffL || val < 0)
            data[pos++] = ((byte) (NULL_BYTE | ((val >>> 21) & MASKING_BYTE)));
        if (val > 0x3fffL || val < 0)
            data[pos++] = ((byte) (NULL_BYTE | ((val >>> 14) & MASKING_BYTE)));
        if (val > 0x7fL || val < 0)
            data[pos++] = ((byte) (NULL_BYTE | ((val >>> 7) & MASKING_BYTE)));
        data[pos] = (byte) (val & MASKING_BYTE);

        return pos;
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
        if (b == NULL_BYTE)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_LONG);

        long result = b & MASKING_BYTE;
        while ((b & NULL_BYTE) != 0) {
            b = data[pos++];
            result <<= 7;
            result |= (b & MASKING_BYTE);
        }

        return result;
    }

    public long readVarLong(RandomBytes bytes, long pos) {
        byte b = bytes.readAt(pos++);
        if (b == NULL_BYTE)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_LONG);

        long result = b & MASKING_BYTE;
        while ((b & NULL_BYTE) != 0) {
            b = bytes.readAt(pos++);
            result <<= 7;
            result |= (b & MASKING_BYTE);
        }

        return result;
    }

    public long readVarLong(SequencedBytes bytes) {
        byte b = bytes.read();
        if (b == NULL_BYTE)
            throw new CobraException(ATTEMPT_TO_READ_NULL_AS_LONG);

        long result = b & MASKING_BYTE;
        while ((b & NULL_BYTE) != 0) {
            b = bytes.read();
            result <<= 7;
            result |= (b & MASKING_BYTE);
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
