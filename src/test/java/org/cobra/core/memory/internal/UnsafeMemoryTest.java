package org.cobra.core.memory.internal;

import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

class UnsafeMemoryTest {

    UnsafeMemory unsafeMemory;

    @BeforeEach
    void setUp() {
        unsafeMemory = new UnsafeMemory();
    }

    @Test
    void write_read_byte() {
        byte bVal = (byte) 'a';
        byte[] arr = new byte[8];
        unsafeMemory.writeByte(arr, UnsafeMemory.ARRAY_BYTE_BASE_OFFSET + 2, bVal);
        Assertions.assertEquals(bVal, unsafeMemory.readByte(arr, UnsafeMemory.ARRAY_BYTE_BASE_OFFSET + 2),
                "read 'a' as " + "byte");

        ByteBuffer directBuffer = ByteBuffer.allocateDirect(8);
        long bbAddress = unsafeMemory.addressOf(directBuffer);
        unsafeMemory.writeByte(bbAddress + 2, bVal);
        Assertions.assertEquals(bVal, unsafeMemory.readByte(bbAddress + 2), "read 'a' as byte");
    }

    @Test
    void write_read_byteArray() {
        byte[] arr1 = new byte[16];
        byte[] arr2 = TestUtils.randString(10).getBytes();

        unsafeMemory.copyMemory(arr2, 0, arr1, 2, arr2.length);
        Assertions.assertArrayEquals(arr2, Arrays.copyOfRange(arr1, 2, 2 + arr2.length),
                "copy arr2 to arr1");

        ByteBuffer directBb1 = ByteBuffer.allocateDirect(16);
        long bbAddress = unsafeMemory.addressOf(directBb1);
        unsafeMemory.copyMemory(bbAddress, arr2, 0, arr2.length);
        byte[] readFromBb1 = new byte[10];
        directBb1.get(0, readFromBb1);
        Assertions.assertArrayEquals(arr2, readFromBb1, "copy arr2 to address of byte buffer");

        ByteBuffer directBb2 = ByteBuffer.allocateDirect(16);
        ByteBuffer directBb3 = ByteBuffer.allocateDirect(10);
        directBb3.put(arr2);
        unsafeMemory.copyMemory(unsafeMemory.addressOf(directBb3), unsafeMemory.addressOf(directBb2) + 2, arr2.length);
        byte[] readFromBb2 = new byte[10];
        directBb2.get(2, readFromBb2);
        Assertions.assertArrayEquals(arr2, readFromBb2, "copy arr2 to address of byte buffer");
    }

    @Test
    void write_read_int() {
        int i32 = 99;

        int[] arr = new int[16];
        ByteBuffer directBb1 = ByteBuffer.allocateDirect(16);
        long addressOfBb1 = unsafeMemory.addressOf(directBb1);

        unsafeMemory.writeInt(arr, UnsafeMemory.ARRAY_BYTE_BASE_OFFSET + 2, i32);
        unsafeMemory.writeInt(addressOfBb1 + 2, i32);

        Assertions.assertEquals(i32, directBb1.order(ByteOrder.LITTLE_ENDIAN).getInt(2),
                "copy (int)99 to buffer at position '2'");
        Assertions.assertEquals(i32, unsafeMemory.readInt(addressOfBb1 + 2));
        Assertions.assertEquals(i32, unsafeMemory.readInt(arr, UnsafeMemory.ARRAY_BYTE_BASE_OFFSET + 2));
    }

    @Test
    void write_read_long() {
        long i32 = 9999L;

        long[] arr = new long[16];
        ByteBuffer directBb1 = ByteBuffer.allocateDirect(16);
        long addressOfBb1 = unsafeMemory.addressOf(directBb1);

        unsafeMemory.writeLong(arr, UnsafeMemory.ARRAY_BYTE_BASE_OFFSET + 2, i32);
        unsafeMemory.writeLong(addressOfBb1 + 2, i32);

        Assertions.assertEquals(i32, directBb1.order(ByteOrder.LITTLE_ENDIAN).getLong(2),
                "copy (int)99 to buffer at position '2'");
        Assertions.assertEquals(i32, unsafeMemory.readLong(addressOfBb1 + 2));
        Assertions.assertEquals(i32, unsafeMemory.readLong(arr, UnsafeMemory.ARRAY_BYTE_BASE_OFFSET + 2));
    }

    @Test
    void allocateAndWrite() {
        byte bVal = (byte) 'a';
        long addr1 = unsafeMemory.allocate(16);
        unsafeMemory.writeByte(addr1, bVal);
        unsafeMemory.writeByte(addr1 + 8, bVal);

        Assertions.assertEquals(bVal, unsafeMemory.readByte(addr1));
        Assertions.assertEquals(bVal, unsafeMemory.readByte(addr1 + 8));

        Assertions.assertEquals(16, unsafeMemory.nativeMemoryUsed());

        unsafeMemory.freeMemory(addr1, 16);
        Assertions.assertEquals(0, unsafeMemory.nativeMemoryUsed());
    }
}