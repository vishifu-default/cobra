package org.cobra.networks.auth;

import org.cobra.networks.plaintext.PlaintextAuthenticationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrincipalBuilderImpl implements PrincipalBuilder {

    private static final Logger log = LoggerFactory.getLogger(PrincipalBuilderImpl.class);

    @Override
    public CobraPrincipal build(AuthenticationContext context) {
        return switch (context) {
            case PlaintextAuthenticationContext ignored -> CobraPrincipal.ANONYMOUS;
            case null, default -> {
                log.warn("Unknown authentication context: {}", context);
                throw new IllegalArgumentException("Unknown authentication context " + context);
            }
        };
    }

    @Override
    public String toString() {
        return "PrincipalBuilderImpl()";
    }
}
