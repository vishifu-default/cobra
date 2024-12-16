package org.serialization;

import org.cobra.commons.utils.Utils;
import org.cobra.core.RecordSchema;
import org.cobra.core.serialization.RecordSerde;
import org.cobra.core.serialization.RecordSerdeImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
public class SerdeBenchmark {

    private static final Logger log = LoggerFactory.getLogger(SerdeBenchmark.class);

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .addProfiler(GCProfiler.class)
                .include(SerdeBenchmark.class.getSimpleName())
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void serialize() {
        final int iterations = 100;
        final RecordSerde serde = new RecordSerdeImpl();
        serde.register(new RecordSchema(SampleA.class));

        for (int i = 0; i < iterations; i++) {
            SampleA sample = new SampleA();
            sample.id = Utils.randLong();
            sample.sampleB = new SampleB();
            sample.sampleB.id = Utils.randInt();
            sample.sampleB.name = new String(Utils.randBytes(128));

            byte[] raw = serde.serialize(sample);

            SampleA ret = serde.deserialize(raw);
        }
    }
}
