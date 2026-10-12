/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.jca.ConnectionObserver;
import se.laz.casual.jca.DomainId;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dispatches remote-domain lifecycle events to connection observers.
 *
 * <p>You can add observers and dispatch notifications concurrently. A failing observer does
 * not prevent other observers from receiving the event and does not propagate into the network
 * protocol handler.
 */
public final class ConnectionObserverHandler
{
    private static final Logger LOG = Logger.getLogger(ConnectionObserverHandler.class.getName());
    private final Set<ConnectionObserver> observers = ConcurrentHashMap.newKeySet();

    /**
     * Creates an observer handler.
     *
     * @return a new observer handler
     */
    public static ConnectionObserverHandler of()
    {
        return new ConnectionObserverHandler();
    }

    /**
     * Adds an observer unless the same observer is already registered.
     *
     * <p>You can call this method concurrently with notification methods.
     *
     * @param observer the observer to add
     * @throws NullPointerException if {@code observer} is {@code null}
     */
    public void addConnectionObserver(ConnectionObserver observer)
    {
        observers.add(Objects.requireNonNull(observer, "observer must not be null"));
    }

    /**
     * Notifies each registered observer that a remote domain's topology changed.
     *
     * @param domainId the remote domain whose topology changed
     * @throws NullPointerException if {@code domainId} is {@code null}
     */
    public void notifyTopologyChanged(DomainId domainId)
    {
        notifyObservers(
                Objects.requireNonNull(domainId, "domainId must not be null"),
                observer -> observer.topologyChanged(domainId));
    }

    /**
     * Notifies each registered observer that a remote domain starts disconnecting.
     *
     * @param domainId the remote domain that starts disconnecting
     * @throws NullPointerException if {@code domainId} is {@code null}
     */
    public void notifyDomainDisconnected(DomainId domainId)
    {
        notifyObservers(
                Objects.requireNonNull(domainId, "domainId must not be null"),
                observer -> observer.domainDisconnected(domainId));
    }

    private void notifyObservers(DomainId domainId, Consumer<ConnectionObserver> notification)
    {
        for (ConnectionObserver observer : observers)
        {
            try
            {
                notification.accept(observer);
            }
            catch (RuntimeException e)
            {
                LOG.log(Level.WARNING, e, () -> "Connection observer notification failed for domain: " + domainId);
            }
        }
    }
}
