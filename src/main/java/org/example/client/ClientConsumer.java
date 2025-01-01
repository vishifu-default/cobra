package org.example.client;

import org.cobra.RecordApi;
import org.cobra.api.CobraRecordApi;
import org.cobra.consumer.CobraConsumer;
import org.cobra.consumer.fs.FilesystemBlobRetriever;
import org.cobra.consumer.fs.RemoteFilesystemBlobRetriever;
import org.cobra.core.memory.MemoryMode;
import org.cobra.networks.CobraClient;
import org.cobra.networks.NetworkConfig;
import org.example.datamodel.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class ClientConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClientConsumer.class);

    public static void main(String[] args) throws InterruptedException {
        Path consumePath = Paths.get("./src/main/resources/consume-dir");
        CobraClient client = new CobraClient(new InetSocketAddress(NetworkConfig.DEFAULT_LOCAL_NETWORK_SOCKET, NetworkConfig.DEFAULT_PORT));
        CobraConsumer.BlobRetriever blobRetriever = new FilesystemBlobRetriever(consumePath,
                new RemoteFilesystemBlobRetriever(client, consumePath));

        CobraConsumer consumer = CobraConsumer.fromBuilder()
                .withBlobRetriever(blobRetriever)
                .withMemoryMode(MemoryMode.ON_HEAP)
                .withNetworkClient(client)
                .build();


        consumer.triggerRefreshWithDelay(300);

        RecordApi recordApi = new CobraRecordApi(consumer);

        Random rand = new Random();
        while (true) {
            Thread.sleep(10_000);
            for (int i = 0; i < 10_000; i++) {
                Movie movie = recordApi.get(String.valueOf(rand.nextInt()));
                if (movie != null) {
                    log.info("FIND MOVIE {}", movie.id);
                }
            }
        }
    }
}
