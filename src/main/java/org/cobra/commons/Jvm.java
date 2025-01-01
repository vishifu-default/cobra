package org.cobra.commons;

import org.cobra.core.encoding.Varint;
import org.cobra.core.memory.OSMemory;

import java.nio.ByteBuffer;

/**
 * Should place static members, that will give information of application in runtime config, something that will
 * never or less be changed in runtime.
 */
public class Jvm {

    /* By using this constant, we'll let OS determine socket buffer size */
    public static final int USE_DEFAULT_SOCKET_BUFFER_SIZE = -1;

    /* Indicating an infinite timestamp */
    public static final long INF_TIMESTAMP = -1L;

    public static final int MAX_SINGLE_BUFFER_CAPACITY = (1 << 30);

    /* Control the assertion define */
    public static boolean SKIP_ASSERTION = false;

    /* A singleton instance that provide var-len handles */
    public static Varint varint() {
        return Varint.INSTANCE;
    }

    /* A singleton instance that provide unsafe memory */
    public static OSMemory osMemory() {
        return OSMemory.MEMORY;
    }

    public static int osPageSize() {
        return OSMemory.MEMORY.pageSize();
    }

    public static final int DEFAULT_MEMORY_THRESHOLD = 1024 * 1024;
    public static final int DEFAULT_PAGE_CHUNK_SIZE_BYTE = 32;

    public static class File {
        public static final String READ_ONLY_MODE = "r";
    }

    public static final String SYSTEM_TEMPDIR = System.getProperty("java.io.tmpdir");

    public static ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
}
