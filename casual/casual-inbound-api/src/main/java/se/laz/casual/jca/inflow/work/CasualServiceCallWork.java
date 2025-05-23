/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow.work;

import io.opentelemetry.context.Scope;
import jakarta.resource.spi.work.Work;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerFactory;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerNotFoundException;
import se.laz.casual.jca.otel.LegacyTraceContext;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Work instance for performing inbound casual service call requests within the work manager.
 */
public final class CasualServiceCallWork implements Work
{
    private static final Logger log = Logger.getLogger(CasualServiceCallWork.class.getName());

    private final CasualServiceCallRequestMessage message;
    private final UUID correlationId;
    private final boolean isTpNoReply;
    private final ProtocolVersion protocolVersion;
    private final LegacyTraceContext legacyTraceContext;
    private CasualNWMessage<CasualServiceCallReplyMessage> response;
    private ServiceHandler handler = null;

    public CasualServiceCallWork(UUID correlationId, CasualServiceCallRequestMessage message, ProtocolVersion protocolVersion, LegacyTraceContext legacyTraceContext)
    {
        this(correlationId, message, false, protocolVersion, legacyTraceContext);
    }

    public CasualServiceCallWork(UUID correlationId, CasualServiceCallRequestMessage message, boolean isTpNoReply, ProtocolVersion protocolVersion)
    {
        this(correlationId, message, isTpNoReply, protocolVersion, null);
    }

    public CasualServiceCallWork(UUID correlationId, CasualServiceCallRequestMessage message, boolean isTpNoReply, ProtocolVersion protocolVersion, LegacyTraceContext legacyTraceContext)
    {
        this.correlationId = correlationId;
        this.message = message;
        this.isTpNoReply = isTpNoReply;
        this.protocolVersion = protocolVersion;
        this.legacyTraceContext = legacyTraceContext;
    }

    public CasualServiceCallRequestMessage getMessage()
    {
        return message;
    }

    public UUID getCorrelationId()
    {
        return correlationId;
    }

    public CasualNWMessage<CasualServiceCallReplyMessage> getResponse()
    {
        return this.response;
    }

    @Override
    public void release()
    {
        /**
         * Currently no way to stop a service lookup or call.
         * Transaction context with which this Work is started
         * is applied with timeout that lies outside this code.
         */
    }

    @Override
    public void run()
    {
        if(isTpNoReply)
        {
            issueCallNoReply();
        }
        else
        {
            issueCall();
        }
    }

    private void issueCallNoReply()
    {
        try
        {
            callService();
        }
        catch( ServiceHandlerNotFoundException e)
        {
            log.warning( ()-> "ServiceHandler not available for: " + message.getServiceName() );
        }
    }

    @SuppressWarnings("try")
    private void issueCall()
    {
        CasualServiceCallReplyMessage.Builder replyBuilder = CasualServiceCallReplyMessage.createBuilder()
                                                                                          .setExecution( message.getExecution() )
                                                                                          .setProtocolVersion(protocolVersion);
        if(!ProtocolVersion.isProtocolVersionOneGreaterOrEqualToOneThree(protocolVersion))
        {
            replyBuilder.setXid( message.getXid() );
        }
        CasualBuffer serviceResult = ServiceBuffer.empty();
        try
        {
            InboundResponse reply;
            if(ProtocolVersion.isProtocolVersionOneGreaterOrEqualToOneThree(protocolVersion))
            {
                try(Scope scope =  legacyTraceContext.toOtelContext().makeCurrent())
                {
                    reply = callService();
                }
            }
            else
            {
                reply = callService();
            }
            serviceResult = reply.getBuffer();

            replyBuilder
                    .setError(reply.getErrorState())
                    .setTransactionState(reply.getTransactionState())
                    .setUserSuppliedError( reply.getUserSuppliedErrorCode() );
        }
        catch( ServiceHandlerNotFoundException e )
        {
            replyBuilder.setError( ErrorState.TPENOENT )
                        .setTransactionState( TransactionState.ROLLBACK_ONLY );
            log.warning( ()-> "ServiceHandler not available for: " + message.getServiceName() );
        }
        finally
        {
            CasualServiceCallReplyMessage reply = replyBuilder
                    .setServiceBuffer( ServiceBuffer.of( serviceResult ) )
                    .build();
            CasualNWMessage<CasualServiceCallReplyMessage> replyMessage = CasualNWMessageImpl.of( correlationId, reply );
            this.response = replyMessage;
        }
    }

    private InboundResponse callService()
    {
        ServiceHandler h = getHandler(message.getServiceName());
        InboundRequest request = InboundRequest.of( message.getServiceName(), message.getServiceBuffer() );
        return h.invokeService( request );
    }

    ServiceHandler getHandler(String serviceName )
    {
        if( this.handler != null )
        {
            return this.handler;
        }
        return ServiceHandlerFactory.getHandler( serviceName );
    }

    void setHandler( ServiceHandler handler )
    {
        this.handler = handler;
    }

}
