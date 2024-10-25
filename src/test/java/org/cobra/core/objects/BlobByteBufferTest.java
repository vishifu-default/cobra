package org.cobra.core.objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlobByteBufferTest {

    private static final int size = 1_024;

    private BlobByteBuffer blobBuffer;

    @BeforeEach
    void setUp() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.put(1, (byte) 1);
        byteBuffer.put(2, (byte) 2);
        byteBuffer.putLong(3, 100_000L);
        blobBuffer = new BlobByteBuffer(new ByteBuffer[]{byteBuffer}, 1024, 10, 1023);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void mmap() throws IOException {
        Path tmpFilePath = Files.createTempFile("test-file", ".tmp");
        File tmpfile = tmpFilePath.toFile();
        tmpfile.deleteOnExit();

        RandomAccessFile raf = new RandomAccessFile(tmpfile, "rw");
        FileChannel fc = raf.getChannel();

        raf.write((byte) 1);
        raf.write((byte) 2);
        raf.writeLong(100_000L);

        BlobByteBuffer blobByteBuffer = BlobByteBuffer.mmap(fc, 1024);
        assertEquals((byte) 1, blobByteBuffer.getByte(0));
        assertEquals((byte) 2, blobByteBuffer.getByte(1));
        assertEquals(100_000L, blobByteBuffer.getLong(2));

        fc.close();
        raf.close();
    }

    @Test
    void duplicate() {
        BlobByteBuffer duplicate = blobBuffer.duplicate();
        for (int i = 0; i < size; i++) {
            assertEquals(duplicate.getByte(i), blobBuffer.getByte(i));
        }
    }

    @Test
    void position() {
        blobBuffer.position(12);
        assertEquals(12, blobBuffer.position());
    }

    @Test
    void position_outBound() {
        assertThrows(IllegalArgumentException.class, () -> blobBuffer.position(2038));
        assertThrows(IllegalArgumentException.class, () -> blobBuffer.position(-1));
    }

    @Test
    void getByte() {
        assertEquals((byte) 1, blobBuffer.getByte(1));
        assertEquals((byte) 2, blobBuffer.getByte(2));
    }

    @Test
    void getLong() {
        assertEquals(100_000L, blobBuffer.getLong(3));
    }
}