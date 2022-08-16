package se.laz.casual.connection.caller.logic;

import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.MatchingEntry;
import se.laz.casual.connection.caller.Pool;
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
        List<MatchingEntry> matchingEntries = new ArrayList<>();
        CasualRequestInfo requestInfo = CasualRequestInfo.of(services, queues);
        LOG.warning(() -> "requestinfo: " + requestInfo);
        pools.forEach(pool -> {
            List<MatchingEntry> maybeMatching = matches(requestInfo, pool.getConnectionFactoryEntry(), pool.getDomainIds());
            matchingEntries.addAll(maybeMatching);
        });
        String entriesString = matchingEntries.stream()
                                              .map(v -> v.toString())
                                              .collect(Collectors.joining(","));
        LOG.warning(() -> "# of matching entries: " + matchingEntries.size() + "\n values: " + entriesString);
        return matchingEntries;
    }

    //Note: due to try with resources usage where we never use the resource
    @SuppressWarnings("try")
    private List<MatchingEntry> matches(CasualRequestInfo requestInfo, ConnectionFactoryEntry connectionFactoryEntry, List<DomainId> poolDomainIds)
    {
        List<MatchingEntry> entries = new ArrayList<>();
        for(DomainId domainId : poolDomainIds)
        {
            ConnectionRequestInfo domainIdRequestInfo = CasualRequestInfo.of(domainId);
            try(CasualConnection connection = connectionFactoryEntry.getConnectionFactory().getConnection(domainIdRequestInfo))
            {
                DiscoveryReturn discoveryReturn = connection.discover(UUID.randomUUID(), requestInfo.getServices(), requestInfo.getQueues());
                LOG.warning(() -> "discoveryReturn:" + discoveryReturn);
                entries.add(MatchingEntry.of(connectionFactoryEntry, domainId, discoveryReturn.getServiceDetails(), discoveryReturn.getQueueDetails()));
            }
            catch (ResourceException resourceException)
            {
                // NOP
            }
        }
        return entries;
    }


}
