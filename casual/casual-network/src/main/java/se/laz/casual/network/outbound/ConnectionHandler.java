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
import java.util.function.Consumer;

public class ConnectionHandler implements ConnectionListener
{
    private final Consumer<DomainDisconnectReplyInfo> domainDisconnectReplyFunction;
    private DomainDisconnectReplyInfo domainDisconnectReplyInfo;
    private final Set<ConnectionListener> connectionListeners = Collections.synchronizedSet(new HashSet<>());

    private ConnectionHandler(Consumer<DomainDisconnectReplyInfo> domainDisconnectReplyFunction)
    {
        this.domainDisconnectReplyFunction = domainDisconnectReplyFunction;
    }

    public static ConnectionHandler of(Consumer<DomainDisconnectReplyInfo> domainDisconnectReplyFunction)
    {
        Objects.requireNonNull(domainDisconnectReplyFunction, "domainDisconnectReplyFunction can not be null");
        return new ConnectionHandler(domainDisconnectReplyFunction);
    }

    public void domainDisconnecting(DomainDisconnectReplyInfo domainDisconnectReplyInfo)
    {
        Objects.requireNonNull(domainDisconnectReplyInfo, "domainDisconnectReplyInfo can not be null");
        this.domainDisconnectReplyInfo = domainDisconnectReplyInfo;
    }

    public boolean hasDomainBeenDisconnected()
    {
        return null != domainDisconnectReplyInfo;
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
        Objects.requireNonNull(domainDisconnectReplyInfo, "execution can not be null");
        domainDisconnectReplyFunction.accept(domainDisconnectReplyInfo);
    }

}
