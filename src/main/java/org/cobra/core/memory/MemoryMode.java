package org.cobra.core.memory;

public enum MemoryMode {
    ON_HEAP,
    VIRTUAL_MAPPED;

    public boolean isFilteringSupport() {
        return this.equals(ON_HEAP);
    }
}
