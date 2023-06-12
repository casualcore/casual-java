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
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import se.laz.casual.api.conversation.ConversationClose;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.CasualNWMessageDecoder;
import se.laz.casual.network.CasualNWMessageEncoder;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.connection.DomainDisconnectedException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.conversation.Request;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectRequestMessage;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class NettyNetworkConnection implements NetworkConnection, ConversationClose, CasualOutboundMessageListener
{
    private static final Logger LOG = Logger.getLogger(NettyNetworkConnection.class.getName());
    private static final String LOG_HANDLER_NAME = "logHandler";
    private final BaseConnectionInformation ci;
    private final Correlator correlator;
    private final ConversationMessageStorage conversationMessageStorage;
    private final Channel channel;
    private final AtomicBoolean connected = new AtomicBoolean(true);
    private final Supplier<ManagedExecutorService> managedExecutorService;
    private final ErrorInformer errorInformer;
    private DomainId domainId;
    private ProtocolVersion protocolVersion;
    private DomainDisconnectHandler domainDisconnectHandler;

    private NettyNetworkConnection(BaseConnectionInformation ci,
                                   Correlator correlator,
                                   Channel channel,
                                   ConversationMessageStorage conversationMessageStorage,
                                   Supplier<ManagedExecutorService> managedExecutorService,
                                   ErrorInformer errorInformer)
    {
        this.ci = ci;
        this.correlator = correlator;
        this.channel = channel;
        this.conversationMessageStorage = conversationMessageStorage;
        this.managedExecutorService = managedExecutorService;
        this.errorInformer = errorInformer;
    }

    public static NetworkConnection of(final NettyConnectionInformation ci, final NetworkListener networkListener)
    {
        Objects.requireNonNull(ci, "connection info can not be null");
        Objects.requireNonNull(ci, "network listener can not be null");
        ErrorInformer errorInformer = ErrorInformer.of(new CasualConnectionException("network connection is gone"));
        errorInformer.addListener(networkListener);
        EventLoopGroup workerGroup = EventLoopFactory.getInstance();
        Correlator correlator = ci.getCorrelator();
        ConversationMessageStorage conversationMessageStorage = ConversationMessageStorageImpl.of();
        OnNetworkError onNetworkError = channel -> NetworkErrorHandler.notifyListenersIfNotConnected(channel, errorInformer);
        ConversationMessageHandler conversationMessageHandler = ConversationMessageHandler.of( conversationMessageStorage);
        CasualMessageHandler messageHandler = CasualMessageHandler.of(correlator);
        Channel ch = init(ci.getAddress(), workerGroup, ci.getChannelClass(), messageHandler, conversationMessageHandler, ExceptionHandler.of(correlator, onNetworkError), ci.isLogHandlerEnabled());
        NettyNetworkConnection networkConnection = new NettyNetworkConnection(ci, correlator, ch, conversationMessageStorage, JEEConcurrencyFactory::getManagedExecutorService, errorInformer);
        LOG.finest(() -> networkConnection + " connected to: " + ci.getAddress());
        ch.closeFuture().addListener(f -> handleClose(networkConnection, errorInformer));
        DomainId id = networkConnection.throwIfProtocolVersionNotSupportedByEIS(ci.getDomainId(), ci.getDomainName());
        networkConnection.setDomainId(id);
        if(networkConnection.getProtocolVersion() == ProtocolVersion.VERSION_1_1 || networkConnection.getProtocolVersion() == ProtocolVersion.VERSION_1_2)
        {
            // domain disconnect only available in 1.1, 1.2
            messageHandler.setMessageListener(networkConnection);
            networkConnection.setConnectionHandler(DomainDisconnectHandler.of(networkConnection.channel, networkConnection.getDomainId()));
        }
        return networkConnection;
    }

    public ProtocolVersion getProtocolVersion()
    {
        return protocolVersion;
    }

    private void setProtocolVersion(ProtocolVersion protocolVersion)
    {
        this.protocolVersion = protocolVersion;
    }

    private static Channel init(final InetSocketAddress address, final EventLoopGroup workerGroup, Class<? extends Channel> channelClass, final CasualMessageHandler messageHandler, ConversationMessageHandler conversationMessageHandler, ExceptionHandler exceptionHandler, boolean enableLogHandler)
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
                    ch.pipeline().addLast(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), messageHandler, conversationMessageHandler, exceptionHandler);
                    if(enableLogHandler)
                    {
                        ch.pipeline().addFirst(LOG_HANDLER_NAME, new LoggingHandler(LogLevel.INFO));
                        LOG.info(() -> "outbound network log handler enabled");
                    }
                }
            });
        LOG.finest(() -> "about to connect to: " + address);
        return b.connect(address).syncUninterruptibly().channel();
    }

    private void setConnectionHandler(DomainDisconnectHandler domainDisconnectHandler)
    {
        this.domainDisconnectHandler = domainDisconnectHandler;
    }

    private static void handleClose(final NettyNetworkConnection connection, ErrorInformer errorInformer)
    {
        // always complete any outstanding requests exceptionally
        // both when the casual domain goes away or when the owner of the network connection
        // closes us, the client, directly
        connection.correlator.completeAllExceptionally(new CasualConnectionException("network connection is gone"));
        if(connection.connected.get())
        {
            // only inform on casual disconnect
            // will result in a close call on the ManagedConnection ( by the application server)
            errorInformer.inform();
        }
    }

    @Override
    public <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message)
    {
        if(hasDomainBeenDisconnectedAndRequestIsServiceOrQueueCall(message))
        {
            // new service calls are not ok when domain has been disconnected
            throw new DomainDisconnectedException("Domain: " + domainId + " has disconnected, no service or queue calls allowed");
        }
        LOG.finest(() -> String.format("request: %s", LogTool.asLogEntry(message)) + "\n using " + this);
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
                LOG.finest(() -> String.format("failed request: %s", LogTool.asLogEntry(message)));
                // This since all outstanding requests may already have been completed exceptionally
                if(!f.isCompletedExceptionally())
                {
                    correlator.completeExceptionally(l, new CasualConnectionException(cf.cause()));
                }
            }// successful correlation is done in CasualMessageHandler
        });
        return f;
    }

    @Override
    public <X extends CasualNetworkTransmittable> void send(CasualNWMessage<X> message)
    {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        ChannelFuture cf = channel.writeAndFlush(message);
        //this handles any exceptional behaviour when writing
        cf.addListener(v -> {
            if(!v.isSuccess())
            {
                future.completeExceptionally(new CasualConnectionException("NetttyNetworkConnection::send failed\nmsg: " + message, v.cause()));
            }
            else
            {
                future.complete(true);
            }
        });
        future.join();
    }

    @Override
    public CompletableFuture<CasualNWMessage<Request>> receive(UUID corrid)
    {
        CompletableFuture<CasualNWMessage<Request>> future = new CompletableFuture<>();
        Optional<CasualNWMessage<Request>> maybeMessage = conversationMessageStorage.nextMessage(corrid);
        maybeMessage.ifPresent(future::complete);
        if(!future.isDone())
        {
            managedExecutorService.get().execute(() -> future.complete(conversationMessageStorage.takeFirst(corrid)));
        }
        return future;
    }

    @Override
    public void close()
    {
        connected.set(false);
        LOG.finest(() -> this + " network connection close called by appserver, closing");
        channel.close();
    }

    private <T extends CasualNetworkTransmittable> boolean hasDomainBeenDisconnectedAndRequestIsServiceOrQueueCall(CasualNWMessage<T> message)
    {
        return isProtocolVersionOneOneOrOneTwo() && domainDisconnectHandler.hasDomainBeenDisconnected() &&
                (message.getType() == CasualNWMessageType.SERVICE_CALL_REQUEST ||
                        message.getType() == CasualNWMessageType.DEQUEUE_REQUEST ||
                        message.getType() == CasualNWMessageType.ENQUEUE_REQUEST);
    }

    @Override
    public boolean isActive()
    {
        return channel.isActive();
    }

    @Override
    public DomainId getDomainId()
    {
        return domainId;
    }

    private void setDomainId(DomainId domainId)
    {
        this.domainId = domainId;
    }

    private boolean isProtocolVersionOneOneOrOneTwo()
    {
        return protocolVersion == ProtocolVersion.VERSION_1_1 || protocolVersion == ProtocolVersion.VERSION_1_2;
    }

    private DomainId throwIfProtocolVersionNotSupportedByEIS(final UUID domainId, final String domainName)
    {
        CasualDomainConnectRequestMessage requestMessage = CasualDomainConnectRequestMessage.createBuilder()
                                                                                            .withExecution(UUID.randomUUID())
                                                                                            .withDomainId(domainId)
                                                                                            .withDomainName(domainName)
                                                                                            .withProtocols(ProtocolVersion.supportedVersionNumbers())
                                                                                            .build();
        CasualNWMessage<CasualDomainConnectRequestMessage> nwMessage = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage);
        LOG.finest(() -> "about to send handshake: " + this);
        CompletableFuture<CasualNWMessage<CasualDomainConnectReplyMessage>> replyEnvelopeFuture = request(nwMessage);
        LOG.finest(() -> "handshake sent: " + this);
        CasualNWMessage<CasualDomainConnectReplyMessage> replyEnvelope = replyEnvelopeFuture.join();
        LOG.finest(() -> "received handshake reply: " + this);
        long actualProtocolVersion = ProtocolVersion.supportedVersionNumbers()
                                                              .stream()
                                                              .filter(version -> version == replyEnvelope.getMessage().getProtocolVersion())
                                                              .findFirst()
                                                              .orElseThrow(() -> new CasualConnectionException("wanted one of protocol versions " + ProtocolVersion.supportedVersionNumbers() + " but it is not supported by casual.\n Casual suggested protocol version " + replyEnvelope.getMessage().getProtocolVersion()));
        setProtocolVersion(ProtocolVersion.unmarshall(actualProtocolVersion));
        LOG.info(() -> "using protocol version: " + protocolVersion + " asked for: " + ProtocolVersion.supportedVersions());
        return DomainId.of(replyEnvelope.getMessage().getDomainId());
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
        NettyNetworkConnection that = (NettyNetworkConnection) o;
        return Objects.equals(channel, that.channel) && Objects.equals(getDomainId(), that.getDomainId());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(channel, getDomainId());
    }

    @Override
    public String toString()
    {
        return "NettyNetworkConnection{" +
                "ci=" + ci +
                "correlator=" + correlator +
                ", channel=" + channel +
                ", domainId=" + domainId +
                '}';
    }

    @Override
    public ConversationClose getConversationClose()
    {
        return this::close;
    }

    @Override
    public void close(UUID conversationalCorrId)
    {
        ConversationMessageStorageImpl.remove(conversationalCorrId);
    }

    public void addListener(NetworkListener listener)
    {
        errorInformer.addListener(listener);
    }

    @Override
    public boolean isInterestedIn(CasualNWMessageType type)
    {
        return isProtocolVersionOneOneOrOneTwo() && type == CasualNWMessageType.DOMAIN_DISCONNECT_REQUEST;
    }

    @Override
    public <T extends CasualNetworkTransmittable> void handleMessage(CasualNWMessage<T> message)
    {
        LOG.finest(() -> "message: " + LogTool.asLogEntry(message));
        if(message.getMessage() instanceof DomainDisconnectRequestMessage)
        {
            DomainDisconnectRequestMessage requestMessage = (DomainDisconnectRequestMessage) message.getMessage();
            domainDisconnectHandler.domainDisconnected(DomainDisconnectReplyInfo.of(message.getCorrelationId(), requestMessage.getExecution()));
        }
        else
        {
            LOG.warning(() -> "message type: " + message.getType() + " not handled!");
        }

    }

}
