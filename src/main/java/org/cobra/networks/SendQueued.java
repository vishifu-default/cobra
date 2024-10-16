package org.cobra.networks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.GatheringByteChannel;
import java.util.Queue;

public class SendQueued implements Send {


    private static final Logger log = LoggerFactory.getLogger(SendQueued.class);

    private final Queue<Send> sendQueue;
    private final long size;

    private long totalWritten = 0L;
    private Send current;

    public SendQueued(Queue<Send> queue) {
        this.sendQueue = queue;
        long size = 0;
        for (Send send : sendQueue)
            size += send.size();

        this.size = size;
        this.current = queue.poll();
    }

    @Override
    public boolean isCompleted() {
        return current == null;
    }

    @Override
    public long writeTo(GatheringByteChannel channel) throws IOException {
        if (isCompleted())
            throw new IllegalStateException("SendQueued is closed");

        long totalWrittenPerCall = 0;
        boolean completed;

        do {
            long written = current.writeTo(channel);
            totalWrittenPerCall += written;
            completed = current.isCompleted();
            if (completed)
                current = sendQueue.poll();
        } while (!isCompleted() && completed);

        totalWritten += totalWrittenPerCall;

        if (isCompleted() && totalWritten != size)
            log.error("Mismatch in sending bytes over socket; expected: {}; actual: {}", size, totalWritten);

        log.trace("Bytes written as part of queued-send call: {}; written so far: {}; expected: {}",
                totalWritten, totalWrittenPerCall, size);

        return totalWrittenPerCall;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void close() throws IOException {
        // nop
    }

    @Override
    public String toString() {
        return "SendQueued(" +
                "current=" + current +
                ", sendQueue=" + sendQueue +
                ", size=" + size +
                ", totalWritten=" + totalWritten +
                ')';
    }
}
