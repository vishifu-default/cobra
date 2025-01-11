package org.cobra.sample.consumer.service;

import org.cobra.RecordApi;
import org.cobra.api.CobraRecordApi;
import org.cobra.consumer.CobraConsumer;
import org.cobra.consumer.fs.FilesystemBlobRetriever;
import org.cobra.networks.CobraClient;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CacheService {
    private static final String host = "localhost";
    private static final int port = 7070;
    private static final String consumePath = "./misc/consume-dir";

    private final CobraConsumer consumer;
    private final RecordApi api;

    public CacheService() {
        Path cacheDir = Paths.get(consumePath);
        CobraClient client = new CobraClient(new InetSocketAddress(host, port));
        CobraConsumer.BlobRetriever blobRetriever = new FilesystemBlobRetriever(cacheDir);

        consumer = CobraConsumer.fromBuilder()
                .withNetworkClient(client)
                .withBlobRetriever(blobRetriever)
                .build();

        consumer.poll();

        api = new CobraRecordApi(consumer);
    }

    public CobraConsumer consumer() {
        return consumer;
    }

    public RecordApi api() {
        return api;
    }
}
