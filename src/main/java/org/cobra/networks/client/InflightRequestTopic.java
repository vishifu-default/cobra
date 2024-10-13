package org.cobra.networks.client;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class InflightRequestTopic {

    private final int maxInflightRequestPerConnection;
    private final Deque<InflightRequest> inflightRequestQueue;

    InflightRequestTopic(int maxInflightRequestPerConnection) {
        this.maxInflightRequestPerConnection = maxInflightRequestPerConnection;
        this.inflightRequestQueue = new ConcurrentLinkedDeque<>();
    }

    public void offer(InflightRequest request) {
        inflightRequestQueue.offer(request);
    }

    public InflightRequest takeNext() {
        return inflightRequestQueue.pollLast();
    }

    public boolean canSendMore() {
        return isEmpty() || (inflightRequestQueue.peekFirst().getSend().isCompleted()
                && inflightRequestQueue.size() < maxInflightRequestPerConnection);
    }

    public int size() {
        return inflightRequestQueue.size();
    }

    public boolean isEmpty() {
        return inflightRequestQueue.isEmpty();
    }

    public Iterable<InflightRequest> removeAll() {
        Deque<InflightRequest> copy = new ArrayDeque<>(inflightRequestQueue);
        inflightRequestQueue.clear();
        return copy::descendingIterator;
    }

    public boolean anyExpiredRequest(long ms) {
        for (InflightRequest inflightRequest : inflightRequestQueue) {
            if (inflightRequest.getElapsedMs(ms) > inflightRequest.getTimeoutMs())
                return true;
        }
        return false;
    }
}
