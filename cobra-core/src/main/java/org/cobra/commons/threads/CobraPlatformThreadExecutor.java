package org.cobra.commons.threads;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class CobraPlatformThreadExecutor extends ThreadPoolExecutor {

    private static final Logger log = LoggerFactory.getLogger(CobraPlatformThreadExecutor.class);

    private final List<Future<?>> futures = new ArrayList<>();

    public CobraPlatformThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                       @NotNull TimeUnit unit,
                                       @NotNull BlockingQueue<Runnable> workQueue,
                                       @NotNull ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public static CobraPlatformThreadExecutor ofDaemon(int poolSize, String group, String desc) {
        return new CobraPlatformThreadExecutor(poolSize,
                Runtime.getRuntime().availableProcessors() * poolSize,
                1, TimeUnit.HOURS,
                new LinkedBlockingQueue<>(),
                task -> CobraThread.daemon(task, group, desc));
    }

    public static CobraPlatformThreadExecutor ofNonDaemon(int poolSize, String group, String desc) {
        return new CobraPlatformThreadExecutor(poolSize,
                Runtime.getRuntime().availableProcessors() * poolSize,
                1, TimeUnit.HOURS,
                new LinkedBlockingQueue<>(),
                task -> CobraThread.nonDaemon(task, group, desc));
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (command instanceof RunnableFuture<?>) {
            super.execute(command);
        } else {
            super.execute(newTaskFor(command, Boolean.TRUE));
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        final RunnableFuture<T> task = super.newTaskFor(callable);
        futures.add(task);
        return task;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        final RunnableFuture<T> task = super.newTaskFor(runnable, value);
        futures.add(task);
        return task;
    }

    public void awaitUniterruptibly() {
        shutdown();
        while (!isTerminated()) {
            try {
                awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                log.warn("interrupt awaiting; {}", e.getMessage(), e);
            }
        }
    }

    public void await() throws ExecutionException, InterruptedException {
        awaitUniterruptibly();
        for (Future<?> future : futures) {
            future.get();
        }
    }

    public void awaitCurrentTasks() throws ExecutionException, InterruptedException {
        for (Future<?> future : futures) {
            future.get();
        }

        futures.clear();
    }
}
