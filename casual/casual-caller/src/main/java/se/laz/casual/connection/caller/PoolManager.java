package se.laz.casual.connection.caller;

import se.laz.casual.connection.caller.logic.PoolDataRetriever;
import se.laz.casual.jca.CasualConnectionListener;
import se.laz.casual.jca.DomainId;

import javax.enterprise.event.Event;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class PoolManager implements CasualConnectionListener
{
    private static final Logger LOG = Logger.getLogger(PoolManager.class.getName());
    private ConnectionFactoryEntryStore connectionFactoryEntryStore;
    private PoolDataRetriever poolDataRetriever;
    private Event<NewDomainEvent> newDomain;
    private Event<DomainGoneEvent> domainGone;
    private List<Pool> pools;

    // for wls
    public PoolManager()
    {}

    @Inject
    public PoolManager(ConnectionFactoryEntryStore connectionFactoryEntryStore, PoolDataRetriever poolDataRetriever, Event<NewDomainEvent> newDomain, Event<DomainGoneEvent> domainGone)
    {
        this.connectionFactoryEntryStore = connectionFactoryEntryStore;
        this.poolDataRetriever = poolDataRetriever;
        this.newDomain = newDomain;
        this.domainGone = domainGone;
        this.pools = poolDataRetriever.get(connectionFactoryEntryStore.get(), this);
    }

    public List<Pool> getPools()
    {
        return Collections.unmodifiableList(pools);
    }

    @Override
    public void newConnection(DomainId domainId)
    {
        LOG.warning(() -> "start handling new connection: " + domainId);
        boolean alreadyHandled = pools.stream()
                                      .filter(pool -> pool.getDomainIds().contains(domainId))
                                      .collect(Collectors.counting()) != 0;
        if (!alreadyHandled)
        {
            LOG.warning(() -> "handling new connection: " + domainId);
            Pool matchingPool = updatePools().stream()
                                             .filter(pool -> pool.getDomainIds().contains(domainId))
                                             .findFirst()
                                             .orElseThrow(() -> new CasualCallerException("Expected domainId: " + domainId + " missing"));
            List<DomainId> domainIds = new ArrayList<>();
            domainIds.add(domainId);
            newDomain.fire(NewDomainEvent.of(Pool.of(matchingPool.getConnectionFactoryEntry(), domainIds)));
        }
    }

    @Override
    public void connectionGone(DomainId domainId)
    {
        Pool matchingPool = pools.stream()
                                 .filter(pool -> pool.getDomainIds().contains(domainId))
                                 .findFirst()
                                 .orElse(null);
        if(null != matchingPool)
        {
            LOG.warning(() -> "handling connection gone: " + domainId);
            updatePools();
            domainGone.fire(DomainGoneEvent.of(matchingPool.getConnectionFactoryEntry(), domainId));
        }
    }

    private synchronized List<Pool> updatePools()
    {
        pools = poolDataRetriever.get(connectionFactoryEntryStore.get(), this);
        return getPools();
    }
}
