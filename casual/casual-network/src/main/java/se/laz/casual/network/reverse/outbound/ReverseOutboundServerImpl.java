/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.reverse.outbound;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import se.laz.casual.network.EventLoopClient;
import se.laz.casual.network.EventLoopFactory;
import se.laz.casual.network.outbound.JEEConcurrencyFactory;
import se.laz.casual.network.outbound.NettyConnectionInformation;
import se.laz.casual.network.outbound.NettyNetworkConnection;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Reverse outbound "server": listens on a port that the EIS connects to.
 * On accept we issue the domain connect request, just as if we had connected to the EIS - the
 * reverse inbound side waits for us, the outbound side, to initiate the handshake.
 * The established connection is delivered through the connection consumer for casual-caller use.
 */
public class ReverseOutboundServerImpl implements ReverseOutboundServer
{
    private static final Logger LOG = Logger.getLogger(ReverseOutboundServerImpl.class.getName());

    private final String name;
    private final Channel serverChannel;
    private final ChannelGroup connectedChannels;
    private final InetSocketAddress address;
    private final AtomicBoolean deactivated = new AtomicBoolean(false);

    private ReverseOutboundServerImpl(String name, Channel serverChannel, ChannelGroup connectedChannels, InetSocketAddress address)
    {
        this.name = name;
        this.serverChannel = serverChannel;
        this.connectedChannels = connectedChannels;
        this.address = address;
    }

    public static ReverseOutboundServer of(ReverseOutboundConnectionInformation ci)
    {
        Objects.requireNonNull(ci, "connectionInformation can not be null");
        Objects.requireNonNull(ci.getName(), "name can not be null");
        ChannelGroup connectedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        Channel ch = init(ci, connectedChannels);
        ReverseOutboundServerImpl server = new ReverseOutboundServerImpl(ci.getName(), ch, connectedChannels, (InetSocketAddress) ch.localAddress());
        LOG.info(() -> "reverse outbound listening on: " + server.getPort() + " for name: " + ci.getName());
        return server;
    }

    private static Channel init(final ReverseOutboundConnectionInformation ci, final ChannelGroup connectedChannels)
    {
        // accept
        EventLoopGroup bossGroup = EventLoopFactory.getInstance(EventLoopClient.REVERSE_OUTBOUND_BOSS_GROUP);
        // client work
        EventLoopGroup workerGroup = EventLoopFactory.getInstance(EventLoopClient.REVERSE_OUTBOUND);
        ServerBootstrap b = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(ci.getChannelClass())
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel channel)
                    {
                        connectedChannels.add(channel);
                        // the handshake blocks awaiting the reply so it can not run on the event loop thread
                        CompletableFuture.runAsync(() -> handshake(channel, ci), JEEConcurrencyFactory.getExecutorService())
                                         .exceptionally(exception -> {
                                             LOG.warning(() -> "reverse outbound handshake failed for name=" + ci.getName() + ", closing connection: " + exception);
                                             channel.close();
                                             return null;
                                         });
                    }
                });
        LOG.finest(() -> "reverse outbound about to bind to port: " + ci.getPort() + " name=" + ci.getName());
        return b.bind(ci.getPort()).syncUninterruptibly().channel();
    }

    private static void handshake(SocketChannel channel, ReverseOutboundConnectionInformation ci)
    {
        NettyConnectionInformation nettyCi = NettyConnectionInformation.createBuilder()
                                                                       .withAddress(channel.remoteAddress())
                                                                       .withDomainId(ci.getDomainId())
                                                                       .withDomainName(ci.getDomainName())
                                                                       .build();
        // connection removal from the pool is handled via a listener that the consumer adds
        NettyNetworkConnection connection = NettyNetworkConnection.ofAcceptedChannel(channel, nettyCi,
                exception -> LOG.finest(() -> "reverse outbound connection gone for name=" + ci.getName()));
        ci.getConnectionConsumer().accept(connection);
        LOG.info(() -> "reverse outbound connection established for name=" + ci.getName() + " towards domain: " + connection.getDomainId());
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getPort()
    {
        return address.getPort();
    }

    @Override
    public void deactivate()
    {
        if(!deactivated.compareAndSet(false, true))
        {
            return;
        }
        // note: the event loop group is shared between all reverse outbound instances and
        // any live connections, it is never shut down here
        serverChannel.close().syncUninterruptibly();
        // close any connections that have been handed off after handshake, their close listeners
        // issue the disconnect notifications
        connectedChannels.close().awaitUninterruptibly();
        LOG.info(() -> "reverse outbound deactivated for: " + name );
    }

    @Override
    public String toString()
    {
        return "ReverseOutboundServerImpl{" +
                "name='" + name + '\'' +
                ", address=" + address +
                '}';
    }
}
