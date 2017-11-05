package se.kodarkatten.casual.jca.inflow.work;

import se.kodarkatten.casual.jca.CasualResourceAdapterException;
import se.kodarkatten.casual.jca.inflow.CasualMessageListener;
import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader;

import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.work.Work;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class CasualSocketWork implements Work
{
    private static Logger log = Logger.getLogger(CasualSocketWork.class.getName());
    private AtomicBoolean released = new AtomicBoolean( false );
    private final SocketChannel channel;
    private final CasualInboundWork work;

    public CasualSocketWork( SocketChannel channel, CasualInboundWork work )
    {
        this.channel = channel;
        this.work = work;
    }

    public SocketChannel getSocketChannel()
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

    @Override
    public void run()
    {
        while( this.channel.isConnected() && !released.get() )
        {
            try
            {
                MessageEndpoint endpoint = work.getMessageEndpointFactory().createEndpoint(null);
                CasualMessageListener listener = (CasualMessageListener) endpoint;

                CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader(channel);
                switch (header.getType())
                {
                    case COMMIT_REQUEST:
                        listener.commitRequest(header, channel, work.getXaTerminator());
                        break;
                    case PREPARE_REQUEST:
                        listener.prepareRequest(header, channel, work.getXaTerminator());
                        break;
                    case REQUEST_ROLLBACK:
                        listener.requestRollback(header, channel, work.getXaTerminator());
                        break;
                    case SERVICE_CALL_REQUEST:
                        listener.serviceCallRequest(header, channel, work.getWorkManager());
                        break;
                    case DOMAIN_CONNECT_REQUEST:
                        listener.domainConnectRequest(header, channel);
                        break;
                    case DOMAIN_DISCOVERY_REQUEST:
                        listener.domainDiscoveryRequest(header, channel);
                        break;
                    default:
                        log.warning("Message type not supported: " + header.getType());
                }

            } catch (UnavailableException e)
            {
                throw new CasualResourceAdapterException("Error creating endpoint.", e);
            }
        }
    }
}
