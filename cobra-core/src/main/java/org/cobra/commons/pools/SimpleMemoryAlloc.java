package org.cobra.commons.pools;

import org.cobra.commons.threads.CobraThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleMemoryAlloc implements MemoryAlloc {


    private static final Logger log = LoggerFactory.getLogger(SimpleMemoryAlloc.class);

    private final long maxSingleAlloc;
    private final Thread gcListenerThread;
    private volatile boolean alive;
    private final ReferenceQueue<ByteBuffer> gcCollectedBuffers;
    private final Map<BufferReference, BufferMetadata> bufferInFlights;


    public SimpleMemoryAlloc(long maxSingleAlloc) {
        if (maxSingleAlloc <= 0)
            throw new IllegalArgumentException("maxSingleAlloc must be positive");

        this.maxSingleAlloc = maxSingleAlloc;
        this.alive = true;
        this.gcCollectedBuffers = new ReferenceQueue<>();
        this.bufferInFlights = new ConcurrentHashMap<>();

        this.gcListenerThread = CobraThread.daemon(new GcCollectorListener(), "mem-alloc", "gc-collector");
        this.gcListenerThread.start();
    }

    @Override
    public ByteBuffer allocate(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size must be positive");

        if (size > maxSingleAlloc)
            throw new IllegalArgumentException("size must be less than or equal to maxSingleAlloc");

        ByteBuffer buffer = ByteBuffer.allocate(size);
        allocBufferToBeReturned(buffer);
        return buffer;
    }

    @Override
    public void release(ByteBuffer buffer) {
        if (buffer == null)
            throw new IllegalArgumentException("buffer is null");

        allocBufferToBeReleased(buffer);
    }

    @Override
    public void close() {
        alive = false;
        gcListenerThread.interrupt();
    }

    private void allocBufferToBeReturned(ByteBuffer buffer) {
        BufferReference reference = new BufferReference(buffer, gcCollectedBuffers);
        BufferMetadata metadata = new BufferMetadata(buffer.capacity());

        if (bufferInFlights.put(reference, metadata) != null) {
            throw new IllegalStateException(String.format("duplication allocated buffer; hashcode = %s; size = %s",
                    reference.hashCode(), metadata.size));
        }

        log.trace("allocated buffer; hashcode = {}; size = {}", reference.hashCode(), metadata.size);
    }

    private void allocBufferToBeReleased(ByteBuffer buffer) {
        BufferReference reference = new BufferReference(buffer);
        BufferMetadata metadata = bufferInFlights.remove(reference);
        if (metadata == null) {
            throw new IllegalStateException(String.format("returned buffer never in this pool; hashcode = %s; size = " +
                    "%s", reference.hashCode(), buffer.capacity()));
        }

        log.trace("release buffer; hashcode = {}; size = {}", reference.hashCode(), metadata.size);
    }

    @Override
    public String toString() {
        return "SimpleMemoryPool(" +
                "maxSingleAlloc=" + maxSingleAlloc +
                ", gcListenerThread=" + gcListenerThread +
                ')';
    }

    private record BufferMetadata(long size) {
    }

    private static final class BufferReference extends WeakReference<ByteBuffer> {

        private final int hashcode;

        BufferReference(ByteBuffer referent) {
            this(referent, null);
        }

        BufferReference(ByteBuffer referent, ReferenceQueue<? super ByteBuffer> q) {
            super(referent, q);
            this.hashcode = System.identityHashCode(referent);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            BufferReference that = (BufferReference) obj;
            if (hashcode != that.hashcode) return false;

            ByteBuffer thisBuf = get();
            if (thisBuf == null) return false;

            ByteBuffer thatBuf = that.get();
            if (thatBuf == null) return false;

            return thisBuf == thatBuf;
        }

        @Override
        public int hashCode() {
            return hashcode;
        }
    }

    private final class GcCollectorListener implements Runnable {

        @Override
        public void run() {
            while (alive) {
                try {
                    BufferReference ref = (BufferReference) gcCollectedBuffers.remove();
                    ref.clear();

                    BufferMetadata metadata = bufferInFlights.remove(ref);
                    if (metadata == null)
                        continue;

                    log.warn("force to collect buffer unhappily; hashcode = {}, size = {}",
                            ref.hashCode(), metadata.size
                    );

                } catch (InterruptedException e) {
                    log.warn("interrupted", e);
                    // continue daemon, skip exception
                }
            }

            log.debug("gc-collector-listener killed");
        }

    }
}
