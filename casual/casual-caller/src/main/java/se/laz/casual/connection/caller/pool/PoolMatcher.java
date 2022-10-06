/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.pool;

import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.api.queue.QueueDetails;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceDetails;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.entities.MatchingEntry;
import se.laz.casual.connection.caller.entities.Pool;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualRequestInfo;
import se.laz.casual.jca.DomainId;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PoolMatcher
{
    private static final Logger LOG = Logger.getLogger(PoolMatcher.class.getName());

    public List<MatchingEntry> match(ServiceInfo serviceInfo, List<Pool> pools)
    {
        return match(Arrays.asList(serviceInfo), Collections.emptyList(), pools);
    }

    public List<MatchingEntry> match(QueueInfo queueInfo, List<Pool> pools)
    {
        return match(Collections.emptyList(), Arrays.asList(queueInfo), pools);
    }

    public List<MatchingEntry> match(List<ServiceInfo> services, List<QueueInfo> queues,  List<Pool> pools)
    {
        if(services.isEmpty() && queues.isEmpty())
        {
            return Collections.emptyList();
        }
        List<MatchingEntry> matchingEntries = new ArrayList<>();
        pools.forEach(pool -> {
            List<MatchingEntry> maybeMatching = matches(services, queues, pool.getConnectionFactoryEntry(), pool.getDomainIds());
            matchingEntries.addAll(maybeMatching);
        });
        String entriesString = matchingEntries.stream()
                                              .map(MatchingEntry::toString)
                                              .collect(Collectors.joining(","));
        LOG.finest(() -> "# of matching entries: " + matchingEntries.size() + "\n values: " + entriesString);
        return matchingEntries;
    }

    private List<MatchingEntry> matches(List<ServiceInfo> services, List<QueueInfo> queues, ConnectionFactoryEntry connectionFactoryEntry, List<DomainId> poolDomainIds)
    {
        List<MatchingEntry> entries = new ArrayList<>();
        for(DomainId domainId : poolDomainIds)
        {
            ConnectionRequestInfo domainIdRequestInfo = CasualRequestInfo.of(domainId);
            try(CasualConnection connection = connectionFactoryEntry.getConnectionFactory().getConnection(domainIdRequestInfo))
            {
                DiscoveryReturn discoveryReturn = connection.discover(
                        UUID.randomUUID(),
                        services.stream()
                                .map(ServiceInfo::getServiceName)
                                .collect(Collectors.toList()),
                        queues.stream()
                              .map(QueueInfo::getQueueName)
                              .collect(Collectors.toList()));
                LOG.finest(() -> "discoveryReturn:" + discoveryReturn);
                if(matchedSomething(discoveryReturn.getQueueDetails(), discoveryReturn.getServiceDetails()))
                {
                    entries.add(MatchingEntry.of(connectionFactoryEntry, domainId, discoveryReturn.getServiceDetails(), discoveryReturn.getQueueDetails()));
                }
            }
            catch (ResourceException resourceException)
            {
                //NOP
            }
        }
        return entries;
    }

    private boolean matchedSomething(List<QueueDetails> queueDetails, List<ServiceDetails> serviceDetails)
    {
        return !queueDetails.isEmpty() || !serviceDetails.isEmpty();
    }
}


