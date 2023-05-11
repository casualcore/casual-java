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
import se.laz.casual.network.connection.DomainDisconnectingException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.conversation.Request;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectReplyMessage;

import javax.enterprise.concurrent.ManagedExecutorService;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class NettyNetworkConnection implements NetworkConnection, ConversationClose, DomainDisconnectListener, CorrelatorEmptyListener
{
    private static final Logger LOG = Logger.getLogger(NettyNetworkConnection.class.getName());
    private static final Map<NettyNetworkConnection, UUID> disconnectedDomains = new ConcurrentHashMap<>();
    private static final String LOG_HANDLER_NAME = "logHandler";
    private final TransactionInformation transactionInformation = TransactionInformation.of();
    private final BaseConnectionInformation ci;
    private final Correlator correlator;
    private final ConversationMessageStorage conversationMessageStorage;
    private final Channel channel;
    private final AtomicBoolean connected = new AtomicBoolean(true);
    private final AtomicBoolean domainDisconnected = new AtomicBoolean(false);
    private final AtomicBoolean domainDisconnectedHandled = new AtomicBoolean(false);
    private final Supplier<ManagedExecutorService> managedExecutorService;
    private final ErrorInformer errorInformer;
    private DomainId domainId;

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
        if(ci.getProtocolVersion() == ProtocolVersion.VERSION_1_1.getVersion() || ci.getProtocolVersion() == ProtocolVersion.VERSION_1_2.getVersion())
        {
            // domain disconnect only available in 1.1, 1.2
            messageHandler.setDomainDisconnectListener(networkConnection);
            correlator.setCorrelatorEmptyListener(networkConnection);
        }
        LOG.finest(() -> networkConnection + " connected to: " + ci.getAddress());
        ch.closeFuture().addListener(f -> handleClose(networkConnection, errorInformer));
        DomainId id = networkConnection.throwIfProtocolVersionNotSupportedByEIS(ci.getProtocolVersion(), ci.getDomainId(), ci.getDomainName());
        networkConnection.setDomainId(id);
        return networkConnection;
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
        LOG.finest(() -> String.format("request: %s", LogTool.asLogEntry(message)) + "\n using " + this);
        if(isProtocolVersionOneOneOrOneTwo() && domainDisconnected.get() && message.getType() == CasualNWMessageType.SERVICE_CALL_REQUEST)
        {

            throw new DomainDisconnectingException("domain: " + domainId + " is disconnecting, sending new service calls is not allowed");
        }
        CompletableFuture<CasualNWMessage<T>> f = new CompletableFuture<>();
        if(!channel.isActive())
        {
            LOG.finest("channel not active, connection gone");
            f.completeExceptionally(new CasualConnectionException("can not write msg: " + message + " connection is gone"));
            return f;
        }
        correlator.put(message.getCorrelationId(), f);
        if(isProtocolVersionOneOneOrOneTwo())
        {
            transactionInformation.pruneTransactions();
            transactionInformation.addCurrentTransaction();
        }
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

    private void sendDomainDisconnectReply()
    {
        DomainDisconnectReplyMessage replyMessage = DomainDisconnectReplyMessage.of(disconnectedDomains.get(this));
        try
        {
            channel.writeAndFlush(CasualNWMessageImpl.of(UUID.randomUUID(), replyMessage));
        }
        catch(Exception e)
        {
            // if we did not manage to send the domain disconnect message it is due to the connection being gone
            // which is fine
            LOG.info(() -> "Could not send domain disconnect reply: " + e);
        }
    }

    @Override
    public <X extends CasualNetworkTransmittable> void send(CasualNWMessage<X> message)
    {
        if(domainDisconnected.get())
        {
            throw new DomainDisconnectingException("domain: " + domainId + " is disconnecting, sending messages is not allowed");
        }
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
        if(domainDisconnectedButThereArePendingMessages())
        {
            LOG.finest(()-> "network connection close called by appserver but domain disconnected, appserver is not allowed to close connection for: " + this);
            return;
        }
        if(domainDisconnected.get())
        {
            sendDomainDisconnectReply();
            disconnectedDomains.remove(this);
            transactionInformation.clear();
            logDisconnectedDomainInfo();
        }
        connected.set(false);
        LOG.finest(() -> this + " network connection close called by appserver, closing");
        channel.close();
    }

    private boolean domainDisconnectedButThereArePendingMessages()
    {
        if(isProtocolVersionOneOneOrOneTwo())
        {
            // only available in 1.1, 1.2
            return !domainDisconnectedHandled.get() || transactionInformation.transactionsInFlight();
        }
        return false;
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
        return ci.getProtocolVersion() == ProtocolVersion.VERSION_1_1.getVersion() || ci.getProtocolVersion() == ProtocolVersion.VERSION_1_2.getVersion();
    }

    private DomainId throwIfProtocolVersionNotSupportedByEIS(long version, final UUID domainId, final String domainName)
    {
        CasualDomainConnectRequestMessage requestMessage = CasualDomainConnectRequestMessage.createBuilder()
                                                                                            .withExecution(UUID.randomUUID())
                                                                                            .withDomainId(domainId)
                                                                                            .withDomainName(domainName)
                                                                                            .withProtocols(Arrays.asList(version))
                                                                                            .build();
        CasualNWMessage<CasualDomainConnectRequestMessage> nwMessage = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage);
        LOG.finest(() -> "about to send handshake: " + this);
        CompletableFuture<CasualNWMessage<CasualDomainConnectReplyMessage>> replyEnvelopeFuture = request(nwMessage);
        LOG.finest(() -> "handshake sent: " + this);
        CasualNWMessage<CasualDomainConnectReplyMessage> replyEnvelope = replyEnvelopeFuture.join();
        LOG.finest(() -> "received handshake reply: " + this);
        if(replyEnvelope.getMessage().getProtocolVersion() != version)
        {
            LOG.warning(() -> "protocol version mismatch, requesting:  " + version + " reply version: " + replyEnvelope.getMessage().getProtocolVersion());
            throw new CasualConnectionException("wanted protocol version " + version + " is not supported by casual.\n Casual suggested protocol version " + replyEnvelope.getMessage().getProtocolVersion());
        }
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
    public void domainDisconnecting(UUID execution)
    {
        LOG.info(() -> "Domain: " + execution + " is disconnecting");
        LOG.info(() -> "# of transactions active, commiting, preparing or rolling: " + transactionInformation.numberOfCurrentTransactions());
        domainDisconnected.set(true);
        disconnectedDomains.put(this, execution);
        logDisconnectedDomainInfo();
        // it could be that there are no messages in flight
        // which means we can handle domain disconnect immediately
        // if not, it will be triggered each time pending messages are empty
        if(correlator.isEmpty() && !transactionInformation.transactionsInFlight())
        {
            LOG.info(() -> "Correlator empty and no transactions in flight");
            handleDomainDisconnected();
        }
    }

    @Override
    public void correlatorEmpty()
    {
        if(!domainDisconnected.get() || transactionInformation.transactionsInFlight())
        {
            return;
        }
        handleDomainDisconnected();
    }

    private void handleDomainDisconnected()
    {
        if(domainDisconnectedHandled.get())
        {
            return;
        }
        domainDisconnectedHandled.set(true);
        handleClose(this, errorInformer);
    }

    private void logDisconnectedDomainInfo()
    {
        LOG.finest(() -> "current number of disconnected domains: " + disconnectedDomains.size());
    }

}
