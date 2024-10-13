package org.cobra.networks.auth;

import java.security.Principal;
import java.util.Objects;

/**
 * Hold applicable principal piece information
 */
public class CobraPrincipal implements Principal {

    public static final String USER_TYPE = "user";
    private static final String ANONYMOUS_NAME = "ANONYMOUS";

    public static final CobraPrincipal ANONYMOUS = new CobraPrincipal(USER_TYPE, ANONYMOUS_NAME);

    private final String principalType;
    private final String name;

    public CobraPrincipal(String principalType, String name) {
        this.principalType = Objects.requireNonNull(principalType, "principal type is null");
        this.name = Objects.requireNonNull(name, "name is null");
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPrincipalType() {
        return principalType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CobraPrincipal that = (CobraPrincipal) o;
        return name.equals(that.name)
                && principalType.equals(that.principalType);
    }

    @Override
    public int hashCode() {
        return 31 * principalType.hashCode() + name.hashCode();
    }
}
