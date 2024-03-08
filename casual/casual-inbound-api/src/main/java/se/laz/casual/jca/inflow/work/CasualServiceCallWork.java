/*
 * Copyright (c) 2017 - 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow.work;

import jakarta.resource.spi.work.Work;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.event.Order;
import se.laz.casual.event.ServiceCallEventHandlerFactory;
import se.laz.casual.event.ServiceCallEventPublisher;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerFactory;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerNotFoundException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
    private final CompletableFuture<Long> startupTimeFuture;
    private ServiceCallEventPublisher  eventPublisher;
    private CasualNWMessage<CasualServiceCallReplyMessage> response;
    private ServiceHandler handler = null;

    public CasualServiceCallWork(UUID correlationId, CasualServiceCallRequestMessage message, CompletableFuture<Long> startupTimeFuture)
    {
        this(correlationId, message, false, startupTimeFuture);
    }

    public CasualServiceCallWork(UUID correlationId, CasualServiceCallRequestMessage message, boolean isTpNoReply, CompletableFuture<Long> startupTimeFuture)
    {
        this.correlationId = correlationId;
        this.message = message;
        this.isTpNoReply = isTpNoReply;
        this.startupTimeFuture = startupTimeFuture;
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
            Instant start = Instant.now();
            callService();
            Instant end = Instant.now();
            getEventPublisher().createAndPostEvent( message.getXid(), message.getExecution(), message.getParentName(), message.getServiceName(), ErrorState.OK, startupTimeFuture.join(), start, end, Order.CONCURRENT);
        }
        catch( ServiceHandlerNotFoundException e)
        {
            log.warning( ()-> "ServiceHandler not available for: " + message.getServiceName() );
        }
    }

    private void issueCall()
    {
        CasualServiceCallReplyMessage.Builder replyBuilder = CasualServiceCallReplyMessage.createBuilder()
                                                                                          .setXid( message.getXid() )
                                                                                          .setExecution( message.getExecution() );
        CasualBuffer serviceResult = ServiceBuffer.empty();
        Instant start = Instant.now();
        try
        {

            InboundResponse reply = callService();
            Instant end = Instant.now();
            serviceResult = reply.getBuffer();

            replyBuilder
                    .setError(reply.getErrorState())
                    .setTransactionState(reply.getTransactionState())
                    .setUserSuppliedError( reply.getUserSuppliedErrorCode() );
            getEventPublisher().createAndPostEvent( message.getXid(), message.getExecution(), message.getParentName(), message.getServiceName(), reply.getErrorState(), startupTimeFuture.join(), start, end, Order.CONCURRENT);
        }
        catch( ServiceHandlerNotFoundException e )
        {
            Instant end = Instant.now();
            getEventPublisher().createAndPostEvent( message.getXid(), message.getExecution(), message.getParentName(), message.getServiceName(), ErrorState.TPENOENT, startupTimeFuture.join(), start, end, Order.CONCURRENT);
            replyBuilder.setError( ErrorState.TPENOENT )
                        .setTransactionState( TransactionState.ROLLBACK_ONLY );
            log.warning( ()-> "ServiceHandler not available for: " + message.getServiceName() );
        }
        finally
        {
            CasualServiceCallReplyMessage reply = replyBuilder
                    .setServiceBuffer( ServiceBuffer.of( serviceResult ) )
                    .build();
            CasualNWMessage<CasualServiceCallReplyMessage> replyMessage = CasualNWMessageImpl.of( correlationId,reply );
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

    ServiceCallEventPublisher getEventPublisher()
    {
        if(eventPublisher == null)
        {
            eventPublisher = ServiceCallEventPublisher.of(ServiceCallEventHandlerFactory.getHandler());
        }
        return eventPublisher;
    }

    void setEventPublisher(ServiceCallEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }
}
