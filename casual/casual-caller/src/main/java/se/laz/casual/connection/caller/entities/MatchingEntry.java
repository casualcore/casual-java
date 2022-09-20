/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.entities;

import se.laz.casual.api.queue.QueueDetails;
import se.laz.casual.api.service.ServiceDetails;
import se.laz.casual.jca.DomainId;

import java.util.List;
import java.util.Objects;

public class MatchingEntry
{
    private final ConnectionFactoryEntry connectionFactoryEntry;
    private final DomainId domainId;
    private final List<ServiceDetails> services;
    private final List<QueueDetails> queues;

    private MatchingEntry(ConnectionFactoryEntry connectionFactoryEntry, DomainId domainId, List<ServiceDetails> services, List<QueueDetails> queues)
    {
        this.connectionFactoryEntry = connectionFactoryEntry;
        this.domainId = domainId;
        this.services = services;
        this.queues = queues;
    }

    public static MatchingEntry of(ConnectionFactoryEntry connectionFactoryEntry, DomainId domainId, List<ServiceDetails> services, List<QueueDetails> queues)
    {
        Objects.requireNonNull(connectionFactoryEntry, "connectionFactoryEntry can not be null");
        Objects.requireNonNull(domainId, "domainId can not be null");
        Objects.requireNonNull(services, "services can not be null");
        Objects.requireNonNull(queues, "queues can not be null");
        return new MatchingEntry(connectionFactoryEntry, domainId, services, queues);
    }

    public ConnectionFactoryEntry getConnectionFactoryEntry()
    {
        return connectionFactoryEntry;
    }

    public DomainId getDomainId()
    {
        return domainId;
    }

    public List<ServiceDetails> getServices()
    {
        return services;
    }

    public List<QueueDetails> getQueues()
    {
        return queues;
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
        return getConnectionFactoryEntry().equals(that.getConnectionFactoryEntry()) && getDomainId().equals(that.getDomainId()) && getServices().equals(that.getServices()) && getQueues().equals(that.getQueues());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getConnectionFactoryEntry(), getDomainId(), getServices(), getQueues());
    }

    @Override
    public String toString()
    {
        return "MatchingEntry{" +
                "connectionFactoryEntry=" + connectionFactoryEntry +
                ", domainId=" + domainId +
                ", services=" + services +
                ", queues=" + queues +
                '}';
    }
}
