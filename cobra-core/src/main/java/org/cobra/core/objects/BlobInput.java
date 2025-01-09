package org.cobra.core.objects;

import org.cobra.commons.Jvm;
import org.cobra.core.memory.MemoryMode;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Provides a navigation between RandomAccessFile and DataInputStream
 */
@SuppressWarnings("resource")
public class BlobInput implements Closeable {

    private static final String UNKNOWN_BLOB_INPUT_TYPE = "Unknown blob input type";

    private final MemoryMode memoryMode;
    private Object input;
    private BlobByteBuffer blobBuffer;

    public BlobInput(MemoryMode memoryMode) {
        this.memoryMode = memoryMode;
    }

    public static BlobInput randomAccessFile(File f) throws IOException {
        return randomAccessFile(f, Jvm.MAX_SINGLE_BUFFER_CAPACITY);
    }

    public static BlobInput randomAccessFile(File f, int bufferCapacity) throws IOException {
        BlobInput blobInput = new BlobInput(MemoryMode.VIRTUAL_MAPPED);
        RandomAccessFile raf = new RandomAccessFile(f, Jvm.File.READ_ONLY_MODE);
        blobInput.input = raf;

        FileChannel fc = raf.getChannel();
        blobInput.blobBuffer = BlobByteBuffer.mmap(fc, bufferCapacity);

        return blobInput;
    }

    public static BlobInput serial(byte[] arr) {
        InputStream bais = new ByteArrayInputStream(arr);
        return serial(bais);
    }

    public static BlobInput serial(InputStream is) {
        BlobInput blobInput = new BlobInput(MemoryMode.ON_HEAP);
        blobInput.input = new DataInputStream(is);

        return blobInput;
    }

    public MemoryMode memoryMode() {
        return this.memoryMode;
    }

    public int read() throws IOException {
        if (isFile())
            return asFile().read();
        if (isSerial())
            return asStream().read();

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    public int readNByte(byte[] dest, int offset, int len) throws IOException {
        if (isFile())
            return asFile().read(dest, offset, len);
        if (isSerial())
            return asStream().read(dest, offset, len);

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    public int readNBytes(byte[] dest, int len) throws IOException {
        if (isFile()) {
            asFile().read(dest);
            return dest.length;
        }
        if (isSerial()) {
            byte[] bytes = asStream().readNBytes(len);
            System.arraycopy(bytes, 0, dest, 0, bytes.length);
            return bytes.length;
        }

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    public short readShort() throws IOException {
        if (isFile())
            return asFile().readShort();
        if (isSerial())
            return asStream().readShort();

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    public int readInt() throws IOException {
        if (isFile())
            return asFile().readInt();
        if (isSerial())
            return asStream().readInt();

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    public long readLong() throws IOException {
        if (isFile())
            return asFile().readLong();
        if (isSerial())
            return asStream().readLong();

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    public String readUtf() throws IOException {
        if (isFile())
            return asFile().readUTF();
        if (isSerial())
            return asStream().readUTF();

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    public long skipNBytes(long n) throws IOException {
        if (isFile()) {
            long total = 0;
            int expected;
            int actual;
            do {
                expected = (n - total) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) (n - total);
                actual = asFile().skipBytes(expected);
                total += actual;
            } while (total < n && actual > 0);
            return total;
        }

        if (isSerial())
            return asStream().skip(n);

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    public void seek(long pos) throws IOException {
        if (isFile())
            asFile().seek(pos);
        else if (isSerial())
            throw new UnsupportedOperationException("Could not seek cursor on stream");
        else
            throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    public long getCursor() throws IOException {
        if (isFile())
            return asFile().getFilePointer();
        if (isSerial())
            throw new UnsupportedOperationException("Could not get cursor on stream");

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    @Override
    public void close() throws IOException {
        if (isFile()) {
            asFile().close();
        } else if (isSerial()) {
            asStream().close();
        } else {
            throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
        }
    }

    public Object getInput() {
        return this.input;
    }

    public BlobByteBuffer getBlobBuffer() {
        if (isFile()) return this.blobBuffer;
        if (isSerial()) throw new UnsupportedOperationException("A stream not have buffer");

        throw new IllegalStateException(UNKNOWN_BLOB_INPUT_TYPE);
    }

    private RandomAccessFile asFile() {
        return (RandomAccessFile) this.input;
    }

    private DataInputStream asStream() {
        return (DataInputStream) this.input;
    }

    private boolean isFile() {
        return this.input instanceof RandomAccessFile;
    }

    private boolean isSerial() {
        return this.input instanceof DataInputStream;
    }
}
