package se.kodarkatten.casual.network.io.readers.domain;

import se.kodarkatten.casual.network.io.readers.NetworkReader;
import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.io.readers.utils.DynamicArrayIndexPair;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.DiscoveryReplySizes;
import se.kodarkatten.casual.network.messages.domain.Queue;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.domain.Service;
import se.kodarkatten.casual.network.messages.domain.TransactionType;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-03-08.
 */
public final class CasualDomainDiscoveryReplyMessageReader implements NetworkReader<CasualDomainDiscoveryReplyMessage>
{
    private CasualDomainDiscoveryReplyMessageReader()
    {}

    public static NetworkReader<CasualDomainDiscoveryReplyMessage> of()
    {
        return new CasualDomainDiscoveryReplyMessageReader();
    }

    @Override
    public CasualDomainDiscoveryReplyMessage readSingleBuffer(final AsynchronousByteChannel channel, int messageSize)
    {
        final CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return readMessage(msgFuture.get().array());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

    @Override
    public CasualDomainDiscoveryReplyMessage readChunked(final AsynchronousByteChannel channel)
    {
        try
        {
            final ByteBuffer executionBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.EXECUTION.getNetworkSize()).get();
            final ByteBuffer domainIdBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.DOMAIN_ID.getNetworkSize()).get();
            final ByteBuffer domainNameSizeBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).get();
            final ByteBuffer domainNameBuffer = ByteUtils.readFully(channel, (int) domainNameSizeBuffer.getLong()).get();
            final ByteBuffer numberOfServicesBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize()).get();
            final List<byte[]> services = new ArrayList<>();
            final long numberOfServices = numberOfServicesBuffer.getLong();
            for (int i = 0; i < numberOfServices; ++i)
            {
                services.addAll(readService(channel));
            }
            final ByteBuffer numberOfQueuesBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize()).get();
            final List<byte[]> queues = new ArrayList<>();
            final long numberOfQueues = numberOfQueuesBuffer.getLong();
            for (int i = 0; i < numberOfQueues; ++i)
            {
                queues.addAll(readQueue(channel));
            }
            final List<byte[]> msg = new ArrayList<>();
            msg.add(executionBuffer.array());
            msg.add(domainIdBuffer.array());
            msg.add(domainNameSizeBuffer.array());
            msg.add(domainNameBuffer.array());
            msg.add(numberOfServicesBuffer.array());
            msg.addAll(services);
            msg.add(numberOfQueuesBuffer.array());
            msg.addAll(queues);
            return readMessage(msg);
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

    @Override
    public CasualDomainDiscoveryReplyMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        return readMessage(ByteUtils.readFully(channel, messageSize).array());
    }

    @Override
    public CasualDomainDiscoveryReplyMessage readChunked(final ReadableByteChannel channel)
    {
        final ByteBuffer executionBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.EXECUTION.getNetworkSize());
        final ByteBuffer domainIdBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.DOMAIN_ID.getNetworkSize());
        final ByteBuffer domainNameSizeBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize());
        final ByteBuffer domainNameBuffer = ByteUtils.readFully(channel, (int) domainNameSizeBuffer.getLong());
        final ByteBuffer numberOfServicesBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize());
        final List<byte[]> services = new ArrayList<>();
        final long numberOfServices = numberOfServicesBuffer.getLong();
        for (int i = 0; i < numberOfServices; ++i)
        {
            services.addAll(readService(channel));
        }
        final ByteBuffer numberOfQueuesBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize());
        final List<byte[]> queues = new ArrayList<>();
        final long numberOfQueues = numberOfQueuesBuffer.getLong();
        for (int i = 0; i < numberOfQueues; ++i)
        {
            queues.addAll(readQueue(channel));
        }
        final List<byte[]> msg = new ArrayList<>();
        msg.add(executionBuffer.array());
        msg.add(domainIdBuffer.array());
        msg.add(domainNameSizeBuffer.array());
        msg.add(domainNameBuffer.array());
        msg.add(numberOfServicesBuffer.array());
        msg.addAll(services);
        msg.add(numberOfQueuesBuffer.array());
        msg.addAll(queues);
        return readMessage(msg);
    }

    private static List<byte[]> readService(final AsynchronousByteChannel channel) throws ExecutionException, InterruptedException
    {
        // A service is expected to fit into one byte[] - it is never chunked
        final ByteBuffer serviceNameSizeBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.SERVICES_ELEMENT_NAME_SIZE.getNetworkSize()).get();
        final ByteBuffer serviceNameBuffer = ByteUtils.readFully(channel, (int)serviceNameSizeBuffer.getLong()).get();
        final ByteBuffer categorySizeBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.SERVICES_ELEMENT_CATEGORY_SIZE.getNetworkSize()).get();
        final ByteBuffer categoryNameBuffer = ByteUtils.readFully(channel, (int)categorySizeBuffer.getLong()).get();
        final ByteBuffer transactionTimeoutAndHopsBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.SERVICES_ELEMENT_TRANSACTION.getNetworkSize() +
                                                                                               DiscoveryReplySizes.SERVICES_ELEMENT_TIMEOUT.getNetworkSize() +
                                                                                               DiscoveryReplySizes.SERVICES_ELEMENT_HOPS.getNetworkSize()).get();

        final ByteBuffer msg = ByteBuffer.allocate(serviceNameBuffer.capacity() +
                                                   serviceNameBuffer.capacity() +
                                                   categorySizeBuffer.capacity() +
                                                   categoryNameBuffer.capacity() +
                                                   transactionTimeoutAndHopsBuffer.capacity());
        msg.put(serviceNameSizeBuffer.array());
        msg.put(serviceNameBuffer.array());
        msg.put(categorySizeBuffer.array());
        msg.put(categoryNameBuffer.array());
        msg.put(transactionTimeoutAndHopsBuffer.array());
        final List<byte[]> l = new ArrayList<>();
        l.add(msg.array());
        return l;
    }

    private static List<byte[]> readQueue(final AsynchronousByteChannel channel) throws ExecutionException, InterruptedException
    {
        // A queue is expected to fit into one byte[] - it is never chunked
        final ByteBuffer queueNameSizeBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize()).get();
        final ByteBuffer queueNameBuffer = ByteUtils.readFully(channel, (int)queueNameSizeBuffer.getLong()).get();
        final ByteBuffer queueRetriesBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize()).get();
        final ByteBuffer msg = ByteBuffer.allocate(queueNameSizeBuffer.capacity() + queueNameBuffer.capacity() + queueRetriesBuffer.capacity());
        msg.put(queueNameSizeBuffer.array());
        msg.put(queueNameBuffer.array());
        msg.put(queueRetriesBuffer.array());
        final List<byte[]> l = new ArrayList<>();
        l.add(msg.array());
        return l;
    }

    private static List<byte[]> readService(final ReadableByteChannel channel)
    {
        // A service is expected to fit into one byte[] - it is never chunked
        final ByteBuffer serviceNameSizeBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.SERVICES_ELEMENT_NAME_SIZE.getNetworkSize());
        final ByteBuffer serviceNameBuffer = ByteUtils.readFully(channel, (int)serviceNameSizeBuffer.getLong());
        final ByteBuffer categorySizeBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.SERVICES_ELEMENT_CATEGORY_SIZE.getNetworkSize());
        final ByteBuffer categoryNameBuffer = ByteUtils.readFully(channel, (int)categorySizeBuffer.getLong());
        final ByteBuffer transactionTimeoutAndHopsBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.SERVICES_ELEMENT_TRANSACTION.getNetworkSize() +
            DiscoveryReplySizes.SERVICES_ELEMENT_TIMEOUT.getNetworkSize() +
            DiscoveryReplySizes.SERVICES_ELEMENT_HOPS.getNetworkSize());

        final ByteBuffer msg = ByteBuffer.allocate(serviceNameBuffer.capacity() +
            serviceNameBuffer.capacity() +
            categorySizeBuffer.capacity() +
            categoryNameBuffer.capacity() +
            transactionTimeoutAndHopsBuffer.capacity());
        msg.put(serviceNameSizeBuffer.array());
        msg.put(serviceNameBuffer.array());
        msg.put(categorySizeBuffer.array());
        msg.put(categoryNameBuffer.array());
        msg.put(transactionTimeoutAndHopsBuffer.array());
        final List<byte[]> l = new ArrayList<>();
        l.add(msg.array());
        return l;
    }

    private static List<byte[]> readQueue(final ReadableByteChannel channel)
    {
        // A queue is expected to fit into one byte[] - it is never chunked
        final ByteBuffer queueNameSizeBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize());
        final ByteBuffer queueNameBuffer = ByteUtils.readFully(channel, (int)queueNameSizeBuffer.getLong());
        final ByteBuffer queueRetriesBuffer = ByteUtils.readFully(channel, DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize());
        final ByteBuffer msg = ByteBuffer.allocate(queueNameSizeBuffer.capacity() + queueNameBuffer.capacity() + queueRetriesBuffer.capacity());
        msg.put(queueNameSizeBuffer.array());
        msg.put(queueNameBuffer.array());
        msg.put(queueRetriesBuffer.array());
        final List<byte[]> l = new ArrayList<>();
        l.add(msg.array());
        return l;
    }

    private static CasualDomainDiscoveryReplyMessage readMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, DiscoveryReplySizes.EXECUTION.getNetworkSize()));
        currentOffset +=  DiscoveryReplySizes.EXECUTION.getNetworkSize();
        final UUID domainId = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DiscoveryReplySizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += DiscoveryReplySizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int) ByteBuffer.wrap(bytes, currentOffset , DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualNetworkReaderUtils.getAsString(bytes, currentOffset, domainNameSize);
        currentOffset += domainNameSize;
        final long numberOfServices = ByteBuffer.wrap(bytes, currentOffset, DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize();
        DynamicArrayIndexPair<Service> services = getServices(bytes, currentOffset, numberOfServices);
        currentOffset = services.getIndex();
        final long numberOfQueues = ByteBuffer.wrap(bytes, currentOffset, DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize();
        DynamicArrayIndexPair<Queue> queues = getQueues(bytes, currentOffset, numberOfQueues);

        return CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                .setServices(services.getBytes())
                                                .setQueues(queues.getBytes());
    }

    private static DynamicArrayIndexPair<Service> getServices(final byte[] bytes, int currentOffset, long numberOfServices)
    {
        final List<Service> l = new ArrayList<>();
        int offset = currentOffset;
        for(int i = 0; i < numberOfServices; ++i)
        {
            offset = addService(bytes, offset, l);
        }
        return DynamicArrayIndexPair.of(l, offset);
    }

    private static int addService(final byte[] bytes, final int currentOffset, final List<Service> l)
    {
        int offset = currentOffset;
        final int nameSize = (int)ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_NAME_SIZE.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_NAME_SIZE.getNetworkSize();
        final String name = CasualNetworkReaderUtils.getAsString(bytes, offset, nameSize);
        offset += nameSize;
        final int categorySize = (int)ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_CATEGORY_SIZE.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_CATEGORY_SIZE.getNetworkSize();
        final String category = CasualNetworkReaderUtils.getAsString(bytes, offset, categorySize);
        offset += categorySize;
        final short transaction = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_TRANSACTION.getNetworkSize()).getShort();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_TRANSACTION.getNetworkSize();
        final long timeout = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_TIMEOUT.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_TIMEOUT.getNetworkSize();
        final long hops = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_HOPS.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_HOPS.getNetworkSize();
        final Service s = Service.of(name, category, TransactionType.unmarshal(transaction))
                                 .setTimeout(timeout)
                                 .setHops(hops);
        l.add(s);
        return offset;
    }

    private static DynamicArrayIndexPair<Queue> getQueues(final byte[] bytes, int currentOffset, long numberOfServices)
    {
        final List<Queue> l = new ArrayList<>();
        int offset = currentOffset;
        for(int i = 0; i < numberOfServices; ++i)
        {
            offset = addQueue(bytes, offset, l);
        }
        return DynamicArrayIndexPair.of(l, offset);
    }

    private static int addQueue(final byte[] bytes, int currentOffset, final List<Queue> l)
    {
        int offset = currentOffset;
        final int nameSize = (int)ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize();
        final String name = CasualNetworkReaderUtils.getAsString(bytes, offset, nameSize);
        offset += nameSize;
        final long retries = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize();
        final Queue q = Queue.of(name)
                             .setRetries(retries);
        l.add(q);
        return offset;
    }

    /**
     * Used when header payload > Integer.MAX_VALUE
     * @see CasualDomainDiscoveryReplyMessage::addExtraDataMultipleBuffers
     * To understand how message should be structured
     **/
    private static CasualDomainDiscoveryReplyMessage readMessage(final List<byte[]> message)
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
        final long numberOfServices = ByteBuffer.wrap(message.get(currentIndex++)).getLong();
        List<Service> services = new ArrayList<>();
        for(int i = 0; i < numberOfServices; ++i)
        {
            addService(message.get(currentIndex++), 0, services);
        }
        final long numberOfQueues = ByteBuffer.wrap(message.get(currentIndex++)).getLong();
        List<Queue> queues = new ArrayList<>();
        for(int i = 0; i < numberOfQueues; ++i)
        {
            addQueue(message.get(currentIndex++), 0, queues);
        }
        return CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                .setServices(services)
                                                .setQueues(queues);
    }


}
