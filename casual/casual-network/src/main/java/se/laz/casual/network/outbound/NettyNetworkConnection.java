/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
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
import io.netty.handler.logging.LoggingHandler;
import se.laz.casual.api.conversation.ConversationClose;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.ConnectionObserver;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.CasualNWMessageDecoder;
import se.laz.casual.network.CasualNWMessageEncoder;
import se.laz.casual.network.EventLoopClient;
import se.laz.casual.network.EventLoopFactory;
import se.laz.casual.network.LogLevelProvider;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.connection.DomainDisconnectedException;
import se.laz.casual.network.inbound.ProtocolVersionValueHolder;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.conversation.Request;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.DomainDiscoveryTopologyUpdateMessage;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class NettyNetworkConnection implements NetworkConnection, ConversationClose, CasualOutboundMessageListener
{
    private static final Logger LOG = Logger.getLogger(NettyNetworkConnection.class.getName());
    private static final String LOG_HANDLER_NAME = "logHandler";
    private static final String NETWORK_CONNECTION_GONE = "network connection is gone";
    private final BaseConnectionInformation ci;
    private final Correlator correlator;
    private final ConversationMessageStorage conversationMessageStorage;
    private final Channel channel;
    private final AtomicBoolean connected = new AtomicBoolean(true);
    private final Supplier<ExecutorService> executorServiceSupplier;
    private final Supplier<ProtocolVersion> protocolVersionSupplier;
    private final DomainIdValueHolder domainIdValueHolder;
    private final ErrorInformer errorInformer;
    private final DomainDisconnectHandler domainDisconnectHandler;
    private final ConnectionObserverHandler connectionObserverHandler = ConnectionObserverHandler.of();

    // 8 > 7 parameters used
    @SuppressWarnings("java:S107")
    private NettyNetworkConnection(BaseConnectionInformation ci,
                                   Correlator correlator,
                                   Channel channel,
                                   ConversationMessageStorage conversationMessageStorage,
                                   Supplier<ExecutorService> executorServiceSupplier,
                                   Supplier<ProtocolVersion> protocolVersionSupplier,
                                   DomainIdValueHolder domainIdValueHolder,
                                   ErrorInformer errorInformer)
    {
        this.ci = ci;
        this.correlator = correlator;
        this.channel = channel;
        this.conversationMessageStorage = conversationMessageStorage;
        this.executorServiceSupplier = executorServiceSupplier;
        this.protocolVersionSupplier = Objects.requireNonNull(protocolVersionSupplier, "protocolVersionSupplier can not be null");
        this.domainIdValueHolder = Objects.requireNonNull(domainIdValueHolder, "domainIdValueHolder can not be null");
        this.domainDisconnectHandler = DomainDisconnectHandler.of(channel, domainIdValueHolder);
        this.errorInformer = errorInformer;
    }

    public static NettyNetworkConnection of(final NettyConnectionInformation ci, final NetworkListener networkListener)
    {
        Objects.requireNonNull(ci, "connection info can not be null");
        Objects.requireNonNull(networkListener, "network listener can not be null");
        ErrorInformer errorInformer = ErrorInformer.of(new CasualConnectionException(NETWORK_CONNECTION_GONE));
        errorInformer.addListener(networkListener);
        EventLoopGroup workerGroup = EventLoopFactory.getInstance(EventLoopClient.OUTBOUND);
        Correlator correlator = ci.getCorrelator();
        ConversationMessageStorage conversationMessageStorage = ConversationMessageStorageImpl.of();
        OnNetworkError onNetworkError = channel -> NetworkErrorHandler.notifyListenersIfNotConnected(channel, errorInformer);
        ConversationMessageHandler conversationMessageHandler = ConversationMessageHandler.of( conversationMessageStorage);
        CasualMessageHandler messageHandler = CasualMessageHandler.of(correlator);
        ProtocolVersionValueHolder protocolVersionValueHolder =  ProtocolVersionValueHolder.of();
        DomainIdValueHolder domainIdValueHolder = DomainIdValueHolder.of();
        Channel ch = init(ci, workerGroup, messageHandler, conversationMessageHandler, ExceptionHandler.of(correlator, onNetworkError), protocolVersionValueHolder, domainIdValueHolder);
        return handshake(ci, ch, correlator, conversationMessageStorage, messageHandler, protocolVersionValueHolder::get, domainIdValueHolder, errorInformer);
    }

    /**
     * Creates a connection from an established channel and performs the outbound domain-connect handshake.
     *
     * <p>For reverse outbound, the EIS connects and waits for this outbound side to issue the
     * domain-connect request. This method blocks while awaiting the handshake reply. Do not call
     * it from the channel's event-loop thread.
     *
     * @param channel the established channel to use
     * @param ci the outbound connection information
     * @param networkListener the listener to notify if the network connection closes unexpectedly
     * @return the connected network connection after a successful handshake
     * @throws NullPointerException if any argument is {@code null}
     */
    public static NettyNetworkConnection ofAcceptedChannel(final Channel channel, final NettyConnectionInformation ci, final NetworkListener networkListener)
    {
        Objects.requireNonNull(channel, "channel can not be null");
        Objects.requireNonNull(ci, "connection info can not be null");
        Objects.requireNonNull(networkListener, "network listener can not be null");
        ErrorInformer errorInformer = ErrorInformer.of(new CasualConnectionException(NETWORK_CONNECTION_GONE));
        errorInformer.addListener(networkListener);
        Correlator correlator = ci.getCorrelator();
        ConversationMessageStorage conversationMessageStorage = ConversationMessageStorageImpl.of();
        OnNetworkError onNetworkError = ch -> NetworkErrorHandler.notifyListenersIfNotConnected(ch, errorInformer);
        ConversationMessageHandler conversationMessageHandler = ConversationMessageHandler.of(conversationMessageStorage);
        CasualMessageHandler messageHandler = CasualMessageHandler.of(correlator);
        ProtocolVersionValueHolder protocolVersionValueHolder = ProtocolVersionValueHolder.of();
        DomainIdValueHolder domainIdValueHolder = DomainIdValueHolder.of();
        setupPipeline(channel, ci, messageHandler, conversationMessageHandler, ExceptionHandler.of(correlator, onNetworkError), protocolVersionValueHolder, domainIdValueHolder);
        return handshake(ci, channel, correlator, conversationMessageStorage, messageHandler, protocolVersionValueHolder::get, domainIdValueHolder, errorInformer);
    }

    // 8 > 7 parameters used
    @SuppressWarnings("java:S107")
    private static NettyNetworkConnection handshake(NettyConnectionInformation ci, Channel ch, Correlator correlator, ConversationMessageStorage conversationMessageStorage,
                                                    CasualMessageHandler messageHandler, Supplier<ProtocolVersion> protocolVersionSupplier,
                                                    DomainIdValueHolder domainIdValueHolder, ErrorInformer errorInformer)
    {
        NettyNetworkConnection networkConnection = new NettyNetworkConnection(ci, correlator, ch, conversationMessageStorage,
                JEEConcurrencyFactory::getManagedExecutorService, protocolVersionSupplier, domainIdValueHolder, errorInformer);
        ch.closeFuture().addListener(f -> handleClose(networkConnection, errorInformer));
        messageHandler.setMessageListener(networkConnection);
        boolean handshakeCompleted = false;
        try
        {
            // Note:
            // Protocol version and domain id are set in the decoder ( using the domain connect reply).
            // This guarantees that those are always set and visible to all threads with the correct information.
            // Unsolicited messages, such as topology changed or domain disconnect, can only ever be decoded after the domain connect reply has been decoded.
            networkConnection.requestDomainConnect(ci.getDomainId(), ci.getDomainName());
            LOG.info(() -> "using protocol version: " + networkConnection.getProtocolVersion() + " asked for: " + ProtocolVersion.supportedVersions());
            LOG.finest(() -> networkConnection + " outbound connected to: " + new InetSocketAddress(ci.getAddress().getHostName(), ci.getAddress().getPort()) + " with domainId: " + networkConnection.getDomainId());
            handshakeCompleted = true;
            return networkConnection;
        }
        finally
        {
            if(!handshakeCompleted)
            {
                networkConnection.close();
            }
        }
    }

    @Override
    public ProtocolVersion getProtocolVersion()
    {
        return protocolVersionSupplier.get();
    }

    private static Channel init(NettyConnectionInformation ci, final EventLoopGroup workerGroup, final CasualMessageHandler messageHandler, ConversationMessageHandler conversationMessageHandler, ExceptionHandler exceptionHandler, ProtocolVersionValueHolder protocolVersionValueHolder, DomainIdValueHolder domainIdValueHolder)
    {
        Bootstrap b = new Bootstrap()
            .group(workerGroup)
            .channel(ci.getChannelClass())
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                protected void initChannel(SocketChannel ch)
                {
                    setupPipeline(ch, ci, messageHandler, conversationMessageHandler, exceptionHandler, protocolVersionValueHolder, domainIdValueHolder);
                }
            });
        LOG.finest(() -> "about to connect to: " + ci.getAddress());
        return b.connect(ci.getAddress()).syncUninterruptibly().channel();
    }

    // Both normal and reverse outbound use the same decoder. The decoder stores the negotiated protocol
    // version and peer domain ID while decoding the connect reply, before it can decode a subsequent message.
    private static void setupPipeline(Channel ch, NettyConnectionInformation ci, final CasualMessageHandler messageHandler, ConversationMessageHandler conversationMessageHandler, ExceptionHandler exceptionHandler, ProtocolVersionValueHolder protocolVersionValueHolder, DomainIdValueHolder domainIdValueHolder)
    {
        ch.pipeline().addLast(CasualNWMessageDecoder.of(protocolVersionValueHolder, domainIdValueHolder), CasualNWMessageEncoder.of(), messageHandler, conversationMessageHandler, exceptionHandler);
        if(ci.isLogHandlerEnabled())
        {
            ch.pipeline().addFirst(LOG_HANDLER_NAME, new LoggingHandler(LogLevelProvider.OUTBOUND_LOGGING_LEVEL));
            LOG.info(() -> "outbound network log handler enabled, using netty logging level: " + LogLevelProvider.OUTBOUND_LOGGING_LEVEL);
        }
    }

    @Override
    public boolean isDomainDisconnecting()
    {
        return protocolSupportsDomainDisconnect() && domainDisconnectHandler.hasDomainBeenDisconnected();
    }

    private static void handleClose(final NettyNetworkConnection connection, ErrorInformer errorInformer)
    {
        // always complete any outstanding requests exceptionally
        // both when the casual domain goes away or when the owner of the network connection
        // closes us, the client, directly
        connection.correlator.completeAllExceptionally(new CasualConnectionException(NETWORK_CONNECTION_GONE));
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
        Optional<CompletableFuture<CasualNWMessage<T>>> value = issueRequest(message, false);
        return value.orElseThrow(() -> new CasualConnectionException("missing future in reply - this should never happen!"));
    }

    @Override
    public <X extends CasualNetworkTransmittable> void requestNoReply(CasualNWMessage<X> message)
    {
        issueRequest(message, true).ifPresent(casualNWMessageCompletableFuture -> casualNWMessageCompletableFuture.whenComplete((v, e) -> {
            if (null != e) {
                LOG.warning("requestNoReply: " + message + " error: " + e);
                return;
            }
            LOG.warning("requestNoReply: " + message + " got reply: " + v);
        }));
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
                future.completeExceptionally(new CasualConnectionException("NettyNetworkConnection::send failed\nmsg: " + message, v.cause()));
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
            executorServiceSupplier.get().execute(() -> future.complete(conversationMessageStorage.takeFirst(corrid)));
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

    private <X extends CasualNetworkTransmittable> void preRequest(CasualNWMessage<X> message)
    {
        // The domain-connect request is sent before the decoder receives the negotiated protocol version.
        if(message.getType() != CasualNWMessageType.DOMAIN_CONNECT_REQUEST
           && hasDomainBeenDisconnectedAndRequestIsNotXAMessage(message))
        {
            // new service calls are not ok when domain has been disconnected
            throw new DomainDisconnectedException("Domain: " + getDomainId() + " has disconnected, only XA calls allowed");
        }
        LOG.finest(() -> String.format("request: %s", LogTool.asLogEntry(message)) + "\n using " + this);
    }

    private <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> Optional<CompletableFuture<CasualNWMessage<T>>> issueRequest(CasualNWMessage<X> message, boolean noReply)
    {
        preRequest(message);
        CompletableFuture<CasualNWMessage<T>> f = new CompletableFuture<>();
        if(!channel.isActive())
        {
            LOG.finest("channel not active, connection gone");
            f.completeExceptionally(new CasualConnectionException("can not write msg: " + message + " connection is gone"));
            return noReply ? Optional.empty() : Optional.of(f);
        }
        if(!noReply)
        {
            correlator.put(message.getCorrelationId(), f);
        }
        ChannelFuture cf = channel.writeAndFlush(message);
        //this handles any exceptional behaviour when writing
        cf.addListener(v -> {
            if(!v.isSuccess()){
                List<UUID> l = new ArrayList<>();
                l.add(message.getCorrelationId());
                LOG.finest(() -> String.format("failed request: %s", LogTool.asLogEntry(message)));
                // This since all outstanding requests may already have been completed exceptionally
                // when no reply - nobody is listening Dave
                if(!f.isCompletedExceptionally() && !noReply) {
                    correlator.completeExceptionally(l, new CasualConnectionException(cf.cause()));
                }
            }// successful correlation is done in CasualMessageHandler
        });
        return noReply ? Optional.empty() : Optional.of(f);
    }

    private <T extends CasualNetworkTransmittable> boolean hasDomainBeenDisconnectedAndRequestIsNotXAMessage(CasualNWMessage<T> message)
    {
        if(!protocolSupportsDomainDisconnect())
        {
            return false;
        }
        return domainDisconnectHandler.hasDomainBeenDisconnected() &&
                !(message.getType() == CasualNWMessageType.PREPARE_REQUEST ||
                        message.getType() == CasualNWMessageType.COMMIT_REQUEST ||
                        message.getType() == CasualNWMessageType.REQUEST_ROLLBACK);
    }

    @Override
    public boolean isActive()
    {
        return channel.isActive();
    }

    @Override
    public DomainId getDomainId()
    {
        return domainIdValueHolder.get();
    }

    @Override
    public void addConnectionObserver(ConnectionObserver observer)
    {
        if(protocolSupportsDomainDisconnect())
        {
            connectionObserverHandler.addConnectionObserver(observer);
        }
    }

    private boolean protocolSupportsDomainDisconnect()
    {
        return getProtocolVersion().supportsDomainDisconnect();
    }

    private boolean protocolSupportsDomainTopologyChange()
    {
        return getProtocolVersion().supportsDomainTopologyChange();
    }

    private void requestDomainConnect(final UUID domainId, final String domainName)
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
        replyEnvelopeFuture.join();
        LOG.finest(() -> "received handshake reply: " + this);
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
                ", domainId=" + domainIdValueHolder +
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
        return switch(type)
        {
            case DOMAIN_DISCONNECT_REQUEST -> protocolSupportsDomainDisconnect();
            case DOMAIN_DISCOVERY_TOPOLOGY_UPDATE -> protocolSupportsDomainTopologyChange();
            default -> false;
        };
    }

    @Override
    public <T extends CasualNetworkTransmittable> void handleMessage(CasualNWMessage<T> message)
    {
        LOG.finest(() -> "message: " + LogTool.asLogEntry(message));
        final T msg = message.getMessage();
        if(msg instanceof DomainDisconnectRequestMessage requestMessage)
        {
            try
            {
                domainDisconnectHandler.domainDisconnected(DomainDisconnectReplyInfo.of(message.getCorrelationId(), requestMessage.getExecution()));
            }
            finally
            {
                connectionObserverHandler.notifyDomainDisconnected(getDomainId());
            }
        }
        else if(msg instanceof DomainDiscoveryTopologyUpdateMessage)
        {
            // note, we do not care about the data just that we got the actual message
            connectionObserverHandler.notifyTopologyChanged(getDomainId());
        }
        else
        {
            LOG.warning(() -> "message type: " + message.getType() + " not handled!");
        }

    }
}
