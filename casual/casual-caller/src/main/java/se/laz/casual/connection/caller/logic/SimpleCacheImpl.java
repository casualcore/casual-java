package se.laz.casual.connection.caller.logic;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.DomainGoneEvent;
import se.laz.casual.connection.caller.MatchingEntry;
import se.laz.casual.connection.caller.NewDomainEvent;
import se.laz.casual.connection.caller.Pool;
import se.laz.casual.connection.caller.SimpleCache;

import javax.enterprise.event.Observes;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class SimpleCacheImpl implements SimpleCache
{
    private static final Logger LOG = Logger.getLogger(SimpleCacheImpl.class.getName());
    private final Map<ServiceInfo, List<MatchingEntry>> services = new ConcurrentHashMap<>();
    private final Map<QueueInfo, List<MatchingEntry>> queues = new ConcurrentHashMap<>();

    // We keep a state of the seen world since services and queues can come and go
    // This is so that once we see a new domain we can issue a total domain discovery in one go
    private final Map<ServiceInfo, Boolean> allSeenServiceNames = new ConcurrentHashMap<>();
    private final Map<QueueInfo, Boolean> allSeenQueueNames = new ConcurrentHashMap<>();
    private PoolMatcher poolMatcher;

    // for wls
    public SimpleCacheImpl()
    {}


    @Inject
    public SimpleCacheImpl(PoolMatcher poolMatcher)
    {
        this.poolMatcher = poolMatcher;
    }

    public void onNewDomain(@Observes NewDomainEvent newDomainEvent)
    {
        LOG.warning(() -> "onNewDomain: " + newDomainEvent);
        List<Pool> pools = new ArrayList<>();
        pools.add(newDomainEvent.getPool());
        List<MatchingEntry> matchingEntries = poolMatcher.match(getAllSeenServices(), getAllSeenQueues(), pools);
        store(matchingEntries);
        LOG.warning(() -> "onNewDomain done:" + newDomainEvent);
    }

    public void onDomainGone(@Observes DomainGoneEvent event)
    {
        LOG.warning(() -> "onDomainGone: " + event);
        services.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(matchingEntry -> matchingEntry.getDomainId().equals(event.getDomainId()));
            return entry.getValue().isEmpty();
        });
        queues.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(matchingEntry -> matchingEntry.getDomainId().equals(event.getDomainId()));
            return entry.getValue().isEmpty();
        });
        LOG.warning(() -> "onDomainGone done: " + event);
    }

    @Override
    public void store(List<MatchingEntry> matchingEntries)
    {
        List<MatchingEntry> uniqueEntries = matchingEntries.stream()
                                                           .distinct()
                                                           .collect(Collectors.toList());
        LOG.warning(() -> "will cache:" + uniqueEntries);
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
