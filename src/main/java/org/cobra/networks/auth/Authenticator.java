package org.cobra.networks.auth;

import org.cobra.commons.errors.AuthenticationException;

import java.io.IOException;
import java.util.Optional;

/**
 * Present an authentication actions that could be performed in a context.
 * Do "authenticate" if channel is not ready, retrieve "principal" when need
 */
public interface Authenticator extends AutoCloseable {

    /**
     * Implement any authentication mechanism, use transport layer to read/write
     */
    void authenticate() throws AuthenticationException, IOException;

    /**
     * @return true if authentication process is completed.
     */
    boolean isCompleted();

    /**
     * @return current in use principal
     */
    CobraPrincipal principal();

    /**
     * @return session expired time (nanos), non-null on server-side. After this time, the server-side will locally
     * close the socket connection, if no re-authentication present.
     */
    default Optional<Long> serverSideSessionExpiryTimeNanos() {
        return Optional.empty();
    }

    /**
     * perform any related process due to failure in authentication
     */
    default void handleAuthenticationFailure() {
        // nop
    }
}
