/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.discovery;

import se.laz.casual.api.CasualDiscoveryApi;
import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.queue.QueueDetails;
import se.laz.casual.api.service.ServiceDetails;
import se.laz.casual.api.util.PrettyPrinter;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.info.CasualInfo;
import se.laz.casual.info.Connection;
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.domain.Queue;
import se.laz.casual.network.protocol.messages.domain.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_4;

public class CasualDiscoveryCaller implements CasualDiscoveryApi
{

    private static final Logger LOG = Logger.getLogger(CasualDiscoveryCaller.class.getName());
    private CasualManagedConnection connection;

    private CasualDiscoveryCaller(CasualManagedConnection connection)
    {
        this.connection = connection;
    }

    public static CasualDiscoveryCaller of(CasualManagedConnection managedConnection)
    {
        Objects.requireNonNull(managedConnection, "managedConnection can not be null");
        return new CasualDiscoveryCaller(managedConnection);
    }

    @Override
    public DiscoveryReturn discover(UUID corrid, List<String> serviceNames, List<String> queueNames)
    {
        LOG.finest(() -> "issuing domain discovery, corrid: " + PrettyPrinter.casualStringify(corrid) + " service names: " + serviceNames + " queue names: " + queueNames);

        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_ID ).getId())
                                                                                            .setDomainName(ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_NAME ))
                                                                                            .setServiceNames(serviceNames)
                                                                                            .setQueueNames(queueNames)
                                                                                            .build();
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessageImpl.of(corrid, requestMsg);
        CompletableFuture<CasualNWMessage<CasualDomainDiscoveryReplyMessage>> replyMsgFuture = connection.getNetworkConnection().request(msg);

        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = replyMsgFuture.join();
        LOG.finest(() -> "domain discovery ok for corrid: " + PrettyPrinter.casualStringify(corrid) + "reply -> service names: " + serviceNames + " queue names: " + queueNames);
        DiscoveryReturn discoveryReturn = toDiscoveryReturn( replyMsg.getMessage(), connection.getNetworkConnection().getProtocolVersion() );
        // Add discovered service to local storage:
        CasualInfo.getInstance().addDiscovery(discoveryReturn.getServiceDetails(), new Connection.Builder()
                        .domainId( connection.getNetworkConnection().getDomainId() )
                        .protocolVersion( connection.getNetworkConnection().getProtocolVersion() )
                        .hostName( connection.getManagedConnectionFactory().getHostName() )
                        .portNumber( connection.getManagedConnectionFactory().getPortNumber() )
                .build() );
        return discoveryReturn;
    }

    private DiscoveryReturn toDiscoveryReturn(CasualDomainDiscoveryReplyMessage message, ProtocolVersion protocolVersion)
    {
        DiscoveryReturn.Builder builder = DiscoveryReturn.createBuilder();
        message.getQueues()
               .stream()
               .map((Queue queue) -> toQueueDetails(queue, protocolVersion))
               .forEach(builder::addQueueDetails);
        message.getServices()
               .stream()
               .map(this::toServiceDetails)
               .forEach(builder::addServiceDetails);
        return builder.build();
    }

    private QueueDetails toQueueDetails(Queue queue, ProtocolVersion protocolVersion)
    {
        return protocolVersion.isGreaterThanOrEqualTo( VERSION_1_4 )
                ? QueueDetails.createBuilder()
                              .withName(queue.getName())
                              .withRetries(queue.getRetries())
                              .withProtocolVersion(protocolVersion)
                              .withRetryDelay(queue.getRetryDelay())
                              .withEnqueueEnabled(queue.isEnqueueEnabled())
                              .withDequeueEnabled(queue.isDequeueEnabled())
                              .build()
                : QueueDetails.createBuilder()
                              .withName(queue.getName())
                              .withRetries(queue.getRetries())
                              .withProtocolVersion(protocolVersion)
                              .build();
    }

    private ServiceDetails toServiceDetails(Service service)
    {
        return ServiceDetails.createBuilder()
                             .withName(service.getName())
                             .withCategory(service.getCategory())
                             .withTransactionType(service.getTransactionType())
                             .withTimeout(service.getTimeout())
                             .withHops(service.getHops())
                             .build();
    }
}
