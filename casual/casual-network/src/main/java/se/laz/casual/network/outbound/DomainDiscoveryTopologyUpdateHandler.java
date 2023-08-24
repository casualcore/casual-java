/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.jca.ConnectionObserver;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DomainDiscoveryTopologyUpdateHandler
{
    private final Set<ConnectionObserver> observers = ConcurrentHashMap.newKeySet();
    public static DomainDiscoveryTopologyUpdateHandler of()
    {
        return new DomainDiscoveryTopologyUpdateHandler();
    }

    public void addConnectionObserver(ConnectionObserver observer)
    {
        observers.add(observer);
    }

    public void notifyConnectionObservers()
    {
        observers.forEach(observer -> observer.topologyUpdated());
    }
}
