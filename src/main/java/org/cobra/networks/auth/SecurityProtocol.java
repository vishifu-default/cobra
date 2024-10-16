package org.cobra.networks.auth;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum SecurityProtocol {

    PLAINTEXT(0, "PLAINTEXT"),
    SASL_PLAINTEXT(1, "SASL_PLAINTEXT");

    private static final Map<Short, SecurityProtocol> codeToProtocol;

    /* Complied a map of code -> protocol */
    static {
        SecurityProtocol[] values = SecurityProtocol.values();
        Map<Short, SecurityProtocol> codeMapToProtocol = new HashMap<>();
        for (SecurityProtocol protocol : values) {
            codeMapToProtocol.put(protocol.id, protocol);
        }
        codeToProtocol = codeMapToProtocol;
    }

    public final short id;
    public final String name;

    SecurityProtocol(int id, String name) {
        this.id = (short) id;
        this.name = name;
    }

    public static SecurityProtocol ofId(int id) {
        return codeToProtocol.get((short) id);
    }

    public static SecurityProtocol ofName(String name) {
        return SecurityProtocol.valueOf(name.toUpperCase(Locale.ROOT));
    }
}
