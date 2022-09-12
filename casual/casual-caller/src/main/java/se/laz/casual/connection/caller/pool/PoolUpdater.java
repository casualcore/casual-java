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

import javax.enterprise.event.Event;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class PoolUpdater
{
    private static final Logger LOG = Logger.getLogger(PoolUpdater.class.getName());
    private final ConnectionFactoryEntry connectionFactoryEntry;
    private final List<DomainId> domainIds;
    private final Event<NewDomainEvent> newDomain;
    private final Event<DomainGoneEvent> domainGone;
    private final Map<ConnectionFactoryEntry, Pool> pools;

    private PoolUpdater(Builder builder)
    {
        connectionFactoryEntry = builder.connectionFactoryEntry;
        domainIds = builder.domainIds;
        newDomain = builder.newDomain;
        domainGone = builder.domainGone;
        pools = builder.pools;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public void update()
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
        if(domainIdMismatch(pool.getDomainIds(), domainIds))
        {
            partialUpdate(connectionFactoryEntry, pool,domainIds);
        }
    }

    public static final class Builder
    {
        private ConnectionFactoryEntry connectionFactoryEntry;
        private List<DomainId> domainIds;
        private Event<NewDomainEvent> newDomain;
        private Event<DomainGoneEvent> domainGone;
        private Map<ConnectionFactoryEntry, Pool> pools;

        public Builder withConnectionFactoryEntry(ConnectionFactoryEntry connectionFactoryEntry)
        {
            this.connectionFactoryEntry = connectionFactoryEntry;
            return this;
        }

        public Builder withDomainIds(List<DomainId> domainIds)
        {
            this.domainIds = domainIds;
            return this;
        }

        public Builder withNewDomain(Event<NewDomainEvent> newDomain)
        {
            this.newDomain = newDomain;
            return this;
        }

        public Builder withDomainGone(Event<DomainGoneEvent> domainGone)
        {
            this.domainGone = domainGone;
            return this;
        }

        public Builder withPools(Map<ConnectionFactoryEntry, Pool> pools)
        {
            this.pools = pools;
            return this;
        }

        public PoolUpdater build()
        {
            Objects.requireNonNull(connectionFactoryEntry, "connectionFactoryEntry can not be null");
            Objects.requireNonNull(domainIds, "domainIds can not be null");
            Objects.requireNonNull(newDomain, "newDomain can not be null");
            Objects.requireNonNull(domainGone, "domainGone can not be null");
            Objects.requireNonNull(pools, "can not be null");
            return new PoolUpdater(this);
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

    private boolean domainIdMismatch(List<DomainId> first, List<DomainId> second)
    {
        return !first.equals(second);
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

}
