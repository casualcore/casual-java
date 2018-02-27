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
import io.netty.util.concurrent.Future;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.internal.jca.ManagedConnectionInvalidator;
import se.kodarkatten.casual.internal.network.NetworkConnection;
import se.kodarkatten.casual.network.protocol.connection.CasualConnectionException;
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.CasualNWMessageDecoder;
import se.laz.casual.network.CasualNWMessageEncoder;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NettyNetworkConnection implements NetworkConnection
{
    private static final String USE_LOG_HANDLER_PROPERTY_NAME = "casual.network.outbound.enableLoghandler";
    private final BaseConnectionInformation ci;
    private final ManagedConnectionInvalidator invalidator;
    private final Correlator correlator;
    private final Channel channel;
    private final EventLoopGroup workerGroup;
    private final AtomicBoolean handleClose = new AtomicBoolean(true);

    private NettyNetworkConnection(BaseConnectionInformation ci, ManagedConnectionInvalidator invalidator, Correlator correlator, Channel channel, EventLoopGroup workerGroup)
    {
        this.ci = ci;
        this.invalidator = invalidator;
        this.correlator = correlator;
        this.channel = channel;
        this.workerGroup = workerGroup;
    }

    public static NetworkConnection of(final NettyConnectionInformation ci)
    {
        Objects.requireNonNull(ci, "connection info can not be null");
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Correlator correlator = ci.getCorrelator();
        Channel ch = init(ci.getAddress(), workerGroup, ci.getChannelClass(), CasualMessageHandler.of(correlator), ExceptionHandler.of(ci.getInvalidator(), correlator));
        NettyNetworkConnection c = new NettyNetworkConnection(ci, ci.getInvalidator(), correlator, ch, workerGroup);
        ch.closeFuture().addListener(f -> handleClose(f, c));
        c.throwIfProtocolVersionNotSupportedByEIS(ci.getProtocolVersion(), ci.getDomainId(), ci.getDomainName());
        return c;
    }

    private static Channel init(final InetSocketAddress address, final EventLoopGroup workerGroup, Class<? extends Channel> channelClass, final CasualMessageHandler messageHandler, ExceptionHandler exceptionHandler)
    {
        boolean enableLogHandler = Boolean.parseBoolean(System.getProperty(USE_LOG_HANDLER_PROPERTY_NAME, null));
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
                        ch.pipeline().addFirst("logHandler", new LoggingHandler(LogLevel.INFO));
                    }
                }
            });
        return b.connect(address).syncUninterruptibly().channel();
    }

    private static void handleClose(final Future<? super Void> f, final NettyNetworkConnection c)
    {
        if(c.handleClose.get())
        {
            c.correlator.completeAllExceptionally(new CasualConnectionException("network connection is gone"));
            c.invalidator.invalidate(new CasualConnectionException("connection is gone"));
        }
    }

    @Override
    public <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message)
    {
        CompletableFuture<CasualNWMessage<T>> f = new CompletableFuture<>();
        if(!channel.isActive())
        {
            f.completeExceptionally(new CasualConnectionException("can not write msg: " + message + " connection is gone"));
            correlator.completeAllExceptionally(new CasualConnectionException("network connection is gone"));
            invalidator.invalidate(new CasualConnectionException("connection is gone"));
            return f;
        }
        correlator.put(message.getCorrelationId(), f);
        ChannelFuture cf = channel.writeAndFlush(message);
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
        handleClose.set(false);
        if(channel.isOpen())
        {
            channel.close();
        }
        channel.closeFuture().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
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
        try
        {
            CasualNWMessage<CasualDomainConnectReplyMessage> replyEnvelope = replyEnvelopeFuture.get();
            if(replyEnvelope.getMessage().getProtocolVersion() != version)
            {
                throw new CasualConnectionException("wanted protocol version " + version + " is not supported by casual.\n Casual suggested protocol version " + replyEnvelope.getMessage().getProtocolVersion());
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("NettyNetworkConnection{");
        sb.append("ci=").append(ci);
        sb.append(", invalidator=").append(invalidator);
        sb.append(", correlator=").append(correlator);
        sb.append(", channel=").append(channel);
        sb.append(", workerGroup=").append(workerGroup);
        sb.append('}');
        return sb.toString();
    }
}
