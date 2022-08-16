package se.laz.casual.connection.caller;

import se.laz.casual.jca.DomainId;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Pool
{
    private final ConnectionFactoryEntry connectionFactoryEntry;
    private final List<DomainId> domainIds;

    private Pool(ConnectionFactoryEntry connectionFactoryEntry, List<DomainId> domainIds)
    {
        this.connectionFactoryEntry = connectionFactoryEntry;
        this.domainIds = domainIds;
    }

    public static Pool of(ConnectionFactoryEntry connectionFactoryEntry, List<DomainId> domainIds)
    {
        Objects.requireNonNull(connectionFactoryEntry, "connectionFactoryEntry can not be null");
        Objects.requireNonNull(domainIds, "domainIds can not be null");
        return new Pool(connectionFactoryEntry, domainIds.stream().collect(Collectors.toList()));
    }

    public ConnectionFactoryEntry getConnectionFactoryEntry()
    {
        return connectionFactoryEntry;
    }

    public List<DomainId> getDomainIds()
    {
        return domainIds;
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
        Pool pool = (Pool) o;
        return getConnectionFactoryEntry().equals(pool.getConnectionFactoryEntry()) && getDomainIds().equals(pool.getDomainIds());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getConnectionFactoryEntry(), getDomainIds());
    }

    @Override
    public String toString()
    {
        return "Pool{" +
                "connectionFactoryEntry=" + connectionFactoryEntry +
                ", domainIds=" + domainIds +
                '}';
    }
}
