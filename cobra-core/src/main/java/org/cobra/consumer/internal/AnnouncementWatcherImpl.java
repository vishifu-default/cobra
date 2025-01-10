package org.cobra.consumer.internal;

import org.cobra.commons.CobraConstants;
import org.cobra.commons.errors.CobraException;
import org.cobra.consumer.CobraConsumer;
import org.cobra.networks.CobraClient;

import java.io.IOException;

public class AnnouncementWatcherImpl implements CobraConsumer.AnnouncementWatcher {

    private final CobraClient client;
    private volatile long latestVersion = CobraConstants.VERSION_NULL;

    public AnnouncementWatcherImpl(CobraClient client) {
        this.client = client;
    }

    @Override
    public void setLatestVersion(long latestVersion) {
        this.latestVersion = latestVersion;
    }

    @Override
    public long getLatestVersion() {
        try {
            final long version = client.fetchVersion();
            setLatestVersion(version);
        } catch (IOException e) {
            throw new CobraException(e);
        }
        return latestVersion;
    }

}
