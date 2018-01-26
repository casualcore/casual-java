package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.network.connection.CasualConnectionException;
import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.domain.CasualDomainConnectReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainConnectRequestMessage;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by aleph on 2017-06-14.
 */
public final class CasualNetworkConnection implements NetworkConnection
{
    private final SocketChannel socketChannel;
    private final Object lock = new Object();
    private final CasualNetworkConnectionInformation ci;
    private final ManagedConnectionInvalidator invalidator;

    private CasualNetworkConnection(final SocketChannel socketChannel, final CasualNetworkConnectionInformation ci, final ManagedConnectionInvalidator invalidator)
    {
        this.socketChannel = socketChannel;
        this.ci = ci;
        this.invalidator = invalidator;
    }

    public static CasualNetworkConnection of(final CasualNetworkConnectionInformation ci, final ManagedConnectionInvalidator invalidator)
    {
        Objects.requireNonNull(ci, "connection information is not allowed to be null");
        Objects.requireNonNull(invalidator,  "invalidator can not be null");
        try
        {
            SocketChannel channel = SocketChannel.open();
            channel.connect(ci.getAddress());
            CasualNetworkConnection c = new CasualNetworkConnection(channel, ci, invalidator);
            c.throwIfProtocolVersionNotSupportedByEIS(ci.getProtocolVersion(), ci.getDomainId(), ci.getDomainName());
            return c;
        }
        catch (IOException e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public final <T extends CasualNetworkTransmittable,X extends CasualNetworkTransmittable>  CasualNWMessage<T> requestReply(CasualNWMessage<X> message)
    {
        synchronized (lock)
        {
            try
            {
                CasualNetworkWriter.write(socketChannel, message);
                return CasualNetworkReader.read(socketChannel);
            }
            catch(CasualTransportException e)
            {
                if(e.getCause() instanceof IOException)
                {
                    invalidator.invalidate();
                }
                throw e;
            }
        }
    }

    @Override
    public void close()
    {
        try
        {
            synchronized (lock)
            {
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
        CasualNWMessage<CasualDomainConnectReplyMessage> replyEnvelope = requestReply(nwMessage);
        if(replyEnvelope.getMessage().getProtocolVersion() != version)
        {
            throw new CasualConnectionException("wanted protocol version " + version + " is not supported by casual.\n Casual suggested protocol version " + replyEnvelope.getMessage().getProtocolVersion());
        }
    }

}

