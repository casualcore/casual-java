package se.kodarkatten.casual.network.messages.request;

import se.kodarkatten.casual.network.messages.parseinfo.DiscoveryRequestSizes;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by aleph on 2017-03-01.
 */
// We suppress this since the builder code is much cleaner using the private setters of the class
@SuppressWarnings("squid:S3398")
public final class CasualDomainDiscoveryRequestMessage
{
    private UUID execution;
    private UUID domainId;
    private String domainName;
    private List<String> serviceNames;
    private List<String> queueNames;
    // not part of the message
    // used for testing
    // so that we can get chunks without having to have a huge message
    // Defaults to Integer.MAX_VALUE
    private int maxMessageSize;

    private CasualDomainDiscoveryRequestMessage()
    {}

    public static CasualDomainDiscoveryRequestMessageBuilder createBuilder()
    {
        return new CasualDomainDiscoveryRequestMessageBuilder();
    }

    /**
     * May return several chunks since domainName, serviceNames and queueNames may all be of size Integer.MAX_VALUE
     * @return
     */
    public List<byte[]> toNetworkBytes()
    {
        final byte[] domainNameBytes = domainName.getBytes(StandardCharsets.UTF_8);
        final List<byte[]> serviceNameBytes = serviceNames.stream()
                                                          .map(s -> s.getBytes(StandardCharsets.UTF_8))
                                                          .collect(Collectors.toList());
        final List<byte[]> queueNameBytes  = queueNames.stream()
                                                          .map(s -> s.getBytes(StandardCharsets.UTF_8))
                                                          .collect(Collectors.toList());
        final long messageSize = DiscoveryRequestSizes.EXECUTION.getNetworkSize() + DiscoveryRequestSizes.DOMAIN_ID.getNetworkSize() +
                                 DiscoveryRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize() + domainNameBytes.length +
                                 DiscoveryRequestSizes.SERVICES_SIZE.getNetworkSize() +
                                 DiscoveryRequestSizes.SERVICES_ELEMENT_SIZE.getNetworkSize() * serviceNameBytes.size() + ByteUtils.sumNumberOfBytes(serviceNameBytes) +
                                 DiscoveryRequestSizes.QUEUES_SIZE.getNetworkSize() +
                                 DiscoveryRequestSizes.QUEUES_ELEMENT_SIZE.getNetworkSize() * serviceNameBytes.size() + ByteUtils.sumNumberOfBytes(queueNameBytes);

        return (messageSize <= maxMessageSize) ? toNetworkBytesFitsInOneBuffer((int)messageSize, domainNameBytes, serviceNameBytes, queueNameBytes)
                                                  : toNetworkBytesMultipleBuffers(domainNameBytes, serviceNameBytes, queueNameBytes);
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

    public long getNumberOfRequestedServicesToFollow()
    {
        return serviceNames.size();
    }

    public List<String> getServiceNames()
    {
        return serviceNames.stream()
                           .collect(Collectors.toList());
    }

    public long getNumberOfRequestedQueuesToFollow()
    {
        return queueNames.size();
    }

    public List<String> getQueueNames()
    {
        return queueNames.stream()
                         .collect(Collectors.toList());
    }

    private CasualDomainDiscoveryRequestMessage setMaxMessageSize(int maxMessageSize)
    {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    private CasualDomainDiscoveryRequestMessage setExecution(UUID execution)
    {
        this.execution = execution;
        return this;
    }

    private CasualDomainDiscoveryRequestMessage setDomainId(UUID domainId)
    {
        this.domainId = domainId;
        return this;
    }

    private CasualDomainDiscoveryRequestMessage setDomainName(String domainName)
    {
        this.domainName = domainName;
        return this;
    }

    private CasualDomainDiscoveryRequestMessage setServiceNames(List<String> serviceNames)
    {
        this.serviceNames = serviceNames.stream()
                                        .collect(Collectors.toList());
        return this;
    }

    private CasualDomainDiscoveryRequestMessage setQueueNames(List<String> queueNames)
    {
        this.queueNames = queueNames.stream()
                                    .collect(Collectors.toList());
        return this;
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
        CasualDomainDiscoveryRequestMessage that = (CasualDomainDiscoveryRequestMessage) o;
        return Objects.equals(execution, that.execution) &&
            Objects.equals(domainId, that.domainId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, domainId);
    }

    public static class CasualDomainDiscoveryRequestMessageBuilder
    {
        private UUID execution;
        private UUID domainId;
        private String domainName;
        private List<String> serviceNames;
        private List<String> queueNames;
        private int maxMessageSize = Integer.MAX_VALUE;

        public CasualDomainDiscoveryRequestMessageBuilder setMaxMessageSize(int maxMessageSize)
        {
            this.maxMessageSize = maxMessageSize;
            return this;
        }

        public CasualDomainDiscoveryRequestMessageBuilder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public CasualDomainDiscoveryRequestMessageBuilder setDomainId(UUID domainId)
        {
            this.domainId = domainId;
            return this;
        }

        public CasualDomainDiscoveryRequestMessageBuilder setDomainName(String domainName)
        {
            this.domainName = domainName;
            return this;
        }

        public CasualDomainDiscoveryRequestMessageBuilder setServiceNames(List<String> serviceNames)
        {
            this.serviceNames = serviceNames.stream()
                                            .collect(Collectors.toList());
            return this;
        }

        public CasualDomainDiscoveryRequestMessageBuilder setQueueNames(List<String> queueNames)
        {
            this.queueNames = queueNames.stream()
                                        .collect(Collectors.toList());
            return this;
        }

        public CasualDomainDiscoveryRequestMessage build()
        {
            return new CasualDomainDiscoveryRequestMessage()
                .setExecution(execution)
                .setDomainId(domainId)
                .setDomainName(domainName)
                .setServiceNames(serviceNames)
                .setQueueNames(queueNames)
                .setMaxMessageSize(maxMessageSize);
        }
    }

    private List<byte[]> toNetworkBytesMultipleBuffers(byte[] domainNameBytes, List<byte[]> serviceNameBytes, List<byte[]> queueNameBytes)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer executionBuffer = ByteBuffer.allocate(DiscoveryRequestSizes.EXECUTION.getNetworkSize());
        executionBuffer.putLong(execution.getMostSignificantBits())
                       .putLong(execution.getLeastSignificantBits());
        l.add(executionBuffer.array());
        ByteBuffer domainIdBuffer = ByteBuffer.allocate(DiscoveryRequestSizes.DOMAIN_ID.getNetworkSize());
        domainIdBuffer.putLong(domainId.getMostSignificantBits())
                      .putLong(domainId.getLeastSignificantBits());
        l.add(domainIdBuffer.array());
        ByteBuffer domainNameSizeBuffer = ByteBuffer.allocate(DiscoveryRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize());
        domainNameSizeBuffer.putLong(domainNameBytes.length);
        l.add(domainNameSizeBuffer.array());
        l.add(domainNameBytes);
        addNameBytes(l, serviceNameBytes, DiscoveryRequestSizes.SERVICES_SIZE.getNetworkSize(), DiscoveryRequestSizes.SERVICES_ELEMENT_SIZE.getNetworkSize());
        addNameBytes(l, queueNameBytes, DiscoveryRequestSizes.QUEUES_SIZE.getNetworkSize(), DiscoveryRequestSizes.QUEUES_ELEMENT_SIZE.getNetworkSize());
        return l;
    }

    private List<byte[]>  toNetworkBytesFitsInOneBuffer(int messageSize, byte[] domainNameBytes, List<byte[]> serviceNameBytes, List<byte[]> queueNameBytes)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        b.putLong(execution.getMostSignificantBits())
         .putLong(execution.getLeastSignificantBits())
         .putLong(domainId.getMostSignificantBits())
         .putLong(domainId.getLeastSignificantBits())
         .putLong(domainNameBytes.length)
         .put(domainNameBytes);
        addNameBytes(b, serviceNameBytes);
        addNameBytes(b, queueNameBytes);
        l.add(b.array());
        return l;
    }

    private void addNameBytes(ByteBuffer b, List<byte[]> data)
    {
        if(!data.isEmpty())
        {
            b.putLong(data.size());
            for(int i = 0; i < data.size(); ++i)
            {
                b.putLong(data.get(i).length);
                b.put(data.get(i));
            }
        }
    }

    private void addNameBytes(List<byte[]> l, List<byte[]> data, int headerSize, int elementSize)
    {
        if(!data.isEmpty())
        {
            ByteBuffer header = ByteBuffer.allocate(headerSize);
            header.putLong(data.size());
            l.add(header.array());
            for(int i = 0; i < data.size(); ++i)
            {
                ByteBuffer elementSizeBuffer = ByteBuffer.allocate(elementSize);
                final byte[] elementData = data.get(i);
                elementSizeBuffer.putLong(elementData.length);
                l.add(elementSizeBuffer.array());
                l.add(elementData);
            }
        }
    }

}
