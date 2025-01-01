package org.cobra.manual.consumer;

import org.cobra.RecordApi;
import org.cobra.api.CobraRecordApi;
import org.cobra.consumer.CobraConsumer;
import org.cobra.consumer.fs.FilesystemBlobRetriever;
import org.cobra.consumer.fs.RemoteFilesystemBlobRetriever;
import org.cobra.core.memory.MemoryMode;
import org.cobra.manual.datamodel.Movie;
import org.cobra.networks.CobraClient;
import org.cobra.networks.NetworkConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class Consumer {
    public static void main(String[] args) throws InterruptedException, IOException {
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
            Thread.sleep(rand.nextInt(5_000, 30_000));
            for (int i = 0; i < 1_000; i++) {
                Movie movie = recordApi.get(String.valueOf(i));
            }
        }
    }
}
