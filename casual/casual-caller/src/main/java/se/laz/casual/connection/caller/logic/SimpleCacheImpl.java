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

    @Override
    public void store(ServiceInfo serviceInfo, List<MatchingEntry> matchingEntries)
    {
        String entriesInfo = matchingEntries.stream()
                                            .map(entry -> entry.toString())
                                            .collect(Collectors.joining(","));

        LOG.warning(() -> "would cache<" + serviceInfo + ">:" + entriesInfo);
        matchedEntriesPerService.put(serviceInfo, matchingEntries);
        storeConnectionFactoryEntryByDomainId(matchingEntries);
    }

    @Override
    public List<MatchingEntry> get(ServiceInfo serviceInfo)
    {
        return matchedEntriesPerService.getOrDefault(serviceInfo, Collections.emptyList());
    }

    @Override
    public void store(QueueInfo queueInfo, List<MatchingEntry> matchingEntries)
    {
        String entriesInfo = matchingEntries.stream()
                                            .map(entry -> entry.toString())
                                            .collect(Collectors.joining(","));
        LOG.warning(() -> "would cache<" + queueInfo + ">:" + entriesInfo);
        matchedEntriesPerQueue.put(queueInfo, matchingEntries);
        storeConnectionFactoryEntryByDomainId(matchingEntries);
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
    public Map<ConnectionFactoryEntry, List<DomainId>> handleLostDomains(PoolDomainIdGenerator generator)
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
            poolDomainIdsByConnectionFactoryEntry.putAll(newPoolDomainIds);
        }
        return getCurrentDomainIds();
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
