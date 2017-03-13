package se.kodarkatten.casual.network.io.readers;

import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.io.readers.utils.DynamicArrayIndexPair;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.DiscoveryRequestSizes;
import se.kodarkatten.casual.network.messages.request.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

    public static CasualDomainDiscoveryRequestMessage fromNetworkBytes(final List<byte[]> message)
    {
        Objects.requireNonNull(message, "byte[] is null");
        if(message.isEmpty())
        {
            throw new CasualTransportException("0 sized message");
        }
        if(1 == message.size())
        {
            return getMessage(message.get(0));
        }
        return getMessage(message);
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

    /**
     * Used when header payload > Integer.MAX_VALUE
     * @see CasualDomainDiscoveryRequestMessage::toNetworkBytesMultipleBuffers
     * to see how message should be structured
     **/
    private static CasualDomainDiscoveryRequestMessage getMessage(List<byte[]> message)
    {
        int currentIndex = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(message.get(currentIndex++));
        final UUID domainId = CasualNetworkReaderUtils.getAsUUID(message.get(currentIndex++));
        final ByteBuffer domainNameSizeBuffer = ByteBuffer.wrap(message.get(currentIndex++));
        final int domainNameSize = (int) domainNameSizeBuffer.getLong();
        final byte[] domainNameBytes = message.get(currentIndex++);
        if(domainNameBytes.length != domainNameSize)
        {
            throw new CasualTransportException("domainNameSize: " + domainNameSize + " but buffer has a length of " + domainNameBytes.length);
        }
        final String domainName = CasualNetworkReaderUtils.getAsString(domainNameBytes);
        final DynamicArrayIndexPair serviceNames = CasualNetworkReaderUtils.getDynamicArrayIndexPair(message, currentIndex, (item) -> CasualNetworkReaderUtils.getAsString(item));
        currentIndex = serviceNames.getIndex();
        final DynamicArrayIndexPair queueNames = CasualNetworkReaderUtils.getDynamicArrayIndexPair(message, currentIndex, (item) -> CasualNetworkReaderUtils.getAsString(item));
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

    // it will be used
    @SuppressWarnings("squid:S1172")
    private static CasualDomainDiscoveryRequestMessage readChunked(AsynchronousByteChannel channel)
    {
        return null;
    }

}
