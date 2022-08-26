/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.entities;

import se.laz.casual.jca.DomainId;

import java.util.Objects;

public class CacheEntry
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

    public DomainId getDomainId()
    {
        return domainId;
    }

    public ConnectionFactoryEntry getConnectionFactoryEntry()
    {
        return connectionFactoryEntry;
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

}
