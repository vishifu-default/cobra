package org.cobra.commons.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobraThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(CobraThread.class);
    public static final int NORMAL_PRIORITY = Thread.NORM_PRIORITY;
    public static final int LOW_PRIORITY = Thread.MIN_PRIORITY;
    public static final int HIGH_PRIORITY = Thread.MAX_PRIORITY;

    public CobraThread(final Runnable task, final String name, final boolean daemon, final int priority) {
        super(task, name);
        setupThread(name, daemon, priority);
    }

    public static CobraThread daemon(Runnable task, String group, String desc, int priority) {
        return new CobraThread(task, buildThreadName(group, desc), true, priority);
    }

    public static CobraThread daemon(Runnable task, String group, String desc) {
        return daemon(task, group, desc, NORMAL_PRIORITY);
    }

    public static CobraThread nonDaemon(Runnable task, String group, String desc, int priority) {
        return new CobraThread(task, buildThreadName(group, desc), false, priority);
    }

    public static CobraThread nonDaemon(Runnable task, String group, String desc) {
        return nonDaemon(task, group, desc, NORMAL_PRIORITY);
    }

    public static Thread ofVirtual(Runnable task, String group, String desc, int priority) {
        Thread thread = Thread.startVirtualThread(task);
        thread.setName(buildThreadName(group, desc));
        thread.setPriority(priority);

        return thread;
    }

    public static Thread ofVirtual(Runnable task, String group, String desc) {
        Thread thread = Thread.startVirtualThread(task);
        thread.setName(buildThreadName(group, desc));
        thread.setPriority(NORMAL_PRIORITY);

        return thread;
    }

    private void setupThread(String name, boolean daemon, int priority) {
        setDaemon(daemon);
        setPriority(priority);
        setUncaughtExceptionHandler((t, e) ->
                log.error("uncaught exception; thread: {};", name, e));
    }

    private static String buildThreadName(String group, String desc) {
        return group + "." + desc;
    }
}
