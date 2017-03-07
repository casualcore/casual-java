package se.kodarkatten.casual.network.io.readers;

import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.io.readers.utils.DynamicArrayIndexPair;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.DiscoveryRequestSizes;
import se.kodarkatten.casual.network.messages.request.CasualDomainDiscoveryRequestMessage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


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
    private CasualDomainDiscoveryRequestMessageReader()
    {}

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

    // Used when header payload > Integer.MAX_VALUE
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


}
