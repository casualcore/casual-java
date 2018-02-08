package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.jca.message.Correlator;
import se.kodarkatten.casual.jca.message.impl.CorrelatorImpl;
import se.kodarkatten.casual.jca.work.NetworkReader;
import se.kodarkatten.casual.jca.work.NetworkReaderWorkListener;
import se.kodarkatten.casual.network.connection.CasualConnectionException;
import se.kodarkatten.casual.network.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.domain.CasualDomainConnectReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainConnectRequestMessage;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;

import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * Created by aleph on 2017-06-14.
 */
public final class CasualNetworkConnection implements NetworkConnection
{
    private static final Logger log = Logger.getLogger(NetworkReader.class.getName());
    private static final long readerStartTimeoutMS = 1000;
    private final SocketChannel socketChannel;
    private final Object lock = new Object();
    private final CasualNetworkConnectionInformation ci;
    private final ManagedConnectionInvalidator invalidator;
    private final NetworkReader reader;
    private final Correlator correlator;

    private CasualNetworkConnection(final SocketChannel socketChannel, final CasualNetworkConnectionInformation ci, final ManagedConnectionInvalidator invalidator, final NetworkReader reader, final Correlator correlator)
    {
        this.socketChannel = socketChannel;
        this.ci = ci;
        this.invalidator = invalidator;
        this.reader = reader;
        this.correlator = correlator;
    }

    public static CasualNetworkConnection of(final CasualNetworkConnectionInformation ci, final ManagedConnectionInvalidator invalidator, final WorkManager workManager)
    {
        Objects.requireNonNull(ci, "connection information is not allowed to be null");
        Objects.requireNonNull(invalidator,  "invalidator can not be null");
        Objects.requireNonNull(workManager, "workManager can not be null");
        try
        {
            SocketChannel channel = SocketChannel.open();
            channel.connect(ci.getAddress());

            Correlator correlator = CorrelatorImpl.of();
            NetworkReader reader = NetworkReader.of(correlator, channel, invalidator);
            long startupTime = workManager.startWork(reader, readerStartTimeoutMS, null, new NetworkReaderWorkListener());
            log.finest("reader startup time(ms): " + startupTime);
            CasualNetworkConnection c = new CasualNetworkConnection(channel, ci, invalidator, reader, correlator);
            c.throwIfProtocolVersionNotSupportedByEIS(ci.getProtocolVersion(), ci.getDomainId(), ci.getDomainName());
            return c;
        }
        catch (IOException | WorkException e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message)
    {
        try
        {
            CompletableFuture<CasualNWMessage<T>> f = new CompletableFuture<>();
            correlator.put(message.getCorrelationId(), f);
            synchronized (lock)
            {
                CasualNetworkWriter.write(socketChannel, message);
            }
            return f;
        }
        catch(CasualTransportException e)
        {
            correlator.forget(message.getCorrelationId());
            if(e.getCause() instanceof IOException)
            {
                invalidator.invalidate(e);
            }
            throw e;
        }
    }

    @Override
    public void close()
    {
        try
        {
            synchronized (lock)
            {
                if(!reader.isReleased())
                {
                    // we got here due to MC idling too long so the MC was reaped
                    reader.release();
                }// the other case is that the MC is reaped due to the a connected casual instance going away so the reader failed
                socketChannel.close();
            }
        }
        catch (IOException e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualNetworkConnection{");
        sb.append("socketChannel=").append(socketChannel);
        sb.append(", lock=").append(lock);
        sb.append(", ci=").append(ci);
        sb.append('}');
        return sb.toString();
    }

    public CasualNetworkConnectionInformation getConnectionInformation()
    {
        return ci;
    }

    private void throwIfProtocolVersionNotSupportedByEIS(long version, final UUID domainId, final String domainName)
    {
        CasualDomainConnectRequestMessage requestMessage = CasualDomainConnectRequestMessage.createBuilder()
                                                                                            .withExecution(UUID.randomUUID())
                                                                                            .withDomainId(domainId)
                                                                                            .withDomainName(domainName)
                                                                                            .withProtocols(Arrays.asList(version))
                                                                                            .build();
        CasualNWMessage<CasualDomainConnectRequestMessage> nwMessage = CasualNWMessage.of(UUID.randomUUID(), requestMessage);
        CompletableFuture<CasualNWMessage<CasualDomainConnectReplyMessage>> replyEnvelopeFuture = request(nwMessage);
        try
        {
            CasualNWMessage<CasualDomainConnectReplyMessage> replyEnvelope = replyEnvelopeFuture.get();
            if(replyEnvelope.getMessage().getProtocolVersion() != version)
            {
                throw new CasualConnectionException("wanted protocol version " + version + " is not supported by casual.\n Casual suggested protocol version " + replyEnvelope.getMessage().getProtocolVersion());
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualConnectionException(e);
        }
    }

}

