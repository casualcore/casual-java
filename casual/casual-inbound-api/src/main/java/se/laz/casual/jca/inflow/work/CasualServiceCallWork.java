/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow.work;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerFactory;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerNotFoundException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.api.buffer.type.ServiceBuffer;

import javax.resource.spi.work.Work;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public final class CasualServiceCallWork implements Work
{
    private static Logger log = Logger.getLogger(CasualServiceCallWork.class.getName());

    private final CasualServiceCallRequestMessage message;

    private final UUID correlationId;

    private CasualNWMessage<CasualServiceCallReplyMessage> response;

    private ServiceHandler handler = null;

    public CasualServiceCallWork(UUID correlationId, CasualServiceCallRequestMessage message )
    {
        this.correlationId = correlationId;
        this.message = message;
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
        CasualServiceCallReplyMessage.Builder replyBuilder = CasualServiceCallReplyMessage.createBuilder()
                .setXid( message.getXid() )
                .setExecution( message.getExecution() );

        CasualBuffer serviceResult = ServiceBuffer.of( message.getServiceBuffer().getType(), new ArrayList<>());
        try
        {
            ServiceHandler h = getHandler(message.getServiceName());

            InboundRequest request = InboundRequest.of( message.getServiceName(), message.getServiceBuffer() );
            InboundResponse reply = h.invokeService( request );
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
            CasualNWMessage<CasualServiceCallReplyMessage> replyMessage = CasualNWMessageImpl.of( correlationId,reply );

            response = replyMessage;
        }
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
