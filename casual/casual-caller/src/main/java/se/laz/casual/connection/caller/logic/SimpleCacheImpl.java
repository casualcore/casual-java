package se.laz.casual.connection.caller.logic;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.CasualCallerException;
import se.laz.casual.connection.caller.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.MatchingEntry;
import se.laz.casual.connection.caller.PoolDomainIdGenerator;
import se.laz.casual.connection.caller.SimpleCache;
import se.laz.casual.jca.DomainId;

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
    private final Map<ServiceInfo, List<MatchingEntry>> matchedEntriesPerService = new ConcurrentHashMap<>();
    private final Map<QueueInfo, List<MatchingEntry>> matchedEntriesPerQueue = new ConcurrentHashMap<>();

    private final Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIdsByConnectionFactoryEntry = new ConcurrentHashMap<>();
    private final Map<DomainId, ConnectionFactoryEntry> connectionFactoryEntryByDomainId = new ConcurrentHashMap<>();

    private final Map<MatchingEntry, Boolean> lostDomains = new ConcurrentHashMap<>();
    private ConnectionFactoryMatcher connectionFactoryMatcher;

    // for wls
    public SimpleCacheImpl()
    {}

    @Inject
    public SimpleCacheImpl(ConnectionFactoryMatcher connectionFactoryMatcher)
    {
        this.connectionFactoryMatcher = connectionFactoryMatcher;
    }

    @Override
    public void store(List<MatchingEntry> matchingEntries)
    {
        String entriesInfo = matchingEntries.stream()
                                            .map(entry -> entry.toString())
                                            .collect(Collectors.joining(","));
        LOG.warning(() -> "will cache:" + entriesInfo);
        matchingEntries.forEach(matchingEntry ->
                matchingEntry.getServices().
                             forEach(serviceDetails ->
                             {
                                 ServiceInfo key = ServiceInfo.of(serviceDetails.getName());
                                 matchedEntriesPerService.putIfAbsent(key, new ArrayList<>());
                                 matchedEntriesPerService.get(key).add(matchingEntry);
                             }));
        matchingEntries.forEach(matchingEntry ->
                matchingEntry.getQueues().
                             forEach(queueDetails ->
                             {
                                 QueueInfo key = QueueInfo.of(queueDetails.getName());
                                 matchedEntriesPerQueue.putIfAbsent(key, new ArrayList<>());
                                 matchedEntriesPerQueue.get(key).add(matchingEntry);
                             }));
        storeConnectionFactoryEntryByDomainId(matchingEntries);
    }

    @Override
    public List<MatchingEntry> get(ServiceInfo serviceInfo)
    {
        return matchedEntriesPerService.getOrDefault(serviceInfo, Collections.emptyList());
    }

    @Override
    public List<MatchingEntry> get(QueueInfo queueInfo)
    {
        return matchedEntriesPerQueue.getOrDefault(queueInfo, Collections.emptyList());
    }

    @Override
    public void store(Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds)
    {
        poolDomainIds.putAll(poolDomainIds);
    }

    @Override
    public Map<ConnectionFactoryEntry, List<DomainId>> getCurrentDomainIds()
    {
        return Collections.unmodifiableMap(poolDomainIdsByConnectionFactoryEntry);
    }

    @Override
    public List<ConnectionFactoryEntry> getConnectionFactoryEntriesForLostDomain()
    {
        return lostDomains.keySet()
                          .stream()
                          .map( entry -> entry.getConnectionFactoryEntry())
                          .collect(Collectors.toList());
    }

    @Override
    public synchronized Map<ConnectionFactoryEntry, List<DomainId>> handleLostDomains(PoolDomainIdGenerator generator)
    {
        List<ConnectionFactoryEntry> lost = getConnectionFactoryEntriesForLostDomain();
        if(!lost.isEmpty())
        {
            List<DomainId> knownIds = connectionFactoryEntryByDomainId.keySet().stream().collect(Collectors.toList());
            Map<ConnectionFactoryEntry, List<DomainId>> newPoolDomainIds = generator.apply(lost, this);
            newPoolDomainIds.entrySet().removeIf(entry -> {
                entry.getValue().removeIf(value -> knownIds.contains(value));
                return entry.getValue().isEmpty();
            });
            for(ConnectionFactoryEntry key: newPoolDomainIds.keySet())
            {
                lostDomains.entrySet().removeIf(entry -> entry.getKey().getConnectionFactoryEntry().equals(key));
            }
            discoverTheWorld(newPoolDomainIds);
            poolDomainIdsByConnectionFactoryEntry.putAll(newPoolDomainIds);
        }
        return getCurrentDomainIds();
    }

    private void discoverTheWorld(Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds)
    {
        List<ServiceInfo> knownServices = matchedEntriesPerService.keySet().stream().collect(Collectors.toList());
        List<QueueInfo> knownQueues = matchedEntriesPerQueue.keySet().stream().collect(Collectors.toList());

        List<MatchingEntry> matchingEntries = connectionFactoryMatcher.match(knownServices, knownQueues, poolDomainIds);
        for(MatchingEntry matchingEntry : matchingEntries)
        {

        }
    }

    @Override
    public void connectionGone(DomainId domainId)
    {
        ConnectionFactoryEntry connectionFactoryEntry = connectionFactoryEntryByDomainId.get(domainId);
        if(null == connectionFactoryEntry)
        {
            throw new CasualCallerException("No known connection factory for domainid: " + domainId);
        }
        lostDomains.put(MatchingEntry.of(connectionFactoryEntry, domainId),true);
        clearOnConnectionGone(domainId, connectionFactoryEntry);
    }

    private void storeConnectionFactoryEntryByDomainId(List<MatchingEntry> matchingEntries)
    {
        for(MatchingEntry matchingEntry : matchingEntries)
        {
            connectionFactoryEntryByDomainId.put(matchingEntry.getDomainId(), matchingEntry.getConnectionFactoryEntry());
        }
    }

    private void clearOnConnectionGone(DomainId domainId, ConnectionFactoryEntry connectionFactoryEntry)
    {
        for(Map.Entry<ServiceInfo, List<MatchingEntry>> entrySet : matchedEntriesPerService.entrySet())
        {
            entrySet.getValue().removeIf(matchingEntry -> matchingEntry.equals(MatchingEntry.of(connectionFactoryEntry, domainId)));
            matchedEntriesPerService.entrySet().removeIf(set -> set.getValue().isEmpty());
        }
        for(Map.Entry<QueueInfo, List<MatchingEntry>> entrySet : matchedEntriesPerQueue.entrySet())
        {
            entrySet.getValue().removeIf(matchingEntry -> matchingEntry.equals(MatchingEntry.of(connectionFactoryEntry, domainId)));
            matchedEntriesPerService.entrySet().removeIf(set -> set.getValue().isEmpty());
        }
        for(ConnectionFactoryEntry key : poolDomainIdsByConnectionFactoryEntry.keySet())
        {
            poolDomainIdsByConnectionFactoryEntry.get(key).remove(domainId);
        }
        connectionFactoryEntryByDomainId.remove(domainId);
    }
}
