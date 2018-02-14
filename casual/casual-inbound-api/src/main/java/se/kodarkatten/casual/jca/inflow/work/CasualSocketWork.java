package se.kodarkatten.casual.jca.inflow.work;

import se.kodarkatten.casual.jca.CasualResourceAdapterException;
import se.kodarkatten.casual.jca.inflow.CasualMessageListener;
import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.io.LockableSocketChannel;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainConnectRequestMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static se.kodarkatten.casual.internal.work.WorkContextFactory.createLongRunningContext;

public final class CasualSocketWork implements Work, WorkContextProvider
{
    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.getLogger(CasualSocketWork.class.getName());
    private AtomicBoolean released = new AtomicBoolean( false );
    private final LockableSocketChannel channel;
    private final CasualInboundWork work;
    private final List<WorkContext> workContexts;

    public CasualSocketWork( LockableSocketChannel channel, CasualInboundWork work )
    {
        this.channel = channel;
        this.work = work;
        this.workContexts = createLongRunningContext();
    }

    public LockableSocketChannel getSocketChannel()
    {
        return this.channel;
    }

    public CasualInboundWork getWork()
    {
        return work;
    }

    @Override
    public void release()
    {
        released.set( true );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run()
    {
        while( this.channel.getSocketChannel().isConnected() && !released.get() )
        {
            try
            {
                MessageEndpoint endpoint = work.getMessageEndpointFactory().createEndpoint(null);
                CasualMessageListener listener = (CasualMessageListener) endpoint;

                CasualNWMessage<?> message = CasualNetworkReader.read( channel );
                switch ( message.getType() )
                {
                    case COMMIT_REQUEST:
                        listener.commitRequest((CasualNWMessage<CasualTransactionResourceCommitRequestMessage>)message, channel, work.getXaTerminator());
                        break;
                    case PREPARE_REQUEST:
                        listener.prepareRequest((CasualNWMessage<CasualTransactionResourcePrepareRequestMessage>)message, channel, work.getXaTerminator());
                        break;
                    case REQUEST_ROLLBACK:
                        listener.requestRollback((CasualNWMessage<CasualTransactionResourceRollbackRequestMessage>)message, channel, work.getXaTerminator());
                        break;
                    case SERVICE_CALL_REQUEST:
                        listener.serviceCallRequest((CasualNWMessage<CasualServiceCallRequestMessage>)message, channel, work.getWorkManager());
                        break;
                    case DOMAIN_CONNECT_REQUEST:
                        listener.domainConnectRequest((CasualNWMessage<CasualDomainConnectRequestMessage>)message, channel);
                        break;
                    case DOMAIN_DISCOVERY_REQUEST:
                        listener.domainDiscoveryRequest((CasualNWMessage<CasualDomainDiscoveryRequestMessage>)message, channel);
                        break;
                    default:
                        log.warning("Message type not supported: " + message.getType());
                }

            } catch (UnavailableException e)
            {
                throw new CasualResourceAdapterException("Error creating endpoint.", e);
            }
        }
        log.finest( ()-> "Exiting socket work." );
    }

    @Override
    public List<WorkContext> getWorkContexts()
    {
        return workContexts;
    }
}
