package org.cobra.networks;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.cobra.commons.errors.CobraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CobraServer implements Network {

    private static final Logger log = LoggerFactory.getLogger(CobraServer.class);
    private final InetSocketAddress inetSocketAddress;

    private final EventLoopGroup masterGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final Set<ChannelHandler> channelHandlers = new HashSet<>();

    private io.netty.channel.Channel nettyChannel;

    public CobraServer(InetSocketAddress socketAddress) {
        this(socketAddress, new HashSet<>());
    }

    public CobraServer(InetSocketAddress socketAddress, Set<ChannelHandler> initHandlers) {
        this.inetSocketAddress = socketAddress;
        this.channelHandlers.addAll(initHandlers);
    }

    @Override
    public InetSocketAddress getAddress() {
        return inetSocketAddress;
    }

    @Override
    public void bootstrap() {
        ServerBootstrap bootstrap = init();
        try {
            nettyChannel = bootstrap.bind().sync().channel();
            log.debug("server {} started", this);
        } catch (InterruptedException e) {
            log.error("interrupted while starting server {}", this, e);
            throw new CobraException(e);
        }
    }

    @Override
    public void shutdown() {
        log.debug("server {} shutting down...", this);

        try {
            masterGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            nettyChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.warn("interrupted while shutting down server {}", this, e);
            throw new CobraException(e);
        }

        log.debug("server {} shutdown", this);
    }

    public void registerHandler(ChannelHandler... handler) {
        channelHandlers.addAll(List.of(handler));
    }

    private ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();
                for (ChannelHandler handler : channelHandlers) {
                    pipeline.addLast(handler);
                }
            }
        };
    }

    private ServerBootstrap init() {
        return new ServerBootstrap()
                .group(masterGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 5000)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .localAddress(getAddress())
                .childHandler(channelInitializer());
    }
}
