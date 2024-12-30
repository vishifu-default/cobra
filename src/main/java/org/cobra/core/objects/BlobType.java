package org.cobra.core.objects;

public enum BlobType {
    DELTA_BLOB("delta"),
    REVERSED_DELTA_BLOB("reversedelta"),
    ;

    private final String prefix;

    BlobType(final String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return this.prefix;
    }
}
