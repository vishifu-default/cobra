package org.cobra.commons.pools;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class BufferSupplierImpl implements BufferSupplier {

    private final Map<Integer, Deque<ByteBuffer>> bufferQueueMap = new HashMap<>();

    @Override
    public ByteBuffer get(int capacity) {
        Deque<ByteBuffer> deque = bufferQueueMap.get(capacity);
        if (deque == null || deque.isEmpty())
            return ByteBuffer.allocate(capacity);
        else
            return deque.poll();
    }

    @Override
    public void release(ByteBuffer buffer) {
        buffer.clear();
        Deque<ByteBuffer> deque = bufferQueueMap.get(buffer.capacity());
        deque.addLast(buffer);

    }

    @Override
    public void close() {
        bufferQueueMap.clear();
    }
}
