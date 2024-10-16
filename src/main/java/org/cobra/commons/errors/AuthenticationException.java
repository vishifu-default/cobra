package org.cobra.commons.errors;

import java.io.Serial;

/**
 * An authentication exception used to indicate failure in attempt in order to connect/send/receive data over socket
 */
public class AuthenticationException extends CobraException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}
