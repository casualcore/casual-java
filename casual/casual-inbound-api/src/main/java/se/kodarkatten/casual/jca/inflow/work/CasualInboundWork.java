package se.kodarkatten.casual.jca.inflow.work;

import se.kodarkatten.casual.internal.server.InboundServer;
import se.kodarkatten.casual.jca.inflow.CasualActivationSpec;

import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CasualInboundWork implements Work
{
    private AtomicBoolean released = new AtomicBoolean( false );
    private final CasualActivationSpec spec;
    private final MessageEndpointFactory messageEndpointFactory;
    private final WorkManager workManager;
    private final XATerminator xaTerminator;

    private InboundServer server;

    public CasualInboundWork(CasualActivationSpec spec, MessageEndpointFactory listener, WorkManager workManager, XATerminator xaTerminator)
    {
        this.spec = spec;
        this.messageEndpointFactory = listener;
        this.workManager = workManager;
        this.xaTerminator = xaTerminator;
        server = InboundServer.of( spec.getPort(), new SocketChannelConsumer( this ) );
    }

    @Override
    public void release()
    {
        released.set( true );
        server.stop();
    }

    @Override
    public void run()
    {
        server.start();
    }

    public InboundServer getInboundServer()
    {
        return server;
    }

    public boolean isReleased()
    {
        return released.get();
    }

    public CasualActivationSpec getSpec()
    {
        return spec;
    }

    public MessageEndpointFactory getMessageEndpointFactory()
    {
        return messageEndpointFactory;
    }

    public WorkManager getWorkManager()
    {
        return workManager;
    }

    public XATerminator getXaTerminator()
    {
        return xaTerminator;
    }
}
