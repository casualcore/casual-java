package se.laz.casual.connection.caller;

import se.laz.casual.jca.DomainId;

import java.util.Objects;

public class MatchingEntry
{
    private final ConnectionFactoryEntry connectionFactoryEntry;
    private final DomainId domainId;

    private MatchingEntry(ConnectionFactoryEntry connectionFactoryEntry, DomainId domainId)
    {
        this.connectionFactoryEntry = connectionFactoryEntry;
        this.domainId = domainId;
    }

    public static MatchingEntry of(ConnectionFactoryEntry connectionFactoryEntry, DomainId domainId)
    {
        Objects.requireNonNull(connectionFactoryEntry, "connectionFactoryEntry can not be null");
        Objects.requireNonNull(domainId, "domainId can not be null");
        return new MatchingEntry(connectionFactoryEntry, domainId);
    }

    public ConnectionFactoryEntry getConnectionFactoryEntry()
    {
        return connectionFactoryEntry;
    }

    public DomainId getDomainId()
    {
        return domainId;
    }

    public boolean validate()
    {
        return getConnectionFactoryEntry().validate(domainId);
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
        MatchingEntry that = (MatchingEntry) o;
        return Objects.equals(getConnectionFactoryEntry(), that.getConnectionFactoryEntry()) && Objects.equals(getDomainId(), that.getDomainId());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getConnectionFactoryEntry(), getDomainId());
    }

    @Override
    public String toString()
    {
        return "MatchingEntry{" +
                "connectionFactoryEntry=" + connectionFactoryEntry +
                ", domainId=" + domainId +
                '}';
    }
}
