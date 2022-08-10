package se.laz.casual.connection.caller.logic;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.MatchingEntry;
import se.laz.casual.connection.caller.SimpleCache;
import se.laz.casual.jca.DomainId;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SimpleCacheImpl implements SimpleCache
{
    private static final Logger LOG = Logger.getLogger(SimpleCacheImpl.class.getName());
    private final Map<DomainId, List<MatchingEntry>> matchingServiceEntriesPerDomainId = new ConcurrentHashMap<>();
    private final Map<String, List<MatchingEntry>> matchedServiceEntriesPerService = new ConcurrentHashMap<>();
    @Override
    public void store(ServiceInfo serviceInfo, List<MatchingEntry> matchingEntries)
    {
        String entriesInfo = matchingEntries.stream()
                                            .map(entry -> entry.toString())
                                            .collect(Collectors.joining(","));

        LOG.warning(() -> "would cache<" + serviceInfo + ">:" + entriesInfo);
    }

    @Override
    public List<MatchingEntry> get(ServiceInfo serviceName)
    {
        return Collections.emptyList();
    }

    @Override
    public void store(QueueInfo queueInfo, List<MatchingEntry> matchingEntries)
    {
        String entriesInfo = matchingEntries.stream()
                                            .map(entry -> entry.toString())
                                            .collect(Collectors.joining(","));
        LOG.warning(() -> "would cache<" + queueInfo + ">:" + entriesInfo);
    }

    @Override
    public List<MatchingEntry> get(QueueInfo serviceName)
    {
        return Collections.emptyList();
    }
}
