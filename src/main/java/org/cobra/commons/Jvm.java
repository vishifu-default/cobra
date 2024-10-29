package org.cobra.commons;

import org.cobra.core.encoding.VarLenHandles;
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

    /* Indicating an infinite timestamp */
    public static final long INF_TIMESTAMP = -1L;

    public static final int MAX_SINGLE_BUFFER_CAPACITY = (1 << 30);

    /* Control the assertion define */
    public static boolean SKIP_ASSERTION = false;

    /* An instance that provides UNSAFE manipulation for memory */
    public static UnsafeMemory memory() {
        return UnsafeMemory.INSTANCE;
    }

    /* An singleton instance that provide var-len handles */
    public static VarLenHandles varLenHandles() {
        return VarLenHandles.INSTANCE;
    }

    public static class File {
        public static final String READ_ONLY_MODE = "r";
    }
}
