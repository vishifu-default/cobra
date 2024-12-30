package org.cobra.consumer.internal;

import org.cobra.commons.Clock;
import org.cobra.commons.CobraConstants;
import org.cobra.consumer.CobraConsumer;
import org.cobra.networks.client.Client;
import org.cobra.networks.client.ClientRequest;
import org.cobra.networks.requests.FetchVersionRequest;

public class AnnouncementWatcherImpl implements CobraConsumer.AnnouncementWatcher {

    private final Client networkClient;
    private final Clock clock;

    private long latestVersion = CobraConstants.VERSION_NULL;

    public AnnouncementWatcherImpl(Client networkClient, Clock clock) {
        this.networkClient = networkClient;
        this.clock = clock;
    }

    public void setLatestVersion(long latestVersion) {
        this.latestVersion = latestVersion;
    }

    @Override
    public long getLatestVersion() {
        ClientRequest clientRequest = networkClient.createClientRequest(new FetchVersionRequest.Builder(),
                clock.milliseconds(),
                new FetchResponseCallback(this));

        networkClient.send(clientRequest, clock.milliseconds());
        return latestVersion;
    }

    @Override
    public void subscribeToUpdates(CobraConsumer consumer) {

    }
}
