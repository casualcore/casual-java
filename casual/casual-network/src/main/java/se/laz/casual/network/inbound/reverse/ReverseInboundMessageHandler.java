/*
 * Copyright (c) 2024 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.work.WorkManager;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.jca.inflow.CasualInboundTransactionRegistry;
import se.laz.casual.jca.inflow.CasualMessageListener;
import se.laz.casual.network.inbound.ProtocolVersionValueHolder;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public final class ReverseInboundMessageHandler extends SimpleChannelInboundHandler<CasualNWMessage<?>>
{
    private static Logger log = Logger.getLogger(ReverseInboundMessageHandler.class.getName());
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private final MessageEndpointFactory factory;
    private final XATerminator xaTerminator;
    private final WorkManager workManager;
    private final CasualInboundTransactionRegistry inboundTransactionRegistry;
    private final ProtocolVersionValueHolder valueHolder;

    private ReverseInboundMessageHandler(MessageEndpointFactory factory, XATerminator xaTerminator, WorkManager workManager, CasualInboundTransactionRegistry inboundTransactionRegistry, ProtocolVersionValueHolder valueHolder)
    {
        this.factory = factory;
        this.xaTerminator = xaTerminator;
        this.workManager = workManager;
        this.inboundTransactionRegistry = inboundTransactionRegistry;
        this.valueHolder = valueHolder;
    }

    public static ReverseInboundMessageHandler of(final MessageEndpointFactory factory, final XATerminator xaTerminator, final WorkManager workManager, CasualInboundTransactionRegistry inboundTransactionRegistry, ProtocolVersionValueHolder valueHolder)
    {
        Objects.requireNonNull(factory, "factory can not be null");
        Objects.requireNonNull(xaTerminator, "xaTerminator can not be null");
        Objects.requireNonNull(workManager, "workManager can not be null");
        Objects.requireNonNull(inboundTransactionRegistry, "inboundTransactionRegistry can not be null");
        Objects.requireNonNull(valueHolder, "valueHolder can not be null");
        return new ReverseInboundMessageHandler(factory, xaTerminator, workManager, inboundTransactionRegistry, valueHolder);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CasualNWMessage<?> message) throws Exception
    {
        MessageEndpoint endpoint = factory.createEndpoint(null);
        CasualMessageListener listener = (CasualMessageListener) endpoint;
        log.finest(() -> "reverse inbound msg: " + message);
        switch ( message.getType() )
        {
            case COMMIT_REQUEST:
                executor.execute(() -> listener.commitRequest((CasualNWMessage<CasualTransactionResourceCommitRequestMessage>)message, ctx.channel(), xaTerminator, inboundTransactionRegistry));
                break;
            case PREPARE_REQUEST:
                executor.execute(() -> listener.prepareRequest((CasualNWMessage<CasualTransactionResourcePrepareRequestMessage>)message, ctx.channel(), xaTerminator, inboundTransactionRegistry));
                break;
            case REQUEST_ROLLBACK:
                executor.execute(() -> listener.requestRollback((CasualNWMessage<CasualTransactionResourceRollbackRequestMessage>)message, ctx.channel(), xaTerminator, inboundTransactionRegistry));
                break;
            case SERVICE_CALL_REQUEST, SERVICE_CALL_REQUEST_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE:
                listener.serviceCallRequest((CasualNWMessage<CasualServiceCallRequestMessage>)message, ctx.channel(), workManager, inboundTransactionRegistry, valueHolder.get());
                break;
            case DOMAIN_CONNECT_REQUEST:
                listener.domainConnectRequest((CasualNWMessage<CasualDomainConnectRequestMessage>)message, ctx.channel(), valueHolder);
                break;
            case DOMAIN_DISCONNECT_REPLY:
                listener.domainDisconnectReply((CasualNWMessage<DomainDisconnectReplyMessage>)message);
                break;
            case DOMAIN_DISCOVERY_REQUEST:
                listener.domainDiscoveryRequest((CasualNWMessage<CasualDomainDiscoveryRequestMessage>)message, ctx.channel(), valueHolder.get());
                break;
            default:
                log.warning("Message type not supported: " + message.getType());
        }
    }

}
