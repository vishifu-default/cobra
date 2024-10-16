package org.cobra.commons.errors;

import java.io.Serial;

public class CorrelationIDMismatchException extends CobraException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CorrelationIDMismatchException(long expected, long actual) {
        super("CorrelationId is mismatch; expected: " + expected + ", actual: " + actual);
    }
}
