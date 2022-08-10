package se.laz.casual.connection.caller;

import se.laz.casual.jca.DomainId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheEntry
{
    private final DomainId domainId;
    private final Map<String, MatchingEntry> services = new ConcurrentHashMap<>();
    private final Map<String, MatchingEntry> queues = new ConcurrentHashMap<>();

    private CacheEntry(DomainId domainId)
    {
        this.domainId = domainId;
    }



}
