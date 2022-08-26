/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.events;

import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry;
import se.laz.casual.jca.DomainId;

import java.util.Objects;

public class DomainGoneEvent
{
    private final ConnectionFactoryEntry connectionFactoryEntry;
    private final DomainId domainId;

    private DomainGoneEvent(ConnectionFactoryEntry connectionFactoryEntry, DomainId domainId)
    {
        this.connectionFactoryEntry = connectionFactoryEntry;
        this.domainId = domainId;
    }

    public static DomainGoneEvent of(ConnectionFactoryEntry connectionFactoryEntry, DomainId domainId)
    {
        Objects.requireNonNull(connectionFactoryEntry, "connectionFactoryEntry can not be null");
        Objects.requireNonNull(domainId, "domainId can not be null");
        return new DomainGoneEvent(connectionFactoryEntry, domainId);
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
        DomainGoneEvent that = (DomainGoneEvent) o;
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
        return "DomainGoneEvent{" +
                "connectionFactoryEntry=" + connectionFactoryEntry +
                ", domainId=" + domainId +
                '}';
    }
}
