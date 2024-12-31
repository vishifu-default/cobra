package org.cobra.consumer;

import org.cobra.commons.Clock;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.threads.CobraThreadExecutor;
import org.cobra.consumer.internal.AnnouncementWatcherImpl;
import org.cobra.consumer.internal.ConsumerDataPlane;
import org.cobra.consumer.internal.DataFetcher;
import org.cobra.consumer.read.ConsumerStateContext;
import org.cobra.consumer.read.StateReadEngine;
import org.cobra.core.memory.MemoryMode;
import org.cobra.networks.CobraClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractConsumer implements CobraConsumer {

    private static final Logger log = LoggerFactory.getLogger(AbstractConsumer.class);

    protected final ReadWriteLock fetchLock = new ReentrantReadWriteLock();
    protected final CobraThreadExecutor refreshExecutor;
    protected final AnnouncementWatcher announcementWatcher;
    protected final Clock clock;

    protected final ConsumerDataPlane consumerPlane;

    protected final CobraClient client;

    protected AbstractConsumer(Builder builder) {
        this(
                builder.blobRetriever,
                builder.memoryMode,
                builder.bytesPool,
                builder.refreshExecutor,
                builder.clock,
                builder.client);
    }

    private AbstractConsumer(
            BlobRetriever blobRetriever,
            MemoryMode memoryMode,
            BytesPool bytesPool,
            CobraThreadExecutor refreshExecutor,
            Clock clock,
            CobraClient client) {
        ConsumerStateContext consumerStateContext = new ConsumerStateContext();
        this.consumerPlane = new ConsumerDataPlane(new DataFetcher(blobRetriever),
                memoryMode, new StateReadEngine(consumerStateContext, bytesPool));

        this.refreshExecutor = refreshExecutor;

        this.clock = clock;

        this.announcementWatcher = new AnnouncementWatcherImpl(client);

        this.client = client;
        this.client.bootstrap();
    }

    public void triggerRefresh() {
        fetchLock.writeLock().lock();
        try {
            if (announcementWatcher == null)
                throw new IllegalStateException("announcementWatcher is null");

            final VersionInformation latestVersion = announcementWatcher.getLatestVersionInformation();
            triggerRefreshTo(latestVersion);

        } finally {
            fetchLock.writeLock().unlock();
        }
    }

    public void triggerRefreshAsync() {
        triggerRefreshWithDelay(0);
    }

    public void triggerRefreshWithDelay(int ms) {
        final long targetBeginTime = System.currentTimeMillis() + ms;

        refreshExecutor.execute(() -> {
            try {
                long delayMs = targetBeginTime - System.currentTimeMillis();
                if (delayMs > 0)
                    Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                log.info("async refresh-trigger cancelled", e);
                return;
            }

            try {
                triggerRefresh();
            } catch (Exception e) {
                log.error("error while async refresh-trigger", e);
                throw e;
            }
        });

    }

    public void triggerRefreshTo(long version) {
        triggerRefreshTo(new VersionInformation(version));
    }

    public void triggerRefreshTo(VersionInformation versionInformation) {
        try {
            consumerPlane.update(versionInformation);
        } catch (Throwable cause) {
            throw new CobraException(cause);
        }
    }
}
