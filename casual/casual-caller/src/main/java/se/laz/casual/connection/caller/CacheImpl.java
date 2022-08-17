package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.events.DomainGone;
import se.laz.casual.connection.caller.entities.MatchingEntry;
import se.laz.casual.connection.caller.events.NewDomain;
import se.laz.casual.connection.caller.entities.Pool;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class CacheImpl implements Cache
{
    private static final Logger LOG = Logger.getLogger(CacheImpl.class.getName());
    // The current state of the world
    private final Map<ServiceInfo, List<MatchingEntry>> services = new ConcurrentHashMap<>();
    private final Map<QueueInfo, List<MatchingEntry>> queues = new ConcurrentHashMap<>();

    // We keep a state of the seen world since services and queues can come and go
    // This is so that once we see a new domain we can issue a total domain discovery in one go
    private final Map<ServiceInfo, Boolean> allSeenServiceNames = new ConcurrentHashMap<>();
    private final Map<QueueInfo, Boolean> allSeenQueueNames = new ConcurrentHashMap<>();
    private TransactionLess.PoolMatcher poolMatcher;

    // for wls
    public CacheImpl()
    {}

    @Inject
    public CacheImpl(TransactionLess.PoolMatcher poolMatcher)
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
            entry.getValue().removeIf(matchingEntry -> matchingEntry.getDomainId().equals(event.getDomainId()));
            return entry.getValue().isEmpty();
        });
        queues.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(matchingEntry -> matchingEntry.getDomainId().equals(event.getDomainId()));
            return entry.getValue().isEmpty();
        });
        LOG.finest(() -> "onDomainGone done: " + event);
    }

    @Override
    public void store(List<MatchingEntry> matchingEntries)
    {
        List<MatchingEntry> uniqueEntries = matchingEntries.stream()
                                                           .distinct()
                                                           .collect(Collectors.toList());
        LOG.finest(() -> "will cache:" + uniqueEntries);
        uniqueEntries.forEach(matchingEntry ->
                matchingEntry.getServices().
                             forEach(serviceDetails ->
                             {
                                 ServiceInfo key = ServiceInfo.of(serviceDetails.getName());
                                 services.putIfAbsent(key, new ArrayList<>());
                                 if(!services.get(key).contains(matchingEntry))
                                 {
                                     services.get(key).add(matchingEntry);
                                 }
                                 allSeenServiceNames.putIfAbsent(ServiceInfo.of(serviceDetails.getName()), true);
                             }));
        uniqueEntries.forEach(matchingEntry ->
                matchingEntry.getQueues().
                             forEach(queueDetails ->
                             {
                                 QueueInfo key = QueueInfo.of(queueDetails.getName());
                                 queues.putIfAbsent(key, new ArrayList<>());
                                 if(!queues.get(key).contains(matchingEntry))
                                 {
                                     queues.get(key).add(matchingEntry);
                                 }
                                 allSeenQueueNames.putIfAbsent(QueueInfo.of(queueDetails.getName()), true);
                             }));

    }

    @Override
    public List<MatchingEntry> get(ServiceInfo serviceInfo)
    {
        return Collections.unmodifiableList(services.getOrDefault(serviceInfo, Collections.emptyList()));
    }

    @Override
    public List<MatchingEntry> get(QueueInfo queueInfo)
    {
        return Collections.unmodifiableList(queues.getOrDefault(queueInfo, Collections.emptyList()));
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

