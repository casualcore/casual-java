package se.kodarkatten.casual.jca.inflow.work;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.flags.ErrorState;
import se.kodarkatten.casual.api.flags.TransactionState;
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandler;
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandlerFactory;
import se.kodarkatten.casual.jca.inbound.handler.InboundRequest;
import se.kodarkatten.casual.jca.inbound.handler.InboundResponse;
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandlerNotFoundException;
import se.kodarkatten.casual.network.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.io.LockableSocketChannel;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

import javax.resource.spi.work.Work;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public final class CasualServiceCallWork implements Work
{
    private static Logger log = Logger.getLogger(CasualServiceCallWork.class.getName());

    private final CasualServiceCallRequestMessage message;
    private final LockableSocketChannel channel;
    private final UUID correlationId;

    private ServiceHandler handler = null;

    public CasualServiceCallWork(UUID correlationId, CasualServiceCallRequestMessage message, LockableSocketChannel channel )
    {
        this.correlationId = correlationId;
        this.message = message;
        this.channel = channel;
    }

    public CasualServiceCallRequestMessage getMessage()
    {
        return message;
    }

    public LockableSocketChannel getSocketChannel()
    {
        return channel;
    }

    public UUID getCorrelationId()
    {
        return correlationId;
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
        log.info( "run()." );

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

            if( reply.isSuccessful() )
            {
                replyBuilder
                        .setError(ErrorState.OK)
                        .setTransactionState(TransactionState.TX_ACTIVE);
            }
            else
            {
                replyBuilder
                        .setError( ErrorState.TPESVCERR )
                        .setTransactionState( TransactionState.ROLLBACK_ONLY );
                log.warning( ()->"Error occured whilst calling the service." );
            }
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
            CasualNWMessage<CasualServiceCallReplyMessage> replyMessage = CasualNWMessage.of( correlationId,reply );
            CasualNetworkWriter.write( this.channel, replyMessage );
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
