package org.sample;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.SampleTime)
public class SampleBenchmark {
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .addProfiler(GCProfiler.class)
                .include(SampleBenchmark.class.getSimpleName())
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void counter() {
        int count = 0;
        for (int i = 0; i < 1_000; i++) {
            count++;
        }
    }
}
