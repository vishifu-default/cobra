package org.cobra.networks.auth;

public interface PrincipalBuilder {

    /**
     * Build a {@link CobraPrincipal} base on authentication context.
     *
     * @param authContext authentication context.
     * @return a new applicable principal.
     */
    CobraPrincipal build(AuthenticationContext authContext);

}
