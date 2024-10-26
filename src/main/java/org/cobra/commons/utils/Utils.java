package org.cobra.commons.utils;

import org.cobra.commons.errors.CobraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void swallow(String name, Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.error("{} swallow error", name, e);
        }
    }

    public static void closeAllIfPossible(AutoCloseable... cs) throws CobraException {
        Throwable cause = null;
        for (AutoCloseable c : cs) {
            if (c == null)
                continue;
            try {
                c.close();
            } catch (Exception e) {
                if (cause == null) cause = e;
                else cause.addSuppressed(e);
            }
        }

        if (cause != null)
            throw new CobraException(cause);
    }

    public static void closeSilently(String name, AutoCloseable... cs) {
        for (AutoCloseable c : cs)
            closeSilently(name, c);
    }

    public static void closeSilently(String name, Collection<AutoCloseable> cs) {
        for (AutoCloseable c : cs)
            closeSilently(name, c);
    }

    public static void closeSilently(String name, AutoCloseable c) {
        if (c == null)
            return;

        try {
            c.close();
        } catch (Exception e) {
            log.error("Fail to close resource {}", name, e);
        }
    }

    public static long max(long first, long... values) {
        long max = first;
        for (long v : values)
            max = Math.max(max, v);

        return max;
    }

    public static long min(long first, long... values) {
        long min = first;
        for (long v : values)
            min = Math.min(min, v);

        return min;
    }

    public static int max(int first, int... values) {
        int max = first;
        for (int v : values)
            max = Math.max(max, v);

        return max;
    }

    public static int min(int first, int... values) {
        int min = first;
        for (int v : values)
            min = Math.min(min, v);

        return min;
    }
}
