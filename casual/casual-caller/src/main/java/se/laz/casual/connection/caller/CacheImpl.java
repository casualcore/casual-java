package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.entities.CacheEntry;
import se.laz.casual.connection.caller.entities.CacheEntryWithHops;
import se.laz.casual.connection.caller.entities.CacheEntryWithHopsComparator;
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.entities.MatchingEntry;
import se.laz.casual.connection.caller.entities.Pool;
import se.laz.casual.connection.caller.events.DomainGone;
import se.laz.casual.connection.caller.events.NewDomain;
import se.laz.casual.jca.DomainId;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class CacheImpl implements Cache
{
    private static final Logger LOG = Logger.getLogger(CacheImpl.class.getName());
    // Please note that for queues we only ever cache one thing, queues should be unique across pools
    // if not, we'll just keep using the one we got while also warning when adding subsequent entries
    // The current state of the world
    private final Map<ServiceInfo, List<CacheEntryWithHops>> services = new ConcurrentHashMap<>();
    private final Map<QueueInfo, CacheEntry> queues = new ConcurrentHashMap<>();
    // We keep a state of the seen world since services and queues can come and go
    // This is so that once we see a new domain we can issue a total domain discovery in one go
    private final Map<ServiceInfo, Boolean> allSeenServiceNames = new ConcurrentHashMap<>();
    private final Map<QueueInfo, Boolean> allSeenQueueNames = new ConcurrentHashMap<>();
    private PoolMatcher poolMatcher;

    // for wls
    public CacheImpl()
    {}

    @Inject
    public CacheImpl(PoolMatcher poolMatcher)
    {
        this.poolMatcher = poolMatcher;
    }

    public void onNewDomain(@Observes NewDomain event)
    {
        LOG.finest(() -> "onNewDomain: " + event);
        List<Pool> pools = new ArrayList<>();
        pools.add(event.getPool());
        List<MatchingEntry> matchingEntries = poolMatcher.match(getAllSeenServices(), getAllSeenQueues(), pools);
        store(matchingEntries);
    }

    public void onDomainGone(@Observes DomainGone event)
    {
        LOG.finest(() -> "onDomainGone: " + event);
        services.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(matchingEntry -> matchingEntry.getCacheEntry().getDomainId().equals(event.getDomainId()));
            return entry.getValue().isEmpty();
        });
        queues.entrySet().removeIf(entry -> entry.getValue().getDomainId().equals(event.getDomainId()));
    }

    @Override
    public void store(List<MatchingEntry> matchingEntries)
    {
        List<MatchingEntry> uniqueEntries = matchingEntries.stream()
                                                           .distinct()
                                                           .collect(Collectors.toList());
        LOG.finest(() -> "will cache:" + uniqueEntries);
        uniqueEntries.forEach(matchingEntry -> {
            matchingEntry.getServices().
                         forEach(serviceDetails ->
                         {
                             ServiceInfo key = ServiceInfo.of(serviceDetails.getName());
                             services.putIfAbsent(key, new ArrayList<>());
                             CacheEntryWithHops cacheEntry = createServiceCacheEntry(matchingEntry.getDomainId(), matchingEntry.getConnectionFactoryEntry(), serviceDetails.getHops());
                             if (!services.get(key).contains(cacheEntry))
                             {
                                 services.get(key).add(cacheEntry);
                             }
                             allSeenServiceNames.putIfAbsent(ServiceInfo.of(serviceDetails.getName()), true);
                         });
            matchingEntry.getQueues().
                         forEach(queueDetails ->
                         {
                             QueueInfo key = QueueInfo.of(queueDetails.getName());
                             if(null != queues.putIfAbsent(key, createQueueCacheEntry(matchingEntry.getDomainId(), matchingEntry.getConnectionFactoryEntry())))
                             {
                                 LOG.warning(() -> "More than one queue with the name: " + queueDetails.getName() + "\nNot storing entry: " + matchingEntry);
                             }
                             allSeenQueueNames.putIfAbsent(QueueInfo.of(queueDetails.getName()), true);
                         });
                });
    }

    @Override
    public List<CacheEntry> get(ServiceInfo serviceInfo)
    {
        List<CacheEntryWithHops> matches = services.getOrDefault(serviceInfo, Collections.emptyList());
        Collections.sort(matches, CacheEntryWithHopsComparator.of());
        return matches.stream()
                      .map(match -> match.getCacheEntry())
                      .collect(Collectors.toList());
    }

    @Override
    public Optional<CacheEntry> get(QueueInfo queueInfo)
    {
        return Optional.ofNullable(queues.get(queueInfo));
    }

    @Override
    public List<String> getCachedServices()
    {
        return services.keySet().stream().map(ServiceInfo::toString).collect(Collectors.toList());
    }

    @Override
    public List<String> getCachedSQueues()
    {
        return queues.keySet().stream().map(QueueInfo::toString).collect(Collectors.toList());
    }

    @Override
    public List<String> getAllServices()
    {
        return allSeenServiceNames.keySet().stream().map(ServiceInfo::toString).collect(Collectors.toList());
    }

    @Override
    public List<String> getAllQueues()
    {
        return allSeenQueueNames.keySet().stream().map(QueueInfo::toString).collect(Collectors.toList());
    }

    @Override
    public void purgeServiceCache()
    {
        services.clear();
    }

    @Override
    public void purgeQueueCache()
    {
        queues.clear();
    }

    private CacheEntry createQueueCacheEntry(DomainId domainId, ConnectionFactoryEntry connectionFactoryEntry)
    {
        return CacheEntry.of(domainId, connectionFactoryEntry);
    }

    private CacheEntryWithHops createServiceCacheEntry(DomainId domainId, ConnectionFactoryEntry connectionFactoryEntry, long hops)
    {
        CacheEntry cacheEntry = CacheEntry.of(domainId, connectionFactoryEntry);
        return CacheEntryWithHops.of(cacheEntry, hops);
    }

    private List<ServiceInfo> getAllSeenServices()
    {
        return Collections.unmodifiableList(allSeenServiceNames.keySet().stream().collect(Collectors.toList()));
    }

    private List<QueueInfo> getAllSeenQueues()
    {
        return Collections.unmodifiableList(allSeenQueueNames.keySet().stream().collect(Collectors.toList()));
    }

}

