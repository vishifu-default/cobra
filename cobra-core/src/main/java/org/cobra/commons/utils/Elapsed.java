package org.cobra.commons.utils;

public class Elapsed {
    public static String toStr(long elapsed) {
        if (elapsed < 1_000_000) {
            // Less than 1 millisecond, return in nanoseconds
            return elapsed + " ns";
        } else {
            // Convert to milliseconds
            double elapsedTimeMs = elapsed / 1_000_000.0;
            return String.format("%.3f ms", elapsedTimeMs);
        }
    }

    public static String from(long start) {
        return toStr(System.nanoTime() - start);
    }
}
