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
    private final List<String> services;
    private final List<String> queues;
    private CasualRequestInfo(DomainId domainId, List<String> services, List<String> queues)
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

    public static CasualRequestInfo of(List<ServiceInfo> services, List<QueueInfo> queues)
    {
        List<String> serviceNames = null == services ? null : services.stream()
                                                                      .map(s -> s.getServiceName())
                                                                      .collect(Collectors.toList());
        List<String> queueNames = null == queues ? null : queues.stream()
                                                                .map(q -> q.getQueueName())
                                                                .collect(Collectors.toList());
        return new CasualRequestInfo(null, serviceNames, queueNames);
    }

    public Optional<DomainId> getDomainId()
    {
        return Optional.ofNullable(domainId);
    }

    public List<String> getServices()
    {
        return null == services ? Collections.emptyList() : Collections.unmodifiableList(services);
    }

    public List<String> getQueues()
    {
        return null == queues ? Collections.emptyList() : Collections.unmodifiableList(queues);
    }

    public CasualRequestInfo addDomainId(DomainId domainId)
    {
        return new CasualRequestInfo(domainId, getServices(), getQueues());
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
