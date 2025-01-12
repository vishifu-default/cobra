package org.cobra.commons.threads;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

class CobraVirtualThreadExecutorTest {

    private static final Logger log = LoggerFactory.getLogger(CobraVirtualThreadExecutorTest.class);
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    @Test
    void await_allTask() throws InterruptedException {
        CobraVirtualThreadExecutor virtualExecutor = CobraVirtualThreadExecutor.ofGroup("test");

        for (int i = 0; i < 100; i++) {
            int j = i;
            virtualExecutor.execute(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logHello(j);
            });
        }

        virtualExecutor.await();

        Assertions.assertEquals(100, atomicInteger.get());
    }

    private static void logHello(int i) {
        log.info("[{}]: hello {}", Thread.currentThread(), i);
        atomicInteger.addAndGet(1);
    }
}