package org.cobra.producer;

import org.cobra.commons.Clock;
import org.cobra.commons.pools.BytesPool;
import org.cobra.consumer.CobraConsumer;
import org.cobra.consumer.fs.FsBlobFetcher;
import org.cobra.consumer.internal.ConsumerDataPlane;
import org.cobra.consumer.internal.DataFetcher;
import org.cobra.consumer.read.ConsumerStateContext;
import org.cobra.consumer.read.StateReadEngine;
import org.cobra.core.memory.MemoryMode;
import org.cobra.producer.fs.FsBlobStagger;
import org.cobra.producer.fs.FsPublisher;
import org.cobra.producer.internal.SequencedVersionMinter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FsProducerTest {

    private static final Logger log = LoggerFactory.getLogger(FsProducerTest.class);

    @Test
    void produce() {
        Path publishDirPath = Paths.get("src", "test", "resources", "publish-dir");
        File publishDir = publishDirPath.toFile();

        publishDir.mkdir();

        log.info("producer publish to dir:{}", publishDir.getAbsolutePath());

        CobraProducer producer = CobraProducer.fromBuilder()
                .withBlobPublisher(new FsPublisher(publishDirPath))
                .withBlobStagger(new FsBlobStagger())
                .withVersionMinter(new SequencedVersionMinter())
                .withClock(Clock.system())
                .buildSimple();

        producer.registerModel(TypeA.class);

        producer.produce(task -> {
            for (int i = 0; i < 5; i++) {
                TypeA sampleA = new TypeA(i, "test-%d".formatted(i), false, new TypeB(i, i));
                task.addObject(sampleA.name, sampleA);
            }
        });

        // mock consumer
        ConsumerStateContext consumerStateContext = new ConsumerStateContext();
        ConsumerDataPlane consumerDataPlane = new ConsumerDataPlane(
                new DataFetcher(new FsBlobFetcher(publishDirPath, null)),
                MemoryMode.ON_HEAP,
                new StateReadEngine(consumerStateContext, BytesPool.NONE));

        try {
            consumerDataPlane.update(new CobraConsumer.VersionInformation(1));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
