package org.cobra.networks.plaintext;

import org.cobra.commons.errors.AuthenticationException;
import org.cobra.commons.errors.CobraException;
import org.cobra.networks.auth.Authenticator;
import org.cobra.networks.auth.CobraPrincipal;
import org.cobra.networks.auth.PrincipalBuilder;
import org.cobra.networks.auth.PrincipalBuilderFactory;

import java.io.IOException;
import java.net.InetAddress;

public class PlaintextAuthenticator implements Authenticator {

    private final PlaintextTransportLayer transportLayer;
    private final PrincipalBuilder principalBuilder;

    public PlaintextAuthenticator(PlaintextTransportLayer transportLayer) {
        this.transportLayer = transportLayer;
        this.principalBuilder = PrincipalBuilderFactory.create();
    }

    @Override
    public void authenticate() throws AuthenticationException, IOException {
        // nop
    }

    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public CobraPrincipal principal() {
        InetAddress address = transportLayer.channel().socket().getInetAddress();
        return principalBuilder.build(new PlaintextAuthenticationContext(address));
    }

    @Override
    public void close() throws Exception {
        if (principalBuilder instanceof AutoCloseable autoCloseable) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                throw new CobraException(e);
            }
        }
    }

    @Override
    public String toString() {
        return "PlaintextAuthenticator(" +
                "principalBuilder=" + principalBuilder +
                ", transportLayer=" + transportLayer +
                ')';
    }
}
