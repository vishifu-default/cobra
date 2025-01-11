package org.cobra.utils;

import org.junit.jupiter.api.Assertions;

import java.util.Random;
import java.util.function.Supplier;

public class TestUtils {
    private static final long DEFAULT_POOL_INTERVAL_MS = 100;
    public static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String DIGITS = "0123456789";
    public static final String LETTERS_AND_DIGITS = LETTERS + DIGITS;

    public static int randInt(int low, int high) {
        Random rand = new Random();
        return rand.nextInt(low, high);
    }

    public static String randString(int len) {
        Random rand = new Random();
        int boundRand = LETTERS_AND_DIGITS.length();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(LETTERS_AND_DIGITS.charAt(rand.nextInt(boundRand)));
        }
        return sb.toString();
    }

    public static void waitForCondition(final TestCondition conditional, long timeoutMs, String detail) throws Exception {
        waitForCondition(conditional, timeoutMs, () -> detail);
    }

    public static void waitForCondition(final TestCondition conditional, long timeoutMs,
                                        Supplier<String> conditionalDetailer) throws Exception {
        waitForCondition(conditional, timeoutMs, DEFAULT_POOL_INTERVAL_MS, conditionalDetailer);
    }

    public static void waitForCondition(final TestCondition conditional, long timeoutMs, long pollIntervalMs,
                                        Supplier<String> conditionalDetailer) throws Exception {
        retryOnExceptionWithTimeout(timeoutMs, pollIntervalMs, () -> {
            String conditionalDetail = conditionalDetailer != null ? conditionalDetailer.get() : null;
            Assertions.assertTrue(conditional.reachCondition(), String.format("condition not met; timeout = %s; " +
                    "detail = %s", timeoutMs, conditionalDetail));
        });
    }

    public static void retryOnExceptionWithTimeout(
            final long timoutMs,
            long pollIntervalMs,
            final UnitCallable callable) throws InterruptedException {
        long expectedEnd = System.currentTimeMillis() + timoutMs;

        while (true) {
            try {
                callable.call();
                return;
            } catch (AssertionError e) {
                if (expectedEnd <= System.currentTimeMillis()) {
                    throw e;
                }
            } catch (Exception e) {
                if (expectedEnd <= System.currentTimeMillis()) {
                    throw new AssertionError(String.format("assertion failed with an exception after %s ms", timoutMs));
                }
                // ignore, silently continue until timeout
            }
            Thread.sleep(Math.min(pollIntervalMs, timoutMs));
        }
    }
}
