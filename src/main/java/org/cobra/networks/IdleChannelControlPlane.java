package org.cobra.networks;

import org.cobra.commons.Clock;

import java.util.LinkedHashMap;
import java.util.Map;

public class IdleChannelControlPlane {

    private final Map<String, Long> lruNodeMap;
    private final long connIdleNanos;
    private long nextCheckAtNanos;

    public IdleChannelControlPlane(long acceptedIdleNanos, Clock clock) {
        this.lruNodeMap = new LinkedHashMap<>(20, .75f, true);
        this.connIdleNanos = acceptedIdleNanos;
        this.nextCheckAtNanos = clock.nanoseconds() + acceptedIdleNanos;
    }

    public void update(String id, long atNanos) {
        lruNodeMap.put(id, atNanos);
    }

    public Map.Entry<String, Long> pollLruExpiredNode(long atNanos) {
        if (atNanos < nextCheckAtNanos)
            return null;

        if (lruNodeMap.isEmpty())
            return null;

        final Map.Entry<String, Long> oldestEntry = lruNodeMap.entrySet().iterator().next();
        final Long lastActive = oldestEntry.getValue();
        nextCheckAtNanos = lastActive + connIdleNanos;

        if (atNanos > nextCheckAtNanos)
            return oldestEntry;
        else
            return null;
    }

    public void remove(String id) {
        lruNodeMap.remove(id);
    }
}
