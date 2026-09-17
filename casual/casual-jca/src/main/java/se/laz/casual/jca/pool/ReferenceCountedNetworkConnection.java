/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool;

import se.laz.casual.api.conversation.ConversationClose;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.ConnectionObserver;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.outbound.NettyNetworkConnection;
import se.laz.casual.network.outbound.NetworkListener;
import se.laz.casual.network.protocol.messages.conversation.Request;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ReferenceCountedNetworkConnection implements NetworkConnection
{
    private static final Logger log = Logger.getLogger(ReferenceCountedNetworkConnection.class.getName());
    private final Object referenceLock = new Object();
    private int referenceCount = 1;
    // guarded by referenceLock, once the count reaches zero the connection is closed for good
    private boolean closed;
    private final NettyNetworkConnection networkConnection;
    private final ReferenceCountedNetworkCloseListener closeListener;

    private ReferenceCountedNetworkConnection(NettyNetworkConnection networkConnection, ReferenceCountedNetworkCloseListener closeListener)
    {
        this.networkConnection = networkConnection;
        this.closeListener = closeListener;
    }

    /**
     * Creates a connection with one reference owned by the caller.
     *
     * @param networkConnection the physical connection
     * @param closeListener the listener notified after the final reference is released
     * @return a reference-counted connection
     */
    public static ReferenceCountedNetworkConnection of(NettyNetworkConnection networkConnection, ReferenceCountedNetworkCloseListener closeListener)
    {
        Objects.requireNonNull(networkConnection, "networkConnection can not be null");
        return new ReferenceCountedNetworkConnection(networkConnection, closeListener);
    }

    @Override
    public  boolean isDomainDisconnecting()
    {
        return networkConnection.isDomainDisconnecting();
    }

    /**
     * Acquires a reference unless the final reference has already been released.
     *
     * <p>You can call this method concurrently with {@link #close()}.
     *
     * @return {@code true} if a reference is acquired; {@code false} if already closed
     */
    public boolean tryIncrement()
    {
        synchronized (referenceLock)
        {
            if(closed)
            {
                return false;
            }
            log.finest(() -> "increment current refcount: " + referenceCount + " for network connection: " + networkConnection);
            referenceCount++;
            return true;
        }
    }

    public void addListener(NetworkListener listener)
    {
       networkConnection.addListener(listener);
    }

    @Override
    public <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message)
    {
        return networkConnection.request(message);
    }

    @Override
    public <X extends CasualNetworkTransmittable> void requestNoReply(CasualNWMessage<X> message)
    {
        networkConnection.requestNoReply(message);
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
        synchronized (referenceLock)
        {
            log.finest(() -> "close current refcount: " + referenceCount);
            if(closed || --referenceCount != 0)
            {
                return;
            }
            closed = true;
        }
        // outside the lock, the close listener takes the pool lock and the pool
        // calls tryIncrement while holding it
        log.finest(() -> "closing network connection: " + networkConnection);
        networkConnection.close();
        closeListener.closed(this);
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
    public void addConnectionObserver(ConnectionObserver observer)
    {
        networkConnection.addConnectionObserver(observer);
    }

    @Override
    public ProtocolVersion getProtocolVersion()
    {
        return networkConnection.getProtocolVersion();
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
