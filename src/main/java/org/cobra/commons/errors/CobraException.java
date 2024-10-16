package org.cobra.commons.errors;

import java.io.Serial;

/**
 * A general runtime exception in application, all unexpected errors, uncaught errors should be thrown as this type.
 * All other applicable exception must extend from this also.
 */
public class CobraException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CobraException() {
    }

    public CobraException(Throwable cause) {
        super(cause);
    }

    public CobraException(String message) {
        super(message);
    }

    public CobraException(String message, Throwable cause) {
        super(message, cause);
    }
}
