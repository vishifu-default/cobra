package org.cobra.networks.protocol;

public interface ApiMessage extends Message {

    /**
     * @return {@link Apikey} id of message type.
     */
    short apikeyId();
}
