package se.laz.casual.connection.caller;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.entities.MatchingEntry;

import java.util.List;
import java.util.Optional;

public interface Cache
{
    void store(List<MatchingEntry> matchingEntries);
    List<MatchingEntry> get(ServiceInfo serviceInfo);
    Optional<MatchingEntry> get(QueueInfo queueInfo);
}
