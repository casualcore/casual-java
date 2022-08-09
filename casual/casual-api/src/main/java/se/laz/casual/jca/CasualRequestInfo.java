/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;

import javax.resource.spi.ConnectionRequestInfo;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CasualRequestInfo implements ConnectionRequestInfo
{
    private final DomainId domainId;
    private final List<ServiceInfo> services;
    private final List<QueueInfo> queues;
    private CasualRequestInfo(DomainId domainId, List<ServiceInfo> services, List<QueueInfo> queues)
    {
        this.domainId = domainId;
        this.services = services;
        this.queues = queues;
    }

    public static ConnectionRequestInfo of(DomainId domainId)
    {
        Objects.requireNonNull(domainId, "domainId can not be null");
        return new CasualRequestInfo(domainId, null, null);
    }

    public static ConnectionRequestInfo of(List<ServiceInfo> services, List<QueueInfo> queues)
    {
        return new CasualRequestInfo(null, services, queues);
    }

    public Optional<DomainId> getDomainId()
    {
        return Optional.ofNullable(domainId);
    }

    public List<String> getServices()
    {
        return null == services ? Collections.emptyList() : services.stream()
                                                                    .map(s -> s.getServiceName())
                                                                    .collect(Collectors.toList());
    }

    public List<String> getQueues()
    {
        return null == queues ? Collections.emptyList() : queues.stream()
                                                                .map(q -> q.getQueueName())
                                                                .collect(Collectors.toList());
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
        CasualRequestInfo that = (CasualRequestInfo) o;
        return Objects.equals(getDomainId(), that.getDomainId()) && Objects.equals(getServices(), that.getServices()) && Objects.equals(getQueues(), that.getQueues());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getDomainId(), getServices(), getQueues());
    }

    @Override
    public String toString()
    {
        return "CasualRequestInfo{" +
                "domainId=" + domainId +
                ", services=" + services +
                ", queues=" + queues +
                '}';
    }
}
