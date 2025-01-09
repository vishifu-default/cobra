package org.cobra.producer;

import org.cobra.RecordApi;
import org.cobra.api.CobraRecordApi;
import org.cobra.commons.Clock;
import org.cobra.consumer.CobraConsumer;
import org.cobra.consumer.CobraConsumerImpl;
import org.cobra.consumer.fs.FilesystemBlobRetriever;
import org.cobra.consumer.fs.RemoteFilesystemBlobRetriever;
import org.cobra.core.memory.MemoryMode;
import org.cobra.networks.CobraClient;
import org.cobra.networks.NetworkConfig;
import org.cobra.producer.fs.FilesystemAnnouncer;
import org.cobra.producer.fs.FilesystemBlobStagger;
import org.cobra.producer.fs.FilesystemPublisher;
import org.cobra.producer.internal.SequencedVersionMinter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FilesystemProducerTest {

    private static final Logger log = LoggerFactory.getLogger(FilesystemProducerTest.class);

    @Test
    void produce_and_consume() {
        Path publishDirPath = Paths.get("src", "test", "resources", "producer-test", "producer-store");
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

        producer.registerModel(TypeA.class);
        producer.bootstrapServer();

        // cycle 1
        producer.produce(task -> {
            for (int i = 0; i < 10; i++) {
                TypeA sampleA = new TypeA(i, "test-%d".formatted(i), false, new TypeB(i, i));
                task.addObject(sampleA.name, sampleA);
            }
        });

        // cycle 2
        producer.produce(task -> {
            for (int i = 0; i < 10; i++) {
                TypeA sampleA = new TypeA(200 + i, "test-2-%d".formatted(i), true, new TypeB(20 + i, 20 + i));
                task.addObject(sampleA.name, sampleA);
            }

            task.removeObject("test-0", TypeA.class);
            task.removeObject("test-1", TypeA.class);
        });

        // cycle 3
        producer.produce(task -> {
            for (int i = 0; i < 20; i++) {
                TypeA sampleA = new TypeA(300 + i, "test-3-%d".formatted(i), true, new TypeB(20 + i, 20 + i));
                task.addObject(sampleA.name, sampleA);
            }

            task.removeObject("test-2-0", TypeA.class);
            task.removeObject("test-2-1", TypeA.class);
        });

        Path consumerStorePath = Paths.get("src", "test", "resources", "producer-test", "consumer-store");
        CobraClient client = new CobraClient(new InetSocketAddress(NetworkConfig.DEFAULT_LOCAL_NETWORK_SOCKET, NetworkConfig.DEFAULT_PORT));
        CobraConsumer.BlobRetriever blobRetriever = new FilesystemBlobRetriever(consumerStorePath,
                new RemoteFilesystemBlobRetriever(client, consumerStorePath));

        CobraConsumer consumer = CobraConsumer.fromBuilder()
                .withBlobRetriever(blobRetriever)
                .withMemoryMode(MemoryMode.ON_HEAP)
                .withNetworkClient(client)
                .build();

        ((CobraConsumerImpl) consumer).triggerRefresh();

        RecordApi api = new CobraRecordApi(consumer);

        TypeA aTest5 = api.get("test-5");
        assertNotNull(aTest5);
        assertEquals("test-5", aTest5.name);

        assertNull(api.get("test-10"));
        assertNull(api.get("test-0"));
    }

    @Test
    void producer_pinVersion() {
        Path publishDirPath = Paths.get("src", "test", "resources", "producer-test", "producer-store");
        File publishDir = publishDirPath.toFile();

        publishDir.mkdir();

        log.info("producer publish to dir:{}", publishDir.getAbsolutePath());

        CobraProducer.Announcer announcer = new FilesystemAnnouncer(publishDirPath);

        CobraProducer producer = CobraProducer.fromBuilder()
                .withBlobPublisher(new FilesystemPublisher(publishDirPath))
                .withBlobStagger(new FilesystemBlobStagger())
                .withAnnouncer(announcer)
                .withClock(Clock.system())
                .withBlobStorePath(publishDirPath)
                .buildSimple();

        producer.registerModel(TypeA.class);
        producer.bootstrapServer();

        // 1
        producer.produce(task -> {
            task.addObject("test-1", new TypeA(1, "test-1", false,
                    new TypeB(1, 2)));
        });

        // 2
        producer.produce(task -> {
            task.addObject("test-2", new TypeA(2, "test-2", false,
                    new TypeB(1, 2)));
        });

        // 3
        producer.produce(task -> {
            task.addObject("test-3", new TypeA(3, "test-3", false,
                    new TypeB(1, 2)));
        });

        // 4
        producer.produce(task -> {
            task.addObject("test-4", new TypeA(4, "test-4", false,
                    new TypeB(1, 2)));
        });

        Path consumerStorePath = Paths.get("src", "test", "resources", "producer-test", "consumer-store");
        CobraClient client = new CobraClient(new InetSocketAddress(NetworkConfig.DEFAULT_LOCAL_NETWORK_SOCKET, NetworkConfig.DEFAULT_PORT));
        CobraConsumer.BlobRetriever blobRetriever = new FilesystemBlobRetriever(consumerStorePath,
                new RemoteFilesystemBlobRetriever(client, consumerStorePath));

        CobraConsumer consumer = CobraConsumer.fromBuilder()
                .withBlobRetriever(blobRetriever)
                .withMemoryMode(MemoryMode.ON_HEAP)
                .withNetworkClient(client)
                .build();

        ((CobraConsumerImpl) consumer).triggerRefresh();


        // pin version -> 2
        producer.pinVersion(2);
        assertEquals(2, (((AbstractProducer) producer).latestVersion()));
        assertEquals(2, announcer.retrieve());


        ((CobraConsumerImpl) consumer).triggerRefresh();

        RecordApi api = new CobraRecordApi(consumer);

        TypeA ret1 = api.get("test-1");
        assertNotNull(ret1);
        assertEquals("test-1", ret1.name);

        TypeA ret2 = api.get("test-2");
        assertNotNull(ret2);
        assertEquals("test-2", ret2.name);

        TypeA ret3 = api.get("test-3");
        assertNull(ret3);
    }
}
