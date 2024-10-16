package org.cobra.networks.plaintext;

import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.ChannelBuilder;
import org.cobra.networks.CobraChannel;
import org.cobra.networks.TransportLayer;
import org.cobra.networks.auth.Authenticator;

import java.nio.channels.SelectionKey;
import java.util.function.Supplier;

public class PlaintextChannelBuilder implements ChannelBuilder {

    @Override
    public CobraChannel build(String id, SelectionKey selectionKey, MemoryAlloc memoryAlloc) {
        final PlaintextTransportLayer transportLayer = buildTransportLayer(selectionKey);
        final Supplier<Authenticator> authenticatorSupplier = () -> new PlaintextAuthenticator(transportLayer);
        final MemoryAlloc memAllocation = memoryAlloc == null ? MemoryAlloc.NONE : memoryAlloc;

        return buildChannel(id, transportLayer, authenticatorSupplier, memAllocation);
    }

    @Override
    public void close() throws Exception {
        // nop
    }

    protected CobraChannel buildChannel(
            String id,
            TransportLayer transportLayer,
            Supplier<Authenticator> authenticatorSupplier,
            MemoryAlloc memoryAlloc) {
        return new CobraChannel(id, transportLayer, authenticatorSupplier, memoryAlloc);
    }

    PlaintextTransportLayer buildTransportLayer(SelectionKey selectionKey) {
        return new PlaintextTransportLayer(selectionKey);
    }
}
