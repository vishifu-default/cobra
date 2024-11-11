package org.cobra.networks.server;

import org.cobra.commons.Clock;
import org.cobra.commons.Jvm;
import org.cobra.commons.configs.ConfigDef;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.commons.threads.CobraThread;
import org.cobra.commons.utils.Stringx;
import org.cobra.commons.utils.Utils;
import org.cobra.networks.Endpoint;
import org.cobra.networks.auth.SecurityProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SocketAcceptor implements Acceptor {

    private static final Logger log = LoggerFactory.getLogger(SocketAcceptor.class);

    private final String id;
    private final Endpoint endpoint;
    private final ChannelServer channelServer;
    private final Clock clock;
    private final RequestChanel requestChanel;
    private final MemoryAlloc memoryAlloc;
    private final ConfigDef configDef;
    private final CobraThread mainThread;

    private final int sendBufferSize;
    private final int receiveBufferSize;
    private final int listenBacklogSize;

    private final List<Processor> processors;
    private final java.nio.channels.Selector ioSelector;
    private ServerSocketChannel serverSocketChannel;
    private final int localPort;

    private final AtomicBoolean shouldRunning = new AtomicBoolean(true);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private int currentUsedProcessorIndex = 0;

    protected SocketAcceptor(
            String id,
            ChannelServer channelServer,
            Endpoint endpoint,
            Clock clock,
            MemoryAlloc memoryAlloc,
            RequestChanel requestChanel,
            ConfigDef configDef
    ) {
        this.id = id;
        this.endpoint = endpoint;
        this.clock = clock;
        this.memoryAlloc = memoryAlloc;
        this.requestChanel = requestChanel;
        this.configDef = configDef;
        this.channelServer = channelServer;

        this.sendBufferSize = configDef.get(DefaultServerConfigs.SOCKET_SEND_BUFFER_SIZE_CONF).value();
        this.receiveBufferSize = configDef.get(DefaultServerConfigs.SOCKET_RECEIVE_BUFFER_SIZE_CONF).value();
        this.listenBacklogSize = configDef.get(DefaultServerConfigs.SOCKET_LISTEN_BACKLOG_CONF).value();

        String threadName = String.format("%s-cobra-network-acceptor-%s-%s", threadPrefix(),
                endpoint.securityProtocol(), endpoint.port());
        this.mainThread = CobraThread.nonDaemon(this, threadName);


        if (endpoint.port() != 0) this.localPort = endpoint.port();
        else {
            try {
                serverSocketChannel = openServerSocketChannel(endpoint.host(), 0, this.listenBacklogSize);
                this.localPort = serverSocketChannel.socket().getLocalPort();
                log.info("open wildcard endpoint {}:{}", endpoint.host(), localPort);
            } catch (IOException e) {
                throw new CobraException(e);
            }
        }

        try {
            this.ioSelector = java.nio.channels.Selector.open();
        } catch (IOException e) {
            throw new CobraException(e);
        }

        this.processors = new ArrayList<>();
    }

    public abstract String threadPrefix();

    public int getLocalPort() {
        return localPort;
    }

    public void wakeup() {
        ioSelector.wakeup();
    }

    @Override
    public synchronized void start() {
        try {
            if (!shouldRunning.get())
                throw new ClosedChannelException();

            if (serverSocketChannel == null) {
                log.info("open endpoint {}", endpoint);
                serverSocketChannel = openServerSocketChannel(endpoint.host(), endpoint.port(), listenBacklogSize);
            }

            log.info("start processors for endpoint {}", endpoint);
            processors.forEach(Processor::start);

            log.info("start acceptor thread for endpoint {}", endpoint);
            mainThread.start();

            started.set(true);
        } catch (ClosedChannelException e) {
            log.error("refusing to start acceptor of {}, acceptor has been shutdown", endpoint, e);
        } catch (IOException e) {
            log.error("error while start acceptor of {}", endpoint, e);
        }

    }

    @Override
    public void shutdown() {
        if (shouldRunning.getAndSet(false)) {
            wakeup();
            synchronized (this) {
                try {
                    for (Processor processor : processors) processor.close();
                } catch (Exception e) {
                    throw new CobraException(e);
                }
            }
        }
    }

    @Override
    public void closeAll() {
        log.debug("acceptor closing all resources");
        if (serverSocketChannel != null) {
            Utils.swallow("acceptor release server-socket-channel", () -> {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    throw new CobraException(e);
                }
            });
        }
    }

    @Override
    public void close() throws Exception {
        log.trace("closing socket acceptor of {}", endpoint);

        mainThread.join();
        if (!started.get())
            closeAll();

        synchronized (processors) {
            processors.forEach(proc -> {
                try {
                    proc.close();
                } catch (Exception e) {
                    throw new CobraException(e);
                }
            });
        }
    }

    @Override
    public void run() {
        try {
            serverSocketChannel.register(ioSelector, SelectionKey.OP_ACCEPT);
            while (shouldRunning.get()) {
                acceptNewSocketConnection();
            }
        } catch (ClosedChannelException e) {
            log.error("fail to register a closed server channel", e);
            throw new CobraException(e);
        } catch (Throwable cause) {
            log.error("error while running server channel", cause);
        } finally {
            closeAll();
        }
    }

    public Processor initProcessor(int processorId, SecurityProtocol securityProtocol) {
        String threadName = String.format("%s-cobra-network-thread-%s-%s-%d",
                threadPrefix(), id, endpoint.securityProtocol(), processorId);
        final long connMaxIdleMs = configDef.valueOf(DefaultServerConfigs.CONN_MAX_IDLE_MS_CONF);
        final long failedAuthenticationDelayMs = configDef.valueOf(DefaultServerConfigs.CONN_MAX_IDLE_MS_CONF);

        return new Processor(processorId, Processor.DEFAULT_CONN_QUEUED_CAPACITY,
                connMaxIdleMs, failedAuthenticationDelayMs, threadName, clock, securityProtocol, requestChanel, memoryAlloc);
    }

    public void addSocketProcessor(int num) {
        List<Processor> listeningProcessor = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Processor processor = initProcessor(channelServer.assignNextProcessorId(), endpoint.securityProtocol());
            listeningProcessor.add(processor);
            requestChanel.addProcessor(processor);

            if (started.get())
                processor.start();
        }
        processors.addAll(listeningProcessor);
    }

    private ServerSocketChannel openServerSocketChannel(String host, int port, int backlog) throws IOException {
        final InetSocketAddress socketAddress = Stringx.isBlank(host)
                ? new InetSocketAddress(port) :
                new InetSocketAddress(host, port);

        final ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().bind(socketAddress, backlog);

        if (receiveBufferSize != Jvm.USE_DEFAULT_SOCKET_BUFFER_SIZE)
            server.socket().setReceiveBufferSize(receiveBufferSize);

        log.info("awaiting server connection on {}:{}", socketAddress.getHostString(), server.socket().getLocalPort());

        return server;
    }

    private void acceptNewSocketConnection() {
        try {
            final int selected = ioSelector.select(500L); // todo: hardcode
            if (selected == 0)
                return;

            final Iterator<SelectionKey> keyIterator = ioSelector.selectedKeys().iterator();
            while (keyIterator.hasNext() && shouldRunning.get()) {
                /* iterate selectionKey and remove it do prevent duplication */
                final SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (!key.isAcceptable())
                    throw new IllegalStateException("Unrecognized key interest_ops for acceptor; acceptor: " + id +
                            "; interest_ops: " + key.readyOps());

                int retriesLeft;
                synchronized (processors) {
                    retriesLeft = processors.size();
                }

                Processor processor;
                final Optional<SocketChannel> channelOptional = accept(key);
                do {
                    retriesLeft--;
                    synchronized (this) {
                        currentUsedProcessorIndex = currentUsedProcessorIndex % processors.size();
                        processor = processors.get(currentUsedProcessorIndex);
                    }
                    currentUsedProcessorIndex++;
                } while (!assignProcessor(channelOptional.get(), processor, retriesLeft == 0));
            }
        } catch (IOException e) {
            log.error("error while accepting new socket connection", e);
        }
    }

    private Optional<SocketChannel> accept(SelectionKey selectionKey) {
        try {
            final ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
            final SocketChannel socketChannel = server.accept();
            configureAcceptedSocket(socketChannel);
            return Optional.of(socketChannel);
        } catch (IOException e) {
            log.error("error while accepting/configuring socket channel", e);
        }

        return Optional.empty();
    }

    private void configureAcceptedSocket(SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(false);
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.socket().setKeepAlive(true);

        if (sendBufferSize != Jvm.USE_DEFAULT_SOCKET_BUFFER_SIZE)
            socketChannel.socket().setSendBufferSize(sendBufferSize);
    }

    private boolean assignProcessor(SocketChannel socketChannel, Processor processor, boolean maybeBlock) throws SocketException {
        if (socketChannel == null)
            throw new IllegalArgumentException("SocketChannel is null");

        boolean accepted = processor.accept(socketChannel, maybeBlock);
        if (accepted) {
            Socket socket = socketChannel.socket();
            log.debug("accepted socket connection from {} on {}; assign to processor {}; send_size (requested/actual)" +
                            " {}/{}; recv_size (requested/actual) {}/{}",
                    socket.getRemoteSocketAddress(), socket.getLocalSocketAddress(),
                    processor,
                    socket.getSendBufferSize(), sendBufferSize,
                    socket.getReceiveBufferSize(), receiveBufferSize);
        }

        return accepted;
    }

    public static class DataPlaneAcceptor extends SocketAcceptor {

        public static final String PREFIX = "data_plane";

        protected DataPlaneAcceptor(
                String id,
                ChannelServer channelServer,
                Endpoint endpoint,
                Clock clock,
                MemoryAlloc memoryAlloc,
                RequestChanel requestChanel,
                ConfigDef configDef,
                int processorNum) {
            super(id, channelServer, endpoint, clock, memoryAlloc, requestChanel, configDef);
            addSocketProcessor(processorNum);
        }

        @Override
        public String threadPrefix() {
            return PREFIX;
        }
    }
}
