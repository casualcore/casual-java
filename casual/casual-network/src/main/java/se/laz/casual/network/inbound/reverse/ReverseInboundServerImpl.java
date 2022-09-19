/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import se.laz.casual.network.CasualNWMessageDecoder;
import se.laz.casual.network.CasualNWMessageEncoder;
import se.laz.casual.network.inbound.CasualMessageHandler;
import se.laz.casual.network.inbound.ExceptionHandler;
import se.laz.casual.network.outbound.EventLoopFactory;
import se.laz.casual.network.reverse.inbound.ReverseInboundListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundServer;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Inbound server that connects and then acts exactly like {@link  se.laz.casual.network.inbound.CasualServer}
 * Note that we connect and then casual issues the domain connect request after which we act as if we were inbound all along.
 */
public class ReverseInboundServerImpl implements ReverseInboundServer
{
    private static final Logger LOG = Logger.getLogger(ReverseInboundServerImpl.class.getName());
    private static final String LOG_HANDLER_NAME = "logHandler";
    private static final Duration INITIAL_DURATION = Duration.of(100, ChronoUnit.MILLIS);
    private static final Duration SUBSEQUENT_DURATION = Duration.of(100, ChronoUnit.MILLIS);
    private static final Duration MAX_DURATION = Duration.of(5000, ChronoUnit.MILLIS);
    private static final int STAGGER_FACTOR = 2;
    private final Channel channel;
    private final InetSocketAddress address;

    private ReverseInboundServerImpl(Channel channel, InetSocketAddress address)
    {
        this.channel = channel;
        this.address = address;
    }

    public static ReverseInboundServer of(ReverseInboundConnectionInformation reverseInboundConnectionInformation, ReverseInboundListener eventListener)
    {
        Objects.requireNonNull(reverseInboundConnectionInformation, "connectionInformation can not be null");
        EventLoopGroup workerGroup = EventLoopFactory.getInstance();
        CasualMessageHandler messageHandler = CasualMessageHandler.of(reverseInboundConnectionInformation.getFactory(), reverseInboundConnectionInformation.getXaTerminator(), reverseInboundConnectionInformation.getWorkManager());
        Channel ch = init(reverseInboundConnectionInformation.getAddress(), workerGroup, messageHandler, ExceptionHandler.of(), reverseInboundConnectionInformation.isLogHandlerEnabled());
        ReverseInboundServerImpl server = new ReverseInboundServerImpl(ch, reverseInboundConnectionInformation.getAddress());
        ch.closeFuture().addListener(f -> server.onClose(reverseInboundConnectionInformation, eventListener));
        return server;
    }

    private void onClose(ReverseInboundConnectionInformation reverseInboundConnectionInformation, ReverseInboundListener eventListener)
    {
        // connection gone, need to reconnect while backing off, so we don't spam
        eventListener.disconnected(this);
        StaggeredOptions staggeredOptions = StaggeredOptions.of(INITIAL_DURATION, SUBSEQUENT_DURATION, MAX_DURATION, STAGGER_FACTOR);
        AutoReconnect.of(reverseInboundConnectionInformation, eventListener, staggeredOptions);
    }

    private static Channel init(final InetSocketAddress address, final EventLoopGroup workerGroup, final CasualMessageHandler messageHandler, ExceptionHandler exceptionHandler, boolean enableLogHandler)
    {
        Bootstrap b = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel ch)
                    {
                        ch.pipeline().addLast(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), messageHandler, exceptionHandler);
                        if(enableLogHandler)
                        {
                            ch.pipeline().addFirst(LOG_HANDLER_NAME, new LoggingHandler(LogLevel.INFO));
                            LOG.info(() -> "reverse inbound log handler enabled");
                        }
                    }
                });
        LOG.info(() -> "reverse inbound about to connect to: " + address);
        return b.connect(address).syncUninterruptibly().channel();
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return address;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ReverseInboundServerImpl server = (ReverseInboundServerImpl) o;
        return Objects.equals(channel, server.channel);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(channel);
    }

    @Override
    public String toString()
    {
        return "ReverseInboundServerImpl{" +
                "channel=" + channel +
                ", address=" + address +
                '}';
    }
}