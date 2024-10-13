package org.cobra.networks.requests;

@FunctionalInterface
public interface RequestCompletionCallback {

    /**
     * Invoke to consume the callback of request.
     */
    void onComplete();
}
