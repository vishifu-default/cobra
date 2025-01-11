package org.cobra.commons;

/**
 * An applicable time supplier
 */
public interface Clock {

    static Clock system() {
        return new SystemClockImpl();
    }

    /**
     * @return the current applicable milliseconds
     */
    long milliseconds();

    /**
     * @return the current applicable nanoseconds
     */
    long nanoseconds();

    /**
     * An implementation of system time for applicable clock
     */
    final class SystemClockImpl implements Clock {

        @Override
        public long milliseconds() {
            return System.currentTimeMillis();
        }

        @Override
        public long nanoseconds() {
            return System.nanoTime();
        }
    }
}
