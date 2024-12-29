package org.cobra.core.memory.datalocal;

import org.cobra.core.hashing.HashingTable;
import org.cobra.core.hashing.Table;
import org.cobra.core.hashing.hashcodes.Murmur3Hash;
import org.cobra.core.memory.slab.SlabArena;

public class RecordRepository {

    private final Table lookupTable = new HashingTable();
    private final SlabArena arena;

    public RecordRepository() {
        arena = SlabArena.initialize();
    }

    public void putObject(String key, byte[] representation) {
        final int hashKey = toHashKey(key);

        // todo: mutex lock re-balance
        final long allocAddress = arena.allocate(hashKey, representation);

        lookupTable.put(hashKey, allocAddress);
    }

    public byte[] removeObject(String key) {
        final int hashKey = toHashKey(key);

        final long retAddress = lookupTable.remove(hashKey);
        if (retAddress == -1)
            return null;

        byte[] ans = arena.methods().get(retAddress);
        arena.free(retAddress);

        return ans;
    }

    public byte[] getData(String key) {
        final int hashKey = toHashKey(key);
        final long retAddress = lookupTable.get(hashKey);
        if (retAddress == -1)
            return null;

        return arena.methods().get(retAddress);
    }

    private int toHashKey(String key) {
        return Murmur3Hash.hash(key.getBytes());
    }
}
