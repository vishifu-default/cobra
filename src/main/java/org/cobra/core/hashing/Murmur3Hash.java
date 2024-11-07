package org.cobra.core.hashing;

public final class Murmur3Hash {
    private static final int MURMUR3_SEED = 0xcceaccf9;
    private static final int c1 = 0xcc9e2d51;
    private static final int c2 = 0x1b873593;

    public static int hash(byte[] data) {
        int chunkSize = data.length & 0xfffffffc;

        int hash = MURMUR3_SEED;

        // loop 4-bits block
        for (int i = 0; i < chunkSize; i += 4) {
            int k = (data[i] & 0xff)
                    | ((data[i + 1] & 0xff) << 8)
                    | ((data[i + 2] & 0xff) << 16)
                    | ((data[i + 3] & 0xff) << 24);

            k = scrambleInt32(k);
            hash ^= k;
            hash = (hash << 13) | (k >> 19); // ROL(13)
            hash = (hash * 5) + 0xe6546b64;
        }

        int k1 = 0;
        switch (data.length & 0x03) {
            case 3:
                k1 = (data[chunkSize + 2] & 0xff) << 16;
                // fallthrough
            case 2:
                k1 |= (data[chunkSize + 1] & 0xff) << 8;
                // fallthrough
            case 1:
                k1 |= (data[chunkSize] & 0xff);
                k1 = scrambleInt32(k1);
                hash ^= k1;
        }

        hash ^= data.length;
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);

        return hash;
    }

    public static int fmix(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    private static int scrambleInt32(int k) {
        k *= c1;
        k = (k << 15) | (k >> 17); // ROL(15)
        k *= c2;

        return k;
    }
}
