package org.cobra.commons.utils;

import java.util.Random;

public class Rands {

    private static final Random RAND = new Random();

    public static int randInt() {
        return RAND.nextInt();
    }

    public static int randInt(int min, int bound) {
        return RAND.nextInt(min, bound);
    }

    public static long randLong() {
        return RAND.nextLong();
    }

    public static long randLong(int min, int bound) {
        return RAND.nextLong(min, bound);
    }
}
