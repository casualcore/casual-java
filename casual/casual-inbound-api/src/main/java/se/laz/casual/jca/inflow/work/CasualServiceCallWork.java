/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow.work;

import com.google.protobuf.ByteString;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerFactory;
import se.laz.casual.jca.inbound.handler.service.ServiceHandlerNotFoundException;
import se.laz.casual.network.grpc.MessageCreator;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualServiceCallReply;
import se.laz.casual.network.messages.CasualServiceCallRequest;

import javax.resource.spi.work.Work;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Work instance for performing inbound casual service call requests within the work manager.
 */
public final class CasualServiceCallWork implements Work
{
    private static Logger log = Logger.getLogger(CasualServiceCallWork.class.getName());

    private final CasualServiceCallRequest request;

    private final UUID correlationId;

    private CasualReply response;

    private ServiceHandler handler = null;

    public CasualServiceCallWork(UUID correlationId, CasualServiceCallRequest request)
    {
        this.correlationId = correlationId;
        this.request = request;
    }

    public CasualServiceCallRequest getRequest()
    {
        return request;
    }

    public UUID getCorrelationId()
    {
        return correlationId;
    }

    public CasualReply getResponse()
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
        CasualServiceCallReply.Builder replyBuilder = CasualServiceCallReply.newBuilder()
                                                                            .setXid(request.getXid())
                                                                            .setExecution(request.getExecution());

        CasualBuffer serviceResult = ServiceBuffer.empty();
        try
        {
            ServiceHandler h = getHandler(request.getServiceName());

            List<byte[]> payload = new ArrayList<>();
            payload.add(this.request.getPayload().toByteArray());
            InboundRequest request = InboundRequest.of( this.request.getServiceName(),
                                                        ServiceBuffer.of(this.request.getBufferTypeName(), payload));
            InboundResponse reply = h.invokeService( request );
            serviceResult = reply.getBuffer();

            replyBuilder
                    .setResult(se.laz.casual.network.messages.ErrorState.valueOf(reply.getErrorState().name()))
                    .setTransactionState(MessageCreator.toTransactionState(reply.getTransactionState()))
                    .setUser( reply.getUserSuppliedErrorCode() );
        }
        catch( ServiceHandlerNotFoundException e )
        {
            replyBuilder.setResult(se.laz.casual.network.messages.ErrorState.valueOf(ErrorState.TPENOENT.name()))
                        .setTransactionState( MessageCreator.toTransactionState(TransactionState.ROLLBACK_ONLY ));
            log.warning( ()-> "ServiceHandler not available for: " + request.getServiceName() );
        }
        finally
        {
            byte[] payload = serviceResult.getBytes().isEmpty() ? new byte[0] : serviceResult.getBytes().get(0);
            CasualServiceCallReply reply = replyBuilder
                    .setPayload(ByteString.copyFrom(payload))
                    .setBufferTypeName(serviceResult.getType())
                    .build();
            response = CasualReply.newBuilder()
                                  .setCorrelationId(MessageCreator.toUUID4(correlationId))
                                  .setMessageType(CasualReply.MessageType.SERVICE_CALL_REPLY)
                                  .setServiceCall(reply)
                                  .build();
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
