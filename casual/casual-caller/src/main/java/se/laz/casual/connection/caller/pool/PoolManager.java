/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.pool;

import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.entities.Pool;
import se.laz.casual.connection.caller.events.DomainGoneEvent;
import se.laz.casual.connection.caller.events.NewDomainEvent;
import se.laz.casual.jca.DomainId;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@ApplicationScoped
public class PoolManager
{
    private static final Logger LOG = Logger.getLogger(PoolManager.class.getName());
    private Event<NewDomainEvent> newDomain;
    private Event<DomainGoneEvent> domainGone;
    private Map<ConnectionFactoryEntry, Pool> pools = new ConcurrentHashMap<>();
    private Object poolLock = new Object();

    // for wls
    public PoolManager()
    {}

    @Inject
    public PoolManager(Event<NewDomainEvent> newDomain, Event<DomainGoneEvent> domainGone)
    {
        this.newDomain = newDomain;
        this.domainGone = domainGone;
    }

    public void updatePool(ConnectionFactoryEntry connectionFactoryEntry, List<DomainId> domainIds)
    {
        synchronized (poolLock)
        {
            LOG.info(() -> "updatePool: " + domainIds + " , " + connectionFactoryEntry);
            PoolUpdater updater = PoolUpdater.newBuilder()
                                            .withConnectionFactoryEntry(connectionFactoryEntry)
                                            .withDomainIds(domainIds)
                                            .withPools(pools)
                                            .withNewDomain(newDomain)
                                            .withDomainGone(domainGone)
                                            .build();
            updater.update();
            LOG.info(this::logPools);
        }
    }

    public List<Pool> getPools()
    {
        return Collections.unmodifiableList(new ArrayList<>(pools.values()));
    }

    private String logPools()
    {
        StringBuilder builder = new StringBuilder("currently known pools: ");
        getPools().forEach(builder::append);
        return builder.toString();
    }
}
