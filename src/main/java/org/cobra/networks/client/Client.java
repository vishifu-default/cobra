package org.cobra.networks.client;

import org.cobra.commons.errors.AuthenticationException;
import org.cobra.networks.requests.AbstractRequest;
import org.cobra.networks.requests.RequestCompletionCallback;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

public interface Client extends Closeable {

    /**
     * Test if application is currently ready to send request to the server, if not yet, attempt to connect.
     *
     * @param nowMs now timestamp (ms).
     * @return true if client is ready, otherwise false.
     */
    boolean ready(long nowMs);

    /**
     * Test if client is ready, means that we can send request to server, if not yet, the client is not ready.
     *
     * @param nowMs current timestamp (ms).
     * @return true if we are ready to immediately send request, otherwise false.
     */
    boolean isReady(long nowMs);

    /**
     * @return true if the client is still active. Otherwise, if client called close or beginShutdown, return false.
     */
    boolean active();

    /**
     * Test if the connection has failed, based on the connection state. Such connection failure are
     * usually transient and can be resumed in the next {@link #isReady(long)} call, but there are cases
     * where transient failures needs to be caught and re-acted upon
     *
     * @return true if socket connection has failed and be disconnected
     */
    boolean isConnectFailed();

    /**
     * Check if authentication has failed, based on the connection state. Authentication failures are
     * propagated without any retries
     *
     * @return an option of exception if authentication has failed, otherwise empty
     */
    Optional<AuthenticationException> authenticationException();


    /**
     * Get the number of milliseconds to wait, based on the connection state, before attempt to send data. When
     * disconnected, this respects the reconnect backoff time. When connecting/disconnection, this handles
     * slow/stalled connections
     *
     * @param nowMs current timestamp (ms)
     * @return milliseconds to wait
     */
    long ioLatencyMs(long nowMs);

    /**
     * Send request to server, this is blocking-io operation
     *
     * @param clientRequest request to send
     * @param nowMs         current time (ms)
     * @return true if send is complete, otherwise false.
     */
    boolean send(ClientRequest clientRequest, long nowMs);

    /**
     * Disconnect the socket connection, if there is one. Any pending ClientRequest for this connection
     * will receive cancelled
     */
    void disconnect() throws IOException;


    /**
     * Create new ClientRequest
     *
     * @param requestBuilder request builder to use for building request
     * @param creationMs     creation time of request (in millis)
     * @return a new ClientRequest
     */
    ClientRequest createClientRequest(AbstractRequest.Builder<?> requestBuilder,
                                      long creationMs);

    /**
     * Create new ClientRequest with callback
     *
     * @param requestBuilder request builder use for building request
     * @param creationMs     creation time of request (in millis)
     * @param callback       callback to invoked after received response
     * @return a new ClientRequest
     */
    ClientRequest createClientRequest(AbstractRequest.Builder<?> requestBuilder,
                                      long creationMs,
                                      RequestCompletionCallback callback);
}
