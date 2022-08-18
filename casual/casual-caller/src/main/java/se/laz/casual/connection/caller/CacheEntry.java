package se.laz.casual.connection.caller;

import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry;
import se.laz.casual.jca.DomainId;

import java.util.Objects;

public class CacheEntry implements Comparable<CacheEntry>
{
    private final ConnectionFactoryEntry connectionFactoryEntry;
    private final DomainId domainId;

    public CacheEntry(ConnectionFactoryEntry connectionFactoryEntry, DomainId domainId)
    {
        this.connectionFactoryEntry = connectionFactoryEntry;
        this.domainId = domainId;
    }

    public static CacheEntry of(DomainId domainId, ConnectionFactoryEntry connectionFactoryEntry)
    {
        Objects.requireNonNull(domainId, "domainId can not be null");
        Objects.requireNonNull(connectionFactoryEntry, "connectionFactoryEntry can not be null");
        return new CacheEntry(connectionFactoryEntry, domainId);
    }

    public ConnectionFactoryEntry getConnectionFactoryEntry()
    {
        return connectionFactoryEntry;
    }

    public DomainId getDomainId()
    {
        return domainId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CacheEntry that = (CacheEntry) o;
        return getConnectionFactoryEntry().equals(that.getConnectionFactoryEntry()) && getDomainId().equals(that.getDomainId());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getConnectionFactoryEntry(), getDomainId());
    }

    @Override
    public String toString()
    {
        return "CacheEntry{" +
                "connectionFactoryEntry=" + connectionFactoryEntry +
                ", domainId=" + domainId +
                '}';
    }

    @Override
    public int compareTo(CacheEntry cacheEntry)
    {
        if(getConnectionFactoryEntry().equals(cacheEntry.getConnectionFactoryEntry()))
        {
            return getDomainId().getId().compareTo(cacheEntry.getDomainId().getId());
        }
        return 1;
    }
}
