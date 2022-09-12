/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.pool;

import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.entities.DomainIdDiffResult;
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
            doUpdatePool(connectionFactoryEntry, domainIds);
            LOG.info(this::logPools);
        }
    }

    public List<Pool> getPools()
    {
        return Collections.unmodifiableList(new ArrayList<>(pools.values()));
    }

    private void doUpdatePool(ConnectionFactoryEntry connectionFactoryEntry, List<DomainId> domainIds)
    {
        Pool pool = pools.get(connectionFactoryEntry);
        if(alreadyHandled(pool, domainIds))
        {
            return;
        }
        if(newPool(pool))
        {
            LOG.info(() -> "new connection for: " + connectionFactoryEntry + " with domain ids: " + domainIds);
            pools.put(connectionFactoryEntry, Pool.of(connectionFactoryEntry, domainIds));
            newDomain.fire(NewDomainEvent.of(Pool.of(connectionFactoryEntry, domainIds)));
            return;
        }
        if (poolGone(domainIds))
        {
            LOG.info(() -> "connection gone for: " + connectionFactoryEntry + " with domain ids: " + pool.getDomainIds());
            pools.remove(connectionFactoryEntry);
            pool.getDomainIds().stream().forEach(id -> domainGone.fire(DomainGoneEvent.of(connectionFactoryEntry, id)));
            return;
        }
        if(!pool.equals(Pool.of(connectionFactoryEntry, domainIds)))
        {
            partialUpdate(connectionFactoryEntry, pool,domainIds);
        }
    }

    private boolean alreadyHandled(Pool pool, List<DomainId> domainIds)
    {
        return null == pool && domainIds.isEmpty();
    }

    private boolean newPool(Pool pool)
    {
        return null == pool;
    }

    private boolean poolGone(List<DomainId> domainIds)
    {
        return domainIds.isEmpty();
    }

    private void partialUpdate(ConnectionFactoryEntry connectionFactoryEntry, Pool pool, List<DomainId> domainIds)
    {
        DomainIdDiffResult diff = DomainIdDiffer.of(pool.getDomainIds(), domainIds).diff();
        if(!diff.getNewDomainIds().isEmpty())
        {
            LOG.info(() -> "new domain ids for: " + connectionFactoryEntry + " domain ids: " + diff.getNewDomainIds());
            pools.replace(connectionFactoryEntry, Pool.of(connectionFactoryEntry, diff.getNewDomainIds()));
            newDomain.fire(NewDomainEvent.of(Pool.of(connectionFactoryEntry, diff.getNewDomainIds())));
        }
        if(!diff.getLostDomainIds().isEmpty())
        {
            LOG.info(() -> "domain ids gone for: " + connectionFactoryEntry + " domain ids: " + diff.getLostDomainIds());
            pool.getDomainIds().removeIf(id -> diff.getLostDomainIds().contains(id));
            diff.getLostDomainIds().forEach(id -> domainGone.fire(DomainGoneEvent.of(connectionFactoryEntry, id)));
        }
    }

    private String logPools()
    {
        StringBuilder builder = new StringBuilder("currently known pools: ");
        getPools().forEach(builder::append);
        return builder.toString();
    }
}
