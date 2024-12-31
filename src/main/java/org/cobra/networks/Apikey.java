package org.cobra.networks;

public enum Apikey {

    FETCH_VERSION(1, "FETCH_VERSION"),
    FETCH_HEADER(2, "FETCH_HEADER"),
    FETCH_BLOB(3, "FETCH_BLOB"),;

    private final short id;
    private final String label;

    Apikey(int id, String label) {
        this.id = (short) id;
        this.label = label;
    }

    public int id() {
        return id;
    }

    public String label() {
        return label;
    }
}
