package se.kodarkatten.casual.network.io.readers;

import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.io.readers.utils.DynamicArrayIndexPair;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.DiscoveryRequestSizes;
import se.kodarkatten.casual.network.messages.request.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.utils.ByteUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.Arrays;
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
public final class CasualDomainDiscoveryRequestMessageReader
{
    private static int maxSingleBufferByteSize = Integer.MAX_VALUE;
    private CasualDomainDiscoveryRequestMessageReader()
    {}

    /**
     * Number of maximum bytes before any chunk reading takes place
     * Defaults to Integer.MAX_VALUE
     * @return
     */
    public static int getMaxSingleBufferByteSize()
    {
        return maxSingleBufferByteSize;
    }

    /**
     * If not set, defaults to Integer.MAX_VALUE
     * Can be used in testing to force chunked reading
     * by for instance setting it to 1
     * @return
     */
    public static void setMaxSingleBufferByteSize(int maxSingleBufferByteSize)
    {
        CasualDomainDiscoveryRequestMessageReader.maxSingleBufferByteSize = maxSingleBufferByteSize;
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

    public static CasualDomainDiscoveryRequestMessage read(final AsynchronousByteChannel channel, long messageSize)
    {
        try
        {
            if (messageSize <= getMaxSingleBufferByteSize())
            {
                return readSingleBuffer(channel, (int) messageSize);
            }
            return readChunked(channel);
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading", e);
        }
    }

    private static CasualDomainDiscoveryRequestMessage readSingleBuffer(AsynchronousByteChannel channel, int messageSize) throws ExecutionException, InterruptedException
    {
        final CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        return getMessage(msgFuture.get().array());
    }

    private static CasualDomainDiscoveryRequestMessage readChunked(AsynchronousByteChannel channel)
    {
        throw new NotImplementedException();
    }

}
