/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.jca.ConnectionObserver;
import se.laz.casual.jca.DomainId;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DomainDiscoveryTopologyChangedHandler
{
    private final Set<ConnectionObserver> observers = ConcurrentHashMap.newKeySet();
    public static DomainDiscoveryTopologyChangedHandler of()
    {
        return new DomainDiscoveryTopologyChangedHandler();
    }

    public void addConnectionObserver(ConnectionObserver observer)
    {
        observers.add(observer);
    }

    public void notifyTopologyChanged(DomainId domainId)
    {
        observers.forEach(observer -> observer.topologyChanged(domainId));
    }
}
