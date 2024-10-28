package org.benchmark.serialization;

import org.cobra.commons.utils.Utils;
import org.cobra.core.RecordSchema;
import org.cobra.core.serialization.RecordSerde;
import org.cobra.core.serialization.RecordSerdeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SerdeBenchmark {
    private static final Logger log = LoggerFactory.getLogger(SerdeBenchmark.class);

    // Sample run
    // 2024-10-29T02:39:07.529 [INFO ] - Serialized 100000 records in 111ms; bytes: 24893006
    // 2024-10-29T02:39:07.639 [INFO ] - Deserialized 100000 records in 110ms
    public static void main(String[] args) {
        List<SampleA> sampleList = generateSampleData(100_000);

        RecordSerde serde = new RecordSerdeImpl();
        serde.register(new RecordSchema(SampleA.class));

        List<byte[]> serializedBytes = new ArrayList<>();

        long startMs = System.currentTimeMillis();
        for (SampleA sample : sampleList) {
            byte[] bytes = serde.serialize(sample);
            serializedBytes.add(bytes);
        }

        long endMs = System.currentTimeMillis();
        int serializeByteSize = serializedBytes.stream().mapToInt(bytes -> bytes.length).sum();
        log.info("Serialized {} records in {}ms; bytes: {}", sampleList.size(), endMs - startMs, serializeByteSize);

        List<SampleA> deserialized = new ArrayList<>();
        startMs = System.currentTimeMillis();
        for (byte[] bytes : serializedBytes) {
            SampleA sample = serde.deserialize(bytes);
            deserialized.add(sample);
        }
        endMs = System.currentTimeMillis();
        log.info("Deserialized {} records in {}ms", deserialized.size(), endMs - startMs);
    }

    private static List<SampleA> generateSampleData(int num) {
        List<SampleA> samples = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            SampleA sample = new SampleA();
            sample.id = Utils.randLong();
            sample.sampleB = new SampleB();
            sample.sampleB.id = Utils.randInt();
            sample.sampleB.name = new String(Utils.randBytes(128));

            samples.add(sample);
        }

        return samples;
    }
}
