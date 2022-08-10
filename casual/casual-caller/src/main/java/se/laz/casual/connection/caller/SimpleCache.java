package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;

import java.util.List;

public interface SimpleCache
{
    void store(ServiceInfo serviceName, List<MatchingEntry> matchingEntries);
    List<MatchingEntry> get(ServiceInfo serviceName);
    void store(QueueInfo serviceName, List<MatchingEntry> matchingEntries);
    List<MatchingEntry> get(QueueInfo serviceName);
}
