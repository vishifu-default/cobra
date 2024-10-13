package org.cobra.commons.errors;

import java.io.Serial;

/**
 * To error an illegal receive that have wrong receive size (maybe that is negative/not match ...)
 */
public class InvalidSocketReceiveSizeException extends CobraException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidSocketReceiveSizeException(int size) {
        super("invalid receive size: " + size);
    }

}
