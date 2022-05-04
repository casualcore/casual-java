package se.laz.casual.jca.discovery;

import se.laz.casual.api.CasualDiscoveryApi;
import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.queue.QueueDetails;
import se.laz.casual.api.service.ServiceDetails;
import se.laz.casual.api.util.PrettyPrinter;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Domain;
import se.laz.casual.jca.CasualManagedConnection;
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
        Domain domain = ConfigurationService.getInstance().getConfiguration().getDomain();
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(domain.getId())
                                                                                            .setDomainName(domain.getName())
                                                                                            .setServiceNames(serviceNames)
                                                                                            .setQueueNames(queueNames)
                                                                                            .build();
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessageImpl.of(corrid, requestMsg);
        CompletableFuture<CasualNWMessage<CasualDomainDiscoveryReplyMessage>> replyMsgFuture = connection.getNetworkConnection().request(msg);

        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = replyMsgFuture.join();
        LOG.finest(() -> "domain discovery ok for corrid: " + PrettyPrinter.casualStringify(corrid) + "reply -> service names: " + serviceNames + " queue names: " + queueNames);
        return toDiscoveryReturn(replyMsg.getMessage());
    }

    private DiscoveryReturn toDiscoveryReturn(CasualDomainDiscoveryReplyMessage message)
    {
        DiscoveryReturn.Builder builder = DiscoveryReturn.createBuilder();
        message.getQueues()
               .stream()
               .map(this::toQueueDetails)
               .forEach(builder::addQueueDetails);
        message.getServices()
               .stream()
               .map(this::toServiceDetails)
               .forEach(builder::addServiceDetails);
        return builder.build();
    }

    private QueueDetails toQueueDetails(Queue queue)
    {
        return QueueDetails.of(queue.getName(), queue.getRetries());
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
