package se.kodarkatten.casual.network.messages.reply;

import se.kodarkatten.casual.network.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.parseinfo.DiscoveryReplySizes;
import se.kodarkatten.casual.network.messages.queue.Queue;
import se.kodarkatten.casual.network.messages.service.Service;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by aleph on 2017-03-07.
 */

/**
 * Sonarqube should pick up on source/target compatibility as java 8 but it does not, remove this when it does
 */
@SuppressWarnings("squid:S1612")
public final class CasualDomainDiscoveryReplyMessage implements CasualNetworkTransmittable
{
    private static final CasualNWMessageType type = CasualNWMessageType.DOMAIN_DISCOVERY_REPLY;
    private final UUID execution;
    private final UUID domainId;
    private final String domainName;
    private List<Service> services = new ArrayList<>();
    private List<Queue> queues = new ArrayList<>();

    // not part of the message
    // used for testing
    // so that we can get chunks without having to have a huge message
    // Defaults to Integer.MAX_VALUE
    private int maxMessageSize = Integer.MAX_VALUE;

    private CasualDomainDiscoveryReplyMessage(final UUID execution, final UUID domainId, final String domainName)
    {
        this.execution = execution;
        this.domainId = domainId;
        this.domainName = domainName;
    }

    public static CasualDomainDiscoveryReplyMessage of(final UUID execution, final UUID domainId, final String domainName)
    {
        return new CasualDomainDiscoveryReplyMessage(execution, domainId, domainName);
    }

    @Override
    public CasualNWMessageType getType()
    {
        return type;
    }

    public CasualDomainDiscoveryReplyMessage setServices(List<Service> services)
    {
        this.services = services;
        return this;
    }

    public CasualDomainDiscoveryReplyMessage setQueues(List<Queue> queues)
    {
        this.queues = queues;
        return this;
    }

    public CasualDomainDiscoveryReplyMessage setMaxMessageSize(int maxMessageSize)
    {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    public UUID getExecution()
    {
        return execution;
    }

    public UUID getDomainId()
    {
        return domainId;
    }

    public String getDomainName()
    {
        return domainName;
    }

    public List<Service> getServices()
    {
        return services.stream()
                       .collect(Collectors.toList());
    }

    public List<Queue> getQueues()
    {
        return queues.stream()
                     .collect(Collectors.toList());
    }

    public int getMaxMessageSize()
    {
        return maxMessageSize;
    }

    /**
     * May return several chunks since services and queues may be legion
     * @return
     */
    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] domainNameBytes = domainName.getBytes(StandardCharsets.UTF_8);
        final List<byte[]> serviceBytes = services.stream()
                                                  .map(s -> s.toNetworkBytes())
                                                  .reduce(new ArrayList<>(), (s1, s2) -> { s1.addAll(s2); return s1;} );
        final List<byte[]> queueBytes = queues.stream()
                                              .map(s -> s.toNetworkBytes())
                                              .reduce(new ArrayList<>(), (s1, s2) -> { s1.addAll(s2); return s1;} );

        final long messageSize = DiscoveryReplySizes.EXECUTION.getNetworkSize() + DiscoveryReplySizes.DOMAIN_ID.getNetworkSize() +
                                 DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize() + domainNameBytes.length +
                                 DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes) +
                                 DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(queueBytes);

        return (messageSize <= maxMessageSize) ? toNetworkBytesFitsInOneBuffer((int)messageSize, domainNameBytes, serviceBytes, queueBytes)
                                               : toNetworkBytesMultipleBuffers(domainNameBytes, serviceBytes, queueBytes);
    }

    private List<byte[]> toNetworkBytesFitsInOneBuffer(int messageSize, byte[] domainNameBytes, List<byte[]> serviceBytes, List<byte[]> queueBytes)
    {
        final List<byte[]> l = new ArrayList<>();
        final ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualNetworkWriterUtils.writeUUID(execution, b);
        CasualNetworkWriterUtils.writeUUID(domainId, b);
        b.putLong(domainNameBytes.length);
        b.put(domainNameBytes);
        b.putLong(services.size());
        serviceBytes.stream()
                    .forEach(s -> b.put(s));
        b.putLong(queues.size());
        queueBytes.stream()
                  .forEach(s -> b.put(s));
        l.add(b.array());
        return l;
    }

    private List<byte[]> toNetworkBytesMultipleBuffers(byte[] domainNameBytes, List<byte[]> serviceBytes, List<byte[]> queueBytes)
    {
        final List<byte[]> l = new ArrayList<>();
        ByteBuffer executionBuffer = ByteBuffer.allocate(DiscoveryReplySizes.EXECUTION.getNetworkSize());
        CasualNetworkWriterUtils.writeUUID(execution, executionBuffer);
        l.add(executionBuffer.array());
        ByteBuffer domainIdBuffer = ByteBuffer.allocate(DiscoveryReplySizes.DOMAIN_ID.getNetworkSize());
        CasualNetworkWriterUtils.writeUUID(domainId, domainIdBuffer);
        l.add(domainIdBuffer.array());
        ByteBuffer domainNameSizeBuffer = ByteBuffer.allocate(DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize());
        domainNameSizeBuffer.putLong(domainNameBytes.length);
        l.add(domainNameSizeBuffer.array());
        l.add(domainNameBytes);

        final ByteBuffer numberOfServicesBuffer = ByteBuffer.allocate(DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize());
        numberOfServicesBuffer.putLong(services.size());
        l.add(numberOfServicesBuffer.array());
        l.addAll(serviceBytes);

        final ByteBuffer numberOfQueuesBuffer = ByteBuffer.allocate(DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize());
        numberOfQueuesBuffer.putLong(queues.size());
        l.add(numberOfQueuesBuffer.array());
        l.addAll(queueBytes);
        return l;
    }

    // This is absolutely fine here
    @SuppressWarnings("squid:S1067")
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
        CasualDomainDiscoveryReplyMessage that = (CasualDomainDiscoveryReplyMessage) o;
        return Objects.equals(execution, that.execution) &&
            Objects.equals(domainId, that.domainId) &&
            Objects.equals(domainName, that.domainName) &&
            Objects.equals(services, that.services) &&
            Objects.equals(queues, that.queues);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, domainId, domainName, services, queues);
    }
}
