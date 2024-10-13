package org.cobra.networks.protocol;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Apikey {

    SAMPLE_REQUEST((short) 0, "SAMPLE");

    private static final Map<Integer, Apikey> ID_TO_APIKEY = Arrays.stream(Apikey.values())
            .collect(Collectors.toMap(e -> (int) e.id(), e -> e));

    private final short id;
    private final String name;

    Apikey(short id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Apikey ofId(int id) {
        Apikey apikey = ID_TO_APIKEY.get(id);
        if (apikey == null)
            throw new IllegalArgumentException("Unknown apikey: " + id);

        return apikey;
    }

    public static boolean supportApikey(int id) {
        return ID_TO_APIKEY.containsKey(id);
    }

    public short id() {
        return id;
    }

    public String getName() {
        return name;
    }
}
