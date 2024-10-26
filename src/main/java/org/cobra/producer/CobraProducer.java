package org.cobra.producer;

public interface CobraProducer {

    /**
     * Provides a way to mint version for application.
     */
    interface VersionMinter {
        /**
         * Mints a new applicable version
         *
         * @return new version
         */
        long mint();
    }
}
