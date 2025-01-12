package org.cobra.commons.threads;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

class CobraPlatformThreadExecutorTest {

    private static final Logger log = LoggerFactory.getLogger(CobraPlatformThreadExecutorTest.class);

    @Test
    void await_allTask() throws InterruptedException, ExecutionException {
        CobraPlatformThreadExecutor platformExecutor = CobraPlatformThreadExecutor.ofDaemon(8, "test", "waiting");

        for (int i = 0; i < 100; i++) {
            int j = i;
            platformExecutor.execute(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logHello(j);
            });
        }

        platformExecutor.await();

        Assertions.assertEquals(100, atomicInteger.get());
    }

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    private static void logHello(int i) {
        log.info("[{}]: hello {}", Thread.currentThread(), i);
        atomicInteger.addAndGet(1);
    }
}