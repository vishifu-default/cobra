package org.cobra.consumer;

import org.cobra.RecordApi;
import org.cobra.api.CobraRecordApi;
import org.cobra.commons.Clock;
import org.cobra.consumer.fs.FilesystemBlobRetriever;
import org.cobra.consumer.fs.RemoteFilesystemBlobRetriever;
import org.cobra.core.memory.MemoryMode;
import org.cobra.networks.CobraClient;
import org.cobra.networks.NetworkConfig;
import org.cobra.producer.CobraProducer;
import org.cobra.producer.fs.FilesystemAnnouncer;
import org.cobra.producer.fs.FilesystemBlobStagger;
import org.cobra.producer.fs.FilesystemPublisher;
import org.cobra.producer.internal.SequencedVersionMinter;
import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilesystemConsumerTest {

    private static final Logger log = LoggerFactory.getLogger(FilesystemConsumerTest.class);

    @Test
    void multithread_read() throws InterruptedException {
        Path publishDirPath = Path.of("src", "test", "resources", "consumer-test", "producer-store");
        File publishDir = publishDirPath.toFile();

        publishDir.mkdir();

        log.info("producer publish to dir:{}", publishDir.getAbsolutePath());

        CobraProducer producer = CobraProducer.fromBuilder()
                .withBlobPublisher(new FilesystemPublisher(publishDirPath))
                .withBlobStagger(new FilesystemBlobStagger())
                .withAnnouncer(new FilesystemAnnouncer(publishDirPath))
                .withClock(Clock.system())
                .withBlobStorePath(publishDirPath)
                .buildSimple();

        producer.registerModel(ConsumeTypeA.class);
        producer.bootstrapServer();

        producer.produce(task -> {
            for (int i = 0; i < 10_000; i++) {
                ConsumeTypeA consumeTypeA = new ConsumeTypeA(i, "test-" + i, false,
                        new ConsumeTypeB(i, 2.2));
                task.addObject(consumeTypeA.name, consumeTypeA);
            }
        });

        CobraClient client = new CobraClient(new InetSocketAddress(NetworkConfig.DEFAULT_LOCAL_NETWORK_SOCKET, NetworkConfig.DEFAULT_PORT));
        CobraConsumer.BlobRetriever blobRetriever = new FilesystemBlobRetriever(publishDirPath,
                new RemoteFilesystemBlobRetriever(client, publishDirPath));

        CobraConsumer consumer = CobraConsumer.fromBuilder()
                .withBlobRetriever(blobRetriever)
                .withMemoryMode(MemoryMode.ON_HEAP)
                .withNetworkClient(client)
                .build();

        ((CobraConsumerImpl) consumer).triggerRefresh();

        final RecordApi api = new CobraRecordApi(consumer);

        ConsumeTypeA ret1 = api.get("test-1");

        Assertions.assertNotNull(ret1);
        Assertions.assertEquals("test-1", ret1.name);
    }

}
