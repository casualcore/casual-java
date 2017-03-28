package se.kodarkatten.casual.network.io.readers;

import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.io.readers.utils.DynamicArrayIndexPair;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.DiscoveryRequestSizes;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * Created by aleph on 2017-03-02.
 */
/**
 * sonar hates lambdas...
 * It should pick up
 * sourceCompatibility = "1.8"
 * targetCompatibility = "1.8"
 * but it seems it does not
 **/

@SuppressWarnings({"squid:S1612", "squid:S1611"})
public final class CasualDomainDiscoveryRequestMessageReader implements Readable<CasualDomainDiscoveryRequestMessage>
{
    private CasualDomainDiscoveryRequestMessageReader()
    {}

    public static Readable<CasualDomainDiscoveryRequestMessage> of()
    {
        return new CasualDomainDiscoveryRequestMessageReader();
    }

    public CasualDomainDiscoveryRequestMessage readSingleBuffer(AsynchronousByteChannel channel, int messageSize)
    {
        final CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return getMessage(msgFuture.get().array());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

    public CasualDomainDiscoveryRequestMessage readChunked(AsynchronousByteChannel channel)
    {
        try
        {
            final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
            final UUID domainId = CasualNetworkReaderUtils.readUUID(channel);
            final int domainNameSize = (int) ByteUtils.readFully(channel, DiscoveryRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).get().getLong();
            final String domainName = CasualNetworkReaderUtils.readString(channel, domainNameSize);
            final List<String> services = readServices(channel);
            final List<String> queues = readQueues(channel);
            return CasualDomainDiscoveryRequestMessage.createBuilder()
                                                      .setExecution(execution)
                                                      .setDomainId(domainId)
                                                      .setDomainName(domainName)
                                                      .setServiceNames(services)
                                                      .setQueueNames(queues)
                                                      .build();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

    private static CasualDomainDiscoveryRequestMessage getMessage(byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, DiscoveryRequestSizes.EXECUTION.getNetworkSize()));
        currentOffset +=  DiscoveryRequestSizes.EXECUTION.getNetworkSize();
        final UUID domainId = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DiscoveryRequestSizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += DiscoveryRequestSizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , DiscoveryRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualNetworkReaderUtils.getAsString(bytes, currentOffset, domainNameSize);
        currentOffset += domainNameSize;
        final DynamicArrayIndexPair serviceNames = CasualNetworkReaderUtils.getDynamicArrayIndexPair(bytes, currentOffset, DiscoveryRequestSizes.SERVICES_SIZE.getNetworkSize(), DiscoveryRequestSizes.SERVICES_ELEMENT_SIZE.getNetworkSize(),
            (data, offset, elementSize) -> CasualNetworkReaderUtils.getAsString(data, offset, elementSize));
        currentOffset = serviceNames.getIndex();
        final DynamicArrayIndexPair queueNames = CasualNetworkReaderUtils.getDynamicArrayIndexPair(bytes, currentOffset, DiscoveryRequestSizes.QUEUES_SIZE.getNetworkSize(), DiscoveryRequestSizes.QUEUES_ELEMENT_SIZE.getNetworkSize(),
            (data, offset, elementSize) -> CasualNetworkReaderUtils.getAsString(data, offset, elementSize));
        return CasualDomainDiscoveryRequestMessage.createBuilder()
                                                  .setExecution(execution)
                                                  .setDomainId(domainId)
                                                  .setDomainName(domainName)
                                                  .setServiceNames(serviceNames.getBytes())
                                                  .setQueueNames(queueNames.getBytes())
                                                  .build();
    }

    private static List<String> readQueues(AsynchronousByteChannel channel) throws ExecutionException, InterruptedException
    {
        final List<String> queues = new ArrayList<>();
        final long numberOfQueues = ByteUtils.readFully(channel, DiscoveryRequestSizes.QUEUES_SIZE.getNetworkSize()).get().getLong();
        for(int i = 0; i < numberOfQueues; ++i)
        {
            queues.add(CasualNetworkReaderUtils.readString(channel));
        }
        return queues;
    }

    private static List<String> readServices(final AsynchronousByteChannel channel) throws ExecutionException, InterruptedException
    {
        final long numberOfServices = ByteUtils.readFully(channel, DiscoveryRequestSizes.SERVICES_SIZE.getNetworkSize()).get().getLong();
        final List<String> services = new ArrayList<>();
        for(int i = 0; i < numberOfServices; ++i)
        {
            services.add(CasualNetworkReaderUtils.readString(channel));
        }
        return services;
    }
}
