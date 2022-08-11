package se.laz.casual.connection.caller.logic;

import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.MatchingEntry;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualRequestInfo;
import se.laz.casual.jca.DomainId;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ConnectionFactoryMatcher
{
    private static final Logger LOG = Logger.getLogger(ConnectionFactoryMatcher.class.getName());

    public List<MatchingEntry> matchService(ServiceInfo serviceInfo, Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds)
    {
        return match(Arrays.asList(serviceInfo), Collections.emptyList(), poolDomainIds);
    }

    public List<MatchingEntry> matchQueue(QueueInfo queueInfo, Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds)
    {
        return match(Collections.emptyList(), Arrays.asList(queueInfo), poolDomainIds);
    }

    public List<MatchingEntry> match(List<ServiceInfo> services, List<QueueInfo> queues,  Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds)
    {
        List<MatchingEntry> matchingEntries = new ArrayList<>();
        CasualRequestInfo requestInfo = CasualRequestInfo.of(services, queues);
        poolDomainIds.forEach((connectionFactoryEntry, domainIds) -> {
            List<MatchingEntry> maybeMatching = matches(requestInfo, connectionFactoryEntry, domainIds);
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
