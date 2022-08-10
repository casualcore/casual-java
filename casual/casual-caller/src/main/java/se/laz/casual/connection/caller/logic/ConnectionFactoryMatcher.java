package se.laz.casual.connection.caller.logic;

import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.MatchingEntry;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualRequestInfo;
import se.laz.casual.jca.DomainId;

import javax.resource.ResourceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ConnectionFactoryMatcher
{
    private static final Logger LOG = Logger.getLogger(ConnectionFactoryMatcher.class.getName());

    public List<MatchingEntry> matchService(ServiceInfo serviceInfo, List<ConnectionFactoryEntry> connectionFactoryEntries, Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds)
    {
        return match(Arrays.asList(serviceInfo.getServiceName()), Collections.emptyList(), connectionFactoryEntries, poolDomainIds);
    }

    public List<MatchingEntry> matchQueue(QueueInfo queueInfo, List<ConnectionFactoryEntry> connectionFactoryEntries, Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds)
    {
        return match(Collections.emptyList(), Arrays.asList(queueInfo.getQueueName()), connectionFactoryEntries, poolDomainIds);
    }

    public List<MatchingEntry> match(List<String> services, List<String> queues, List<ConnectionFactoryEntry> connectionFactoryEntries, Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds)
    {
        List<MatchingEntry> matchingEntries = new ArrayList<>();
        List<ServiceInfo> serviceInfo = services.stream()
                                                .map(name -> ServiceInfo.of(name))
                                                .collect(Collectors.toList());
        List<QueueInfo> queueInfo = queues.stream()
                                          .map(name -> QueueInfo.createBuilder()
                                                                .withQueueName(name)
                                                                .build())
                                          .collect(Collectors.toList());
        CasualRequestInfo requestInfo = CasualRequestInfo.of(serviceInfo, queueInfo);
        for(ConnectionFactoryEntry entry : connectionFactoryEntries)
        {
            List<MatchingEntry> maybeMatching = matches(requestInfo, entry, poolDomainIds.get(entry));
            matchingEntries.addAll(maybeMatching);
        }
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
            CasualRequestInfo addendum = requestInfo.addDomainId(domainId);
            try(CasualConnection connection = connectionFactoryEntry.getConnectionFactory().getConnection(addendum))
            {
                entries.add(MatchingEntry.of(connectionFactoryEntry, domainId));
            }
            catch (ResourceException resourceException)
            {
                // NOP
            }
        }
        return entries;
    }


}
