/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.network.CasualNWMessageDecoder;
import se.laz.casual.network.CasualNWMessageEncoder;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class NettyNetworkConnection implements NetworkConnection
{
    private static final Logger LOG = Logger.getLogger(NettyNetworkConnection.class.getName());
    private static final String LOG_HANDLER_NAME = "logHandler";
    private final BaseConnectionInformation ci;
    private final Correlator correlator;
    private final Channel channel;
    private final EventLoopGroup workerGroup;
    private final AtomicBoolean connected = new AtomicBoolean(true);

    private NettyNetworkConnection(BaseConnectionInformation ci, Correlator correlator, Channel channel, EventLoopGroup workerGroup)
    {
        this.ci = ci;
        this.correlator = correlator;
        this.channel = channel;
        this.workerGroup = workerGroup;
    }

    public static NetworkConnection of(final NettyConnectionInformation ci, final NetworkListener networkListener)
    {
        Objects.requireNonNull(ci, "connection info can not be null");
        Objects.requireNonNull(ci, "network listener can not be null");
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Correlator correlator = ci.getCorrelator();
        Channel ch = init(ci.getAddress(), workerGroup, ci.getChannelClass(), CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator), ci.isLogHandlerEnabled());
        NettyNetworkConnection c = new NettyNetworkConnection(ci, correlator, ch, workerGroup);
        ch.closeFuture().addListener(f -> handleClose(c, networkListener));
        c.throwIfProtocolVersionNotSupportedByEIS(ci.getProtocolVersion(), ci.getDomainId(), ci.getDomainName());
        return c;
    }

    private static Channel init(final InetSocketAddress address, final EventLoopGroup workerGroup, Class<? extends Channel> channelClass, final CasualMessageHandler messageHandler, ExceptionHandler exceptionHandler, boolean enableLogHandler)
    {
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
                        ch.pipeline().addFirst(LOG_HANDLER_NAME, new LoggingHandler(LogLevel.INFO));
                        LOG.info(() -> "outbound network log handler enabled");
                    }
                }
            });
        return b.connect(address).syncUninterruptibly().channel();
    }

    private static void handleClose(final NettyNetworkConnection c, NetworkListener networkListener)
    {
        // always complete any outstanding requests exceptionally
        // both when the casual domain goes away or when the owner of the network connection
        // closes us, the client, directly
        c.correlator.completeAllExceptionally(new CasualConnectionException("network connection is gone"));
        if(c.connected.get())
        {
            // only inform on casual disconnect
            networkListener.disconnected();
        }
    }

    @Override
    public <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message)
    {
        CompletableFuture<CasualNWMessage<T>> f = new CompletableFuture<>();
        if(!channel.isActive())
        {
            LOG.finest("channel not active, connection gone");
            f.completeExceptionally(new CasualConnectionException("can not write msg: " + message + " connection is gone"));
            return f;
        }
        correlator.put(message.getCorrelationId(), f);
        ChannelFuture cf = channel.writeAndFlush(message);
        //this handles any exceptional behaviour when writing
        cf.addListener(v -> {
            if(!v.isSuccess()){
                List<UUID> l = new ArrayList<>();
                l.add(message.getCorrelationId());
                correlator.completeExceptionally(l, new CasualConnectionException(cf.cause()));
            }// successful correlation is done in CasualMessageHandler
        });
        return f;
    }

    @Override
    public void close()
    {
        connected.set(false);
        // all channels are closed when the event loop group shuts down
        workerGroup.shutdownGracefully().addListener(futureListener -> LOG.info("network connection " + this + " gracefully closed"));
    }

    @Override
    public boolean isActive()
    {
        return channel.isActive();
    }

    private void throwIfProtocolVersionNotSupportedByEIS(long version, final UUID domainId, final String domainName)
    {
        CasualDomainConnectRequestMessage requestMessage = CasualDomainConnectRequestMessage.createBuilder()
                                                                                            .withExecution(UUID.randomUUID())
                                                                                            .withDomainId(domainId)
                                                                                            .withDomainName(domainName)
                                                                                            .withProtocols(Arrays.asList(version))
                                                                                            .build();
        CasualNWMessage<CasualDomainConnectRequestMessage> nwMessage = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage);
        CompletableFuture<CasualNWMessage<CasualDomainConnectReplyMessage>> replyEnvelopeFuture = request(nwMessage);
        CasualNWMessage<CasualDomainConnectReplyMessage> replyEnvelope = replyEnvelopeFuture.join();
        if(replyEnvelope.getMessage().getProtocolVersion() != version)
        {
            throw new CasualConnectionException("wanted protocol version " + version + " is not supported by casual.\n Casual suggested protocol version " + replyEnvelope.getMessage().getProtocolVersion());
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("NettyNetworkConnection{");
        sb.append("ci=").append(ci);
        sb.append(", correlator=").append(correlator);
        sb.append(", channel=").append(channel);
        sb.append(", workerGroup=").append(workerGroup);
        sb.append('}');
        return sb.toString();
    }
}
