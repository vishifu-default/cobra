package org.cobra.consumer;

import org.cobra.commons.Clock;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.threads.CobraThread;
import org.cobra.consumer.internal.AnnouncementWatcherImpl;
import org.cobra.consumer.internal.BlobRetrieverFacade;
import org.cobra.consumer.internal.ConsumerDataPlane;
import org.cobra.consumer.internal.FallbackRemoteBlobRetriever;
import org.cobra.consumer.internal.TransitionUpdater;
import org.cobra.consumer.read.ConsumerStateContext;
import org.cobra.consumer.read.StateReadEngine;
import org.cobra.core.memory.MemoryMode;
import org.cobra.networks.CobraClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractConsumer implements CobraConsumer {

    private static final Logger log = LoggerFactory.getLogger(AbstractConsumer.class);

    protected final ReentrantLock stateLock = new ReentrantLock();
    protected final ExecutorService executor;
    protected final AnnouncementWatcher announcementWatcher;
    protected final Clock clock;

    protected final ConsumerDataPlane consumerPlane;
    protected final ConsumerStateContext consumerStateContext;

    protected final CobraClient client;

    protected AbstractConsumer(Builder builder) {
        this(
                builder.blobRetriever,
                builder.memoryMode,
                builder.bytesPool,
                builder.executor,
                builder.clock,
                builder.producerAddress);
    }

    private AbstractConsumer(
            BlobRetriever blobRetriever,
            MemoryMode memoryMode,
            BytesPool bytesPool,
            ExecutorService executor,
            Clock clock,
            InetSocketAddress producerAddress) {
        consumerStateContext = new ConsumerStateContext();
        this.client = new CobraClient(producerAddress);

        final FallbackRemoteBlobRetriever fallbackRemoteBlobRetriever = new FallbackRemoteBlobRetriever(client, blobRetriever);
        final BlobRetrieverFacade blobRetrieverFacade = new BlobRetrieverFacade(blobRetriever, fallbackRemoteBlobRetriever);
        this.consumerPlane = new ConsumerDataPlane(new TransitionUpdater(blobRetrieverFacade),
                memoryMode, new StateReadEngine(consumerStateContext, bytesPool));

        this.executor = executor;
        this.clock = clock;
        this.announcementWatcher = new AnnouncementWatcherImpl(client);

        executor.execute(client::bootstrap);
    }

    @Override
    public void poll() {
        poll(5_000);
    }

    @Override
    public void poll(int timeoutMs) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(t ->
                CobraThread.daemon(t, "consumer", "polling"));

        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!stateLock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                    log.warn("timeout for poll operation");
                    return;
                }

                if (!client.isReady()) {
                    client.tryConnect();
                    return;
                }

                if (announcementWatcher == null)
                    throw new CobraException("announcement-watcher is null, must have implementation of AnnouncementWatcher");

                final VersionInformation latestVersion = announcementWatcher.getLatestVersionInformation();
                triggerRefreshTo(latestVersion);

            } catch (Throwable cause) {
                log.warn(cause.getMessage(), cause);
                client.shutdown();
            } finally {
                stateLock.unlock();
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public ConsumerStateContext context() {
        return consumerStateContext;
    }

    @Override
    public long currentVersion() {
        return consumerPlane.currentVersion();
    }

    public void triggerRefreshTo(VersionInformation versionInformation) {
        try {
            consumerPlane.update(versionInformation);
        } catch (Throwable cause) {
            log.error(cause.getMessage(), cause);
            throw new CobraException(cause);
        }
    }
}
