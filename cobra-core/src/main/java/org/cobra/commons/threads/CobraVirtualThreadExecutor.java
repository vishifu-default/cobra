package org.cobra.commons.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;

public class CobraVirtualThreadExecutor {

    private static final Logger log = LoggerFactory.getLogger(CobraVirtualThreadExecutor.class);

    private final List<Future<?>> futures = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();
    private final ThreadFactory threadFactory;

    private CobraVirtualThreadExecutor(String group) {
        this.threadFactory = Thread.ofVirtual().name(group).factory();
    }

    public static CobraVirtualThreadExecutor ofGroup(String group) {
        return new CobraVirtualThreadExecutor(group);
    }

    public void execute(Runnable runnable) {
        Worker worker = new Worker(runnable);
        threads.add(worker.thread);

        worker.thread.start();
    }

    public void await() throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private void runWorker(Worker worker) {
        final Runnable task = worker.task;
        try {
            task.run();
        } catch (Throwable cause) {
            log.error("interrupt execution; {}", cause.getMessage(), cause);
            throw cause;
        }
    }

    private class Worker implements Runnable{

        private final Runnable task;
        private final Thread thread;

        public Worker(Runnable task) {
            this.task = task;
            this.thread = threadFactory.newThread(task);
        }

        @Override
        public void run() {
            runWorker(this);
        }
    }
}
