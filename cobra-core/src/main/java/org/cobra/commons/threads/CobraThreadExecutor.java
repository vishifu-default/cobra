package org.cobra.commons.threads;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CobraThreadExecutor extends ThreadPoolExecutor {

    private static final Logger log = LoggerFactory.getLogger(CobraThreadExecutor.class);
    private static final String DEFAULT_THREAD_NAMESPACE_DESCRIPTION = "cobra-executor";

    private static final List<Future<?>> futures = new ArrayList<>();

    public CobraThreadExecutor(int threads, ThreadFactory threadFactory) {
        super(threads, threads, 300, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                threadFactory);
    }

    public CobraThreadExecutor(int threads, Class<?> context, String description, int priority) {
        this(threads, task -> CobraThread.daemon(task, context, description, priority));
    }

    public static CobraThreadExecutor ofPhysicalProcessor(Class<?> context, String desc, int priority) {
        return ofThreadsPerCpu(1, context, desc, priority);
    }

    public static CobraThreadExecutor ofPhysicalProcessor(Class<?> context, String desc) {
        return ofThreadsPerCpu(1, context, desc);
    }

    public static CobraThreadExecutor ofThreadsPerCpu(
            int threadsPerCpu,
            Class<?> context,
            String desc,
            int priority) {
        return new CobraThreadExecutor(Runtime.getRuntime().availableProcessors() * threadsPerCpu,
                context, desc, priority);
    }

    public static CobraThreadExecutor ofThreadsPerCpu(int threadsPerCpu, Class<?> context, String desc) {
        return ofThreadsPerCpu(threadsPerCpu, context, desc, CobraThread.NORMAL_PRIORITY);
    }

    public static CobraThreadExecutor of(int threads, Class<?> context) {
        return of(threads, context, DEFAULT_THREAD_NAMESPACE_DESCRIPTION);
    }

    public static CobraThreadExecutor of(int threads, Class<?> context, String desc) {
        return of(threads, context, desc, Thread.NORM_PRIORITY);
    }

    public static CobraThreadExecutor of(int threads, Class<?> context, String desc, int priority) {
        return new CobraThreadExecutor(threads, context, desc, priority);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (command instanceof RunnableFuture<?>)
            super.execute(command);
        else
            super.execute(newTaskFor(command, Boolean.TRUE));
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

    public void awaitNonInterruption() {
        shutdown();
        if (!isTerminated()) {
            try {
                awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                log.warn("interruption task; error: {}", e.getMessage(), e);
            }
        }

    }

    public void waitAll() throws ExecutionException, InterruptedException {
        awaitNonInterruption();
        for (Future<?> f : futures)
            f.get();
    }

    public void flushCurrentTasks() throws InterruptedException, ExecutionException {
        for (Future<?> f : futures)
            f.get();

        futures.clear();
    }
}
