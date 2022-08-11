package se.laz.casual.connection.caller;

import se.laz.casual.jca.ConnectionObserver;
import se.laz.casual.jca.DomainId;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface PoolDomainIdGenerator
{
    Map<ConnectionFactoryEntry, List<DomainId>> apply(List<ConnectionFactoryEntry> entries, ConnectionObserver observer);
}
