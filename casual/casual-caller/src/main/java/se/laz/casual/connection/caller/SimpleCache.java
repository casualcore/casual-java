package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.jca.ConnectionObserver;
import se.laz.casual.jca.DomainId;

import java.util.List;
import java.util.Map;

public interface SimpleCache extends ConnectionObserver
{
    void store(List<MatchingEntry> matchingEntries);
    List<MatchingEntry> get(ServiceInfo serviceInfo);
    List<MatchingEntry> get(QueueInfo queueInfo);
    void store(Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds);
    List<ConnectionFactoryEntry> getConnectionFactoryEntriesForLostDomain();
    Map<ConnectionFactoryEntry, List<DomainId>> getCurrentDomainIds();
    Map<ConnectionFactoryEntry, List<DomainId>> handleLostDomains(PoolDomainIdGenerator generator);
}
