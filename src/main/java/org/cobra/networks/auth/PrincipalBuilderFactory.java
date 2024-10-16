package org.cobra.networks.auth;

import org.cobra.commons.Jvm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrincipalBuilderFactory {

    private static final Logger log = LoggerFactory.getLogger(PrincipalBuilderFactory.class);

    public static PrincipalBuilder create() {
        Class<?> principalBuilderClazz = Jvm.DEFAULT_PRINCIPAL_BUILDER;
        PrincipalBuilder builder;

        if (principalBuilderClazz == null || principalBuilderClazz == PrincipalBuilderImpl.class) {
            builder = new PrincipalBuilderImpl();
        } else {
            log.warn("Unknown principal builder clazz {}", principalBuilderClazz);
            throw new IllegalStateException("Unknown principal builder clazz " + principalBuilderClazz);
        }

        return builder;
    }
}
