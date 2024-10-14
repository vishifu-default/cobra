package org.cobra.commons;

import org.cobra.networks.auth.PrincipalBuilder;
import org.cobra.networks.auth.PrincipalBuilderImpl;

/**
 * Should place static members, that will give information of application in runtime config, something that will
 * never or less be changed in runtime.
 */
public class Jvm {

    /* The default PrincipalBuilder clazz */
    public static final Class<? extends PrincipalBuilder> DEFAULT_PRINCIPAL_BUILDER = PrincipalBuilderImpl.class;

    /* By using this constant, we'll let OS determine socket buffer size */
    public static final int USE_DEFAULT_SOCKET_BUFFER_SIZE = -1;

    public static final long INF_TIMESTAMP = -1L;
}
