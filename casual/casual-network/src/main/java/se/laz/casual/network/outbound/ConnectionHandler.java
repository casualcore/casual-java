/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.jca.ConnectionListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ConnectionHandler implements ConnectionListener
{
    private final Consumer<UUID> domainDisconnectReplyFunction;
    private UUID execution;
    private final Set<ConnectionListener> connectionListeners = Collections.synchronizedSet(new HashSet<>());

    private ConnectionHandler(Consumer<UUID> domainDisconnectReplyFunction)
    {
        this.domainDisconnectReplyFunction = domainDisconnectReplyFunction;
    }

    public static ConnectionHandler of(Consumer<UUID> domainDisconnectReplyFunction)
    {
        Objects.requireNonNull(domainDisconnectReplyFunction, "domainDisconnectReplyFunction can not be null");
        return new ConnectionHandler(domainDisconnectReplyFunction);
    }

    public void domainDisconnecting(UUID execution)
    {
        Objects.requireNonNull(execution, "execution can not be null");
        this.execution = execution;

    }

    public UUID getExecution()
    {
        return execution;
    }

    public boolean hasDomainBeenDisconnected()
    {
        return null != execution;
    }

    public void addConnectionListener(ConnectionListener listener)
    {
        connectionListeners.add(listener);
    }

    @Override
    public void connectionDisabled()
    {
        notifyConnectionListenersConnectionDisabled();
    }

    @Override
    public void connectionEnabled()
    {
        notifyConnectionListenersConnectionEnabled();
    }

    private void notifyConnectionListenersConnectionDisabled()
    {
        synchronized (connectionListeners)
        {
            connectionListeners.forEach(ConnectionListener::connectionDisabled);
            sendDomainDisconnectReply();
        }
    }

    private void notifyConnectionListenersConnectionEnabled()
    {
        synchronized (connectionListeners)
        {
            connectionListeners.forEach(ConnectionListener::connectionEnabled);
        }
    }

    private void sendDomainDisconnectReply()
    {
        Objects.requireNonNull(execution, "execution can not be null");
        domainDisconnectReplyFunction.accept(execution);
    }

}
