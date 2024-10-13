package org.cobra.commons.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobraThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(CobraThread.class);

    public CobraThread(final String name, final boolean daemon) {
        super(name);
        setupThread(name, daemon);
    }

    public CobraThread(final Runnable task, final String name, final boolean daemon) {
        super(task, name);
        setupThread(name, daemon);
    }

    public static CobraThread daemon(final Runnable task, final String name) {
        return new CobraThread(task, name, true);
    }

    public static CobraThread nonDaemon(final Runnable task, final String name) {
        return new CobraThread(task, name, false);
    }

    private void setupThread(final String name, final boolean daemon) {
        setDaemon(daemon);
        setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception; thread: {}", name, e));
    }
}
