/*
 * Copyright (c) 2022 - 2024, The casual project. All rights reserved.
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
import io.netty.handler.logging.LoggingHandler;
import jakarta.resource.spi.work.WorkManager;
import se.laz.casual.network.CasualNWMessageDecoder;
import se.laz.casual.network.CasualNWMessageEncoder;
import se.laz.casual.network.EventLoopClient;
import se.laz.casual.network.EventLoopFactory;
import se.laz.casual.network.LogLevelProvider;
import se.laz.casual.network.reverse.inbound.ReverseInboundListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundServer;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Inbound "server" that connects and then acts exactly like {@link  se.laz.casual.network.inbound.CasualServer}
 * In fact it is in actuality a client but after connect it behaves as if the connection was initiated from the other side.
 * Note that we connect and then casual issues the domain connect request after which we act as if we were inbound all along.
 */
public class ReverseInboundServerImpl implements ReverseInboundServer
{
    private static final Logger LOG = Logger.getLogger(ReverseInboundServerImpl.class.getName());
    private static final String LOG_HANDLER_NAME = "logHandler";
    private final Channel channel;
    private final InetSocketAddress address;
    private final Supplier<WorkManager> workManagerSupplier;

    private ReverseInboundServerImpl(Channel channel, InetSocketAddress address, Supplier<WorkManager> workManagerSupplier)
    {
        this.channel = channel;
        this.address = address;
        this.workManagerSupplier = workManagerSupplier;
    }

    public static ReverseInboundServer of(ReverseInboundConnectionInformation reverseInboundConnectionInformation, ReverseInboundListener eventListener, Supplier<WorkManager> workManagerSupplier)
    {
        Objects.requireNonNull(reverseInboundConnectionInformation, "connectionInformation can not be null");
        InetSocketAddress address = reverseInboundConnectionInformation.getAddress();
        ReverseInboundMessageHandler messageHandler = ReverseInboundMessageHandler.of(reverseInboundConnectionInformation.getFactory(), reverseInboundConnectionInformation.getXaTerminator(), reverseInboundConnectionInformation.getWorkManager());
        Channel ch = init(address, messageHandler, ReverseInboundExceptionHandler.of(), reverseInboundConnectionInformation.isLogHandlerEnabled(), reverseInboundConnectionInformation.getChannelClass());
        ReverseInboundServerImpl server = new ReverseInboundServerImpl(ch, address, workManagerSupplier);
        ch.closeFuture().addListener(f -> server.onClose(reverseInboundConnectionInformation, eventListener));
        LOG.info(() -> "reverse inbound connected to: " + address);
        return server;
    }

    private void onClose(ReverseInboundConnectionInformation reverseInboundConnectionInformation, ReverseInboundListener eventListener)
    {
        eventListener.disconnected(this);
        AutoReconnect.of(reverseInboundConnectionInformation, eventListener, workManagerSupplier);
    }

    private static Channel init(final InetSocketAddress address, final ReverseInboundMessageHandler messageHandler, ReverseInboundExceptionHandler exceptionHandler, boolean enableLogHandler, Class<? extends Channel> channelClass)
    {
        EventLoopGroup workerGroup = EventLoopFactory.getInstance(EventLoopClient.REVERSE);
        Bootstrap b = new Bootstrap()
                .group(workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel ch)
                    {
                        ch.pipeline().addLast(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), messageHandler, exceptionHandler);
                        if(enableLogHandler)
                        {
                            ch.pipeline().addFirst(LOG_HANDLER_NAME, new LoggingHandler(LogLevelProvider.REVERSE_LOGGING_LEVEL));
                            LOG.info(() -> "reverse inbound log handler enabled, using netty logging level: " + LogLevelProvider.REVERSE_LOGGING_LEVEL);
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
                ", workManagerSupplier=" + workManagerSupplier +
                '}';
    }
}
