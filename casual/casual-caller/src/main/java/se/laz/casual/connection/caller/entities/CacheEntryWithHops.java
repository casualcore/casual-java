/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.entities;

import se.laz.casual.jca.DomainId;

import java.util.Objects;

public class CacheEntryWithHops extends CacheEntry
{
    private long hops;
    private CacheEntryWithHops(ConnectionFactoryEntry connectionFactoryEntry, DomainId domainId, long hops)
    {
        super(connectionFactoryEntry, domainId);
        this.hops = hops;
    }

    public static CacheEntryWithHops of(ConnectionFactoryEntry connectionFactoryEntry, DomainId domainId, long hops)
    {
        Objects.requireNonNull(connectionFactoryEntry, "connectionFactoryEntry can not be null");
        Objects.requireNonNull(domainId, "domainId can not be null");
        return new CacheEntryWithHops(connectionFactoryEntry, domainId, hops);
    }

    public long getHops()
    {
        return hops;
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
        if (!super.equals(o))
        {
            return false;
        }
        CacheEntryWithHops that = (CacheEntryWithHops) o;
        return getHops() == that.getHops();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), getHops());
    }

    @Override
    public String toString()
    {
        return "CacheEntryWithHops{" +
                "connectionFactoryEntry=" + getConnectionFactoryEntry() +
                ", domainId=" + getDomainId() +
                ", hops=" + hops +
                '}';
    }
}
