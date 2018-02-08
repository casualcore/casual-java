package se.kodarkatten.casual.jca.work;

import se.kodarkatten.casual.internal.work.WorkContextFactory;
import se.kodarkatten.casual.jca.ManagedConnectionInvalidator;
import se.kodarkatten.casual.jca.message.Correlator;
import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

// "Close this "SocketChannel"" - we can not, we do not own it
@SuppressWarnings("squid:S2095")
public final class NetworkReader implements Work, WorkContextProvider
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(NetworkReader.class.getName());
    private final Correlator correlator;
    private final SocketChannel socketChannel;
    private final ManagedConnectionInvalidator invalidator;
    private AtomicBoolean running = new AtomicBoolean(true);
    private final List<WorkContext> workContexts;

    private NetworkReader(final Correlator correlator, final SocketChannel socketChannel, final ManagedConnectionInvalidator invalidator, final List<WorkContext> workContexts)
    {
        this.correlator = correlator;
        this.socketChannel = socketChannel;
        this.invalidator = invalidator;
        this.workContexts = workContexts;

    }

    public static NetworkReader of(final Correlator correlator, final SocketChannel socketChannel, final ManagedConnectionInvalidator invalidator)
    {
        Objects.requireNonNull(correlator, "correlator can not be null");
        Objects.requireNonNull(socketChannel, "socketChannel can not be null");
        Objects.requireNonNull(invalidator, "invalidator can not be null");
        return new NetworkReader(correlator, socketChannel, invalidator, WorkContextFactory.createLongRunningContext());
    }

    public boolean isReleased()
    {
        return !running.get();
    }

    @Override
    public void release()
    {
        log.finest(() -> "release" + this);
        running.set(false);
    }

    @Override
    public void run()
    {
        log.finest(() -> "starting reader: " + this);
        while(running.get() && !Thread.currentThread().isInterrupted())
        {
            CasualNWMessageHeader header = null;
            try
            {
                header = CasualNetworkReader.networkHeaderToCasualHeader(socketChannel);
                correlator.complete(CasualNetworkReader.read(socketChannel, header));
            }
            catch(Exception e)
            {
                if(running.get())
                {
                    handleException(e, header);
                }
                // if not, we've been released by the network connection due to it being reaped by the application server due to MC being idle too long
                // IE CasualNetworkConnection::close has been called by the MC - it releases us before closing the socket which we then trip on
            }
        }
        log.finest(() -> "NetworkReader: " + this + " exiting" + " running?" + running.get() + " isInterrupted?" + Thread.currentThread().isInterrupted());
    }

    @Override
    public List<WorkContext> getWorkContexts()
    {
        return workContexts;
    }

    private void handleException(final Exception e, final CasualNWMessageHeader header)
    {
        if(e.getCause() instanceof IOException || e.getCause() instanceof InterruptedException || null == header)
        {
            running.set(false);
            correlator.completeExceptionally(correlator.getAllCorrids(), e);
            invalidator.invalidate(e);
        }
        else
        {
            List<UUID> l = new ArrayList<>();
            l.add(header.getCorrelationId());
            correlator.completeExceptionally(l, e);
        }
    }

}
