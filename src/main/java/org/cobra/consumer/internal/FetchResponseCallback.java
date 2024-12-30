package org.cobra.consumer.internal;

import org.cobra.networks.protocol.MessageAccessor;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.FetchVersionResponse;
import org.cobra.networks.requests.RequestCompletionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchResponseCallback implements RequestCompletionCallback {

    private static final Logger log = LoggerFactory.getLogger(FetchResponseCallback.class);

    private final AnnouncementWatcherImpl watcher;

    public FetchResponseCallback(AnnouncementWatcherImpl watcher) {
        this.watcher = watcher;
    }

    @Override
    public void consume(AbstractResponse response) {
        MessageAccessor accessor = new MessageAccessor(response.data().size());
        response.data().write(accessor);

        accessor.flip();

        FetchVersionResponse fetchResponse = FetchVersionResponse.doParse(accessor.buffer());
        long version = fetchResponse.data().getVersion();
        log.info("fetch version: {}", version);

        watcher.setLatestVersion(version);
    }
}
