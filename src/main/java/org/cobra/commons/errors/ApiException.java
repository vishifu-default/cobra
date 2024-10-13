package org.cobra.commons.errors;

import java.io.Serial;

/**
 * A api exception, all related-api works must use this exception to indicate an unexpected error
 */
public class ApiException extends CobraException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ApiException() {
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
