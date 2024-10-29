package org.cobra.commons.threads;

import org.cobra.commons.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobraThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(CobraThread.class);

    public static final int NORMAL_PRIORITY = Thread.NORM_PRIORITY;
    public static final int LOW_PRIORITY = Thread.MIN_PRIORITY;
    public static final int HIGH_PRIORITY = Thread.MAX_PRIORITY;

    public CobraThread(final Runnable task, final String name, final boolean daemon, int priority) {
        super(task, name);
        setupThread(name, daemon, priority);
    }

    public static CobraThread daemon(Runnable task, Class<?> context, String desc, int priority) {
        return new CobraThread(task, buildContextDesc(context, desc), true, priority);
    }

    public static CobraThread daemon(Runnable task, Class<?> context, String desc) {
        return daemon(task, context, desc, NORMAL_PRIORITY);
    }

    public static CobraThread daemon(Runnable task, String name, int priority) {
        return new CobraThread(task, name, true, priority);
    }

    public static CobraThread daemon(final Runnable task, final String name) {
        return daemon(task, name, NORMAL_PRIORITY);
    }


    public static CobraThread nonDaemon(Runnable task, Class<?> context, String desc, int priority) {
        return new CobraThread(task, buildContextDesc(context, desc), false, priority);
    }

    public static CobraThread nonDaemon(Runnable task, Class<?> context, String desc) {
        return nonDaemon(task, context, desc, NORMAL_PRIORITY);
    }

    public static CobraThread nonDaemon(final Runnable task, final String name, int priority) {
        return new CobraThread(task, name, false, priority);
    }

    public static CobraThread nonDaemon(final Runnable task, final String name) {
        return nonDaemon(task, name, NORMAL_PRIORITY);
    }


    private void setupThread(final String name, final boolean daemon, final int priority) {
        setDaemon(daemon);
        setPriority(priority);
        setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception; thread: {}", name, e));
    }

    private static String buildContextDesc(Class<?> context, String desc) {
        StringBuilder sb = new StringBuilder(context.getSimpleName());
        if (!Strings.isBlank(desc))
            sb.append(".").append(desc);
        return sb.toString();
    }
}
