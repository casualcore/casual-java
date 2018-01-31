package se.kodarkatten.casual.jca.inflow.work;

import se.kodarkatten.casual.internal.server.InboundServer;
import se.kodarkatten.casual.jca.inflow.CasualActivationSpec;

import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;
import javax.resource.spi.work.WorkManager;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static se.kodarkatten.casual.jca.inflow.work.WorkContextFactory.createLongRunningContext;

public class CasualInboundWork implements Work, WorkContextProvider
{
    private static final long serialVersionUID = 1L;

    private AtomicBoolean released = new AtomicBoolean( false );
    private final CasualActivationSpec spec;
    private final MessageEndpointFactory messageEndpointFactory;
    private final WorkManager workManager;
    private final XATerminator xaTerminator;
    private final List<WorkContext> workContexts;

    private InboundServer server;

    public CasualInboundWork(CasualActivationSpec spec, MessageEndpointFactory listener, WorkManager workManager, XATerminator xaTerminator)
    {
        this.spec = spec;
        this.messageEndpointFactory = listener;
        this.workManager = workManager;
        this.xaTerminator = xaTerminator;

        workContexts = createLongRunningContext();

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

    @Override
    public List<WorkContext> getWorkContexts()
    {
        return workContexts;
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
