package org.cobra.commons.errors;

import java.io.Serial;

public class DelayedResponseAuthenticationException extends AuthenticationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DelayedResponseAuthenticationException(Throwable cause) {
        super(cause);
    }
}
