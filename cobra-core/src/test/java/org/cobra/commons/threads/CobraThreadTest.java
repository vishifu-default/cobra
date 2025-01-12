package org.cobra.commons.threads;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CobraThreadTest {

    private static final Logger log = LoggerFactory.getLogger(CobraThreadTest.class);

    @Test
    void daemon_run() throws InterruptedException {
        CobraThread thread = CobraThread.daemon(() -> log.info("{}: hello", Thread.currentThread()), "test", "daemon");
        thread.start();
        thread.join();
    }

}