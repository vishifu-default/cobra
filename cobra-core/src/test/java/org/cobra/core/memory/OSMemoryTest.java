package org.cobra.core.memory;

import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

class OSMemoryTest {

    OSMemory memory;

    @BeforeEach
    void setUp() {
        memory = new OSMemory();
    }

    @Test
    void write_read_byte() {
        byte bVal = (byte) 'a';
        byte[] arr = new byte[8];
        memory.writeByte(arr, OSMemory.ARRAY_BYTE_BASE_OFFSET + 2, bVal);
        Assertions.assertEquals(bVal, memory.readByte(arr, OSMemory.ARRAY_BYTE_BASE_OFFSET + 2),
                "read 'a' as " + "byte");

        ByteBuffer directBuffer = ByteBuffer.allocateDirect(8);
        long bbAddress = memory.addressOf(directBuffer);
        memory.writeByte(bbAddress + 2, bVal);
        Assertions.assertEquals(bVal, memory.readByte(bbAddress + 2), "read 'a' as byte");
    }

    @Test
    void write_read_byteArray() {
        byte[] arr1 = new byte[16];
        byte[] arr2 = TestUtils.randString(10).getBytes();

        memory.copyMemory(arr2, 0, arr1, 2, arr2.length);
        Assertions.assertArrayEquals(arr2, Arrays.copyOfRange(arr1, 2, 2 + arr2.length),
                "copy arr2 to arr1");

        ByteBuffer directBb1 = ByteBuffer.allocateDirect(16);
        long bbAddress = memory.addressOf(directBb1);
        memory.copyMemory(bbAddress, arr2, 0, arr2.length);
        byte[] readFromBb1 = new byte[10];
        directBb1.get(0, readFromBb1);
        Assertions.assertArrayEquals(arr2, readFromBb1, "copy arr2 to address of byte buffer");

        ByteBuffer directBb2 = ByteBuffer.allocateDirect(16);
        ByteBuffer directBb3 = ByteBuffer.allocateDirect(10);
        directBb3.put(arr2);
        memory.copyMemory(memory.addressOf(directBb3), memory.addressOf(directBb2) + 2, arr2.length);
        byte[] readFromBb2 = new byte[10];
        directBb2.get(2, readFromBb2);
        Assertions.assertArrayEquals(arr2, readFromBb2, "copy arr2 to address of byte buffer");
    }

    @Test
    void write_read_int() {
        int i32 = 99;

        int[] arr = new int[16];
        ByteBuffer directBb1 = ByteBuffer.allocateDirect(16);
        long addressOfBb1 = memory.addressOf(directBb1);

        memory.writeInt(arr, OSMemory.ARRAY_BYTE_BASE_OFFSET + 2, i32);
        memory.writeInt(addressOfBb1 + 2, i32);

        Assertions.assertEquals(i32, directBb1.order(ByteOrder.LITTLE_ENDIAN).getInt(2),
                "copy (int)99 to buffer at position '2'");
        Assertions.assertEquals(i32, memory.readInt(addressOfBb1 + 2));
        Assertions.assertEquals(i32, memory.readInt(arr, OSMemory.ARRAY_BYTE_BASE_OFFSET + 2));
    }

    @Test
    void write_read_long() {
        long i32 = 9999L;

        long[] arr = new long[16];
        ByteBuffer directBb1 = ByteBuffer.allocateDirect(16);
        long addressOfBb1 = memory.addressOf(directBb1);

        memory.writeLong(arr, OSMemory.ARRAY_BYTE_BASE_OFFSET + 2, i32);
        memory.writeLong(addressOfBb1 + 2, i32);

        Assertions.assertEquals(i32, directBb1.order(ByteOrder.LITTLE_ENDIAN).getLong(2),
                "copy (int)99 to buffer at position '2'");
        Assertions.assertEquals(i32, memory.readLong(addressOfBb1 + 2));
        Assertions.assertEquals(i32, memory.readLong(arr, OSMemory.ARRAY_BYTE_BASE_OFFSET + 2));
    }

    @Test
    void allocateAndWrite() {
        byte bVal = (byte) 'a';
        long addr1 = memory.allocate(16);
        memory.writeByte(addr1, bVal);
        memory.writeByte(addr1 + 8, bVal);

        Assertions.assertEquals(bVal, memory.readByte(addr1));
        Assertions.assertEquals(bVal, memory.readByte(addr1 + 8));

        Assertions.assertEquals(16, memory.nativeMemoryUsed());

        memory.freeMemory(addr1, 16);
        Assertions.assertEquals(0, memory.nativeMemoryUsed());
    }
}