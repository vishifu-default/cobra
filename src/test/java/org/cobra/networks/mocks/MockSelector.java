package org.cobra.networks.mocks;

import org.cobra.commons.Clock;
import org.cobra.commons.utils.Utils;
import org.cobra.networks.ChannelState;
import org.cobra.networks.ReceiveNetwork;
import org.cobra.networks.Selectable;
import org.cobra.networks.SendNetwork;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Predicate;

public class MockSelector implements Selectable {

    private final Clock clock;
    private final List<SendNetwork> initiatedSends = new ArrayList<>();
    private final List<SendNetwork> completedSends = new ArrayList<>();
    private final List<ReceiveNetwork> completedReceives = new ArrayList<>();
    private final List<ByteBufferChannel> completedSendBuffer = new ArrayList<>();
    private final List<String> connectedNodes = new ArrayList<>();
    private final Set<String> readyNodes = new HashSet<>();
    private final Map<String, ChannelState> disconnectedNodes = new HashMap<>();
    private final List<DelayedReceive> delayedReceives = new ArrayList<>();
    private final Predicate<InetSocketAddress> canConnect;

    public MockSelector(Clock clock) {
        this(clock, null);
    }

    public MockSelector(Clock clock, Predicate<InetSocketAddress> canConnect) {
        this.clock = clock;
        this.canConnect = canConnect;
    }

    @Override
    public SocketChannel connect(String node, InetSocketAddress address, int sendBufferSize, int receiveBufferSize) throws IOException {
        if (this.canConnect == null || this.canConnect.test(address)) {
            this.connectedNodes.add(node);
            this.readyNodes.add(node);
        }

        return null;
    }

    @Override
    public void wakeup() {
        // nop
    }

    @Override
    public void close(String node) {
        removeSendsOfNode(node, this.completedSends);
        removeSendsOfNode(node, this.initiatedSends);

        this.readyNodes.remove(node);

        for (int i = 0; i < this.connectedNodes.size(); i++) {
            if (this.connectedNodes.get(i).equals(node)) {
                this.connectedNodes.remove(node);
                break;
            }
        }
    }

    private void removeSendsOfNode(String channelId, List<SendNetwork> sends) {
        sends.removeIf(s -> channelId.equals(s.getDestination()));
    }

    @Override
    public void sent(SendNetwork send) {
        this.initiatedSends.add(send);
    }

    @Override
    public List<SendNetwork> completedSends() {
        return this.completedSends;
    }

    @Override
    public List<ReceiveNetwork> completedReceives() {
        return this.completedReceives;
    }

    public void doCompleteReceive(ReceiveNetwork receive) {
        this.completedReceives.add(receive);
    }

    @Override
    public void poll(long timeout) throws IOException {
        completeInitiatedSends();
        completeDelayedReceives();

        Utils.sleep(timeout);
    }

    private void completeDelayedReceives() {
        for (final SendNetwork completedSend : this.completedSends) {
            Iterator<DelayedReceive> delayedReceiveIt = this.delayedReceives.iterator();
            while (delayedReceiveIt.hasNext()) {
                DelayedReceive delayedReceive = delayedReceiveIt.next();
                if (delayedReceive.getSource().equals(completedSend.getDestination())) {
                    this.completedReceives.add(delayedReceive.getReceive());
                    delayedReceiveIt.remove();
                }
            }
        }
    }

    private void completeInitiatedSends() throws IOException {
        for (final SendNetwork send : this.initiatedSends)
            completeSend(send);

        this.initiatedSends.clear();
    }

    private void completeSend(SendNetwork send) throws IOException {
        try (ByteBufferChannel discardChannel = new ByteBufferChannel((int) send.size())) {

            while (!send.isCompleted())
                send.writeTo(discardChannel);

            this.completedSends.add(send);
            this.completedSendBuffer.add(discardChannel);
        }
    }

    @Override
    public Map<String, ChannelState> disconnectedChannels() {
        return this.disconnectedNodes;
    }

    @Override
    public List<String> connectedChannels() {
        List<String> currentConnectedNodes = new ArrayList<>(this.connectedNodes);
        this.connectedNodes.clear();
        return currentConnectedNodes;
    }

    @Override
    public void mute(String channelId) {
        // nop
    }

    @Override
    public void unmute(String channelId) {
        // nop
    }

    @Override
    public boolean isReadyChannel(String channelId) {
        return this.readyNodes.contains(channelId);
    }

    @Override
    public void close() throws IOException {
        // nop
    }

    public void reset() {
        clear();
        this.initiatedSends.clear();
        this.delayedReceives.clear();
    }

    public void clear() {
        this.completedSends.clear();
        this.completedReceives.clear();
        this.completedSendBuffer.clear();
        this.disconnectedNodes.clear();
        this.connectedNodes.clear();
    }
}
