package se.laz.casual.jca.pool;

import se.laz.casual.api.conversation.ConversationClose;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.protocol.messages.conversation.Request;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ReferenceCountedNetworkConnection implements NetworkConnection
{
    private static final Logger log = Logger.getLogger(ReferenceCountedNetworkConnection.class.getName());
    private final AtomicInteger referenceCount = new AtomicInteger(0);
    private final NetworkConnection networkConnection;
    private final ReferenceCountedNetworkCloseListener closeListener;

    private ReferenceCountedNetworkConnection(NetworkConnection networkConnection, ReferenceCountedNetworkCloseListener closeListener)
    {
        this.networkConnection = networkConnection;
        this.closeListener = closeListener;
    }

    public static ReferenceCountedNetworkConnection of(NetworkConnection networkConnection, ReferenceCountedNetworkCloseListener closeListener)
    {
        Objects.requireNonNull(networkConnection, "networkConnection can not be null");
        return new ReferenceCountedNetworkConnection(networkConnection, closeListener);
    }

    public int increment()
    {
        return referenceCount.incrementAndGet();
    }

    @Override
    public <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message)
    {
        return networkConnection.request(message);
    }

    @Override
    public <X extends CasualNetworkTransmittable> void send(CasualNWMessage<X> message)
    {
        networkConnection.send(message);
    }

    @Override
    public CompletableFuture<CasualNWMessage<Request>> receive(UUID corrid)
    {
        return networkConnection.receive(corrid);
    }

    @Override
    public ConversationClose getConversationClose()
    {
        return networkConnection.getConversationClose();
    }

    @Override
    public void close()
    {
        if(referenceCount.decrementAndGet() == 0)
        {
            log.info(() -> "closing network connection: " + networkConnection);
            closeListener.closed(this);
            networkConnection.close();
        }
    }

    @Override
    public boolean isActive()
    {
        return networkConnection.isActive();
    }

    @Override
    public DomainId getDomainId()
    {
        return networkConnection.getDomainId();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ReferenceCountedNetworkConnection that = (ReferenceCountedNetworkConnection) o;
        return networkConnection.equals(that.networkConnection);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(networkConnection);
    }

    @Override
    public String toString()
    {
        return "ReferenceCountedNetworkConnection{" +
                "referenceCount=" + referenceCount +
                ", networkConnection=" + networkConnection +
                '}';
    }
}
