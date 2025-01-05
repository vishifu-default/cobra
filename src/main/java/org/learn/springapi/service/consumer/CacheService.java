package org.learn.springapi.service.consumer;

import org.cobra.RecordApi;
import org.cobra.api.CobraRecordApi;
import org.cobra.consumer.CobraConsumer;
import org.cobra.consumer.fs.FilesystemBlobRetriever;
import org.cobra.consumer.fs.RemoteFilesystemBlobRetriever;
import org.cobra.core.memory.MemoryMode;
import org.cobra.networks.CobraClient;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheService {
    private static final String host = "host.docker.internal";
    private static final int port = 7070;
    private static final String consumePath = "./misc/consume-dir";

    private CobraConsumer consumer;
    private RecordApi api;

    public CacheService() {
        Path cacheDir = Paths.get(consumePath);
        CobraClient client = new CobraClient(new InetSocketAddress(host, port));
        CobraConsumer.BlobRetriever blobRetriever = new FilesystemBlobRetriever(cacheDir,
                new RemoteFilesystemBlobRetriever(client, cacheDir));

        consumer = CobraConsumer.fromBuilder()
                .withNetworkClient(client)
                .withBlobRetriever(blobRetriever)
                .withMemoryMode(MemoryMode.ON_HEAP)
                .build();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            consumer.triggerRefreshWithDelay(100);
        };

        api = new CobraRecordApi(consumer);

        executorService.scheduleAtFixedRate(task, 100, 500, TimeUnit.MICROSECONDS);
    }

    public CobraConsumer consumer() {
        return consumer;
    }

    public RecordApi api() {
        return api;
    }
}
