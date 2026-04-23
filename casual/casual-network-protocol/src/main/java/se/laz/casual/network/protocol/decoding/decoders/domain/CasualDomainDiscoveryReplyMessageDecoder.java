/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.messages.domain.TransactionType;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.decoding.decoders.utils.DecoderReaderValidator;
import se.laz.casual.network.protocol.decoding.decoders.utils.DynamicArrayIndexPair;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.domain.Queue;
import se.laz.casual.network.protocol.messages.domain.Service;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.DiscoveryReplySizes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_4;

/**
 * Created by aleph on 2017-03-08.
 */
public final class CasualDomainDiscoveryReplyMessageDecoder implements NetworkDecoder<CasualDomainDiscoveryReplyMessage>
{
    private final ProtocolVersion protocolVersion;

    private CasualDomainDiscoveryReplyMessageDecoder(ProtocolVersion protocolVersion)
    {
        this.protocolVersion = protocolVersion;
    }

    public static NetworkDecoder<CasualDomainDiscoveryReplyMessage> of(ProtocolVersion protocolVersion)
    {
        return new CasualDomainDiscoveryReplyMessageDecoder(protocolVersion);
    }

    @Override
    public CasualDomainDiscoveryReplyMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    public CasualDomainDiscoveryReplyMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset +=  CommonSizes.EXECUTION.getNetworkSize();
        final UUID domainId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DiscoveryReplySizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += DiscoveryReplySizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int) ByteBuffer.wrap(bytes, currentOffset , DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, domainNameSize);
        currentOffset += domainNameSize;
        final long numberOfServices = ByteBuffer.wrap(bytes, currentOffset, DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize();
        DynamicArrayIndexPair<Service> services = getServices(bytes, currentOffset, numberOfServices);
        currentOffset = services.getIndex();
        final long numberOfQueues = ByteBuffer.wrap(bytes, currentOffset, DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize();
        DynamicArrayIndexPair<Queue> queues = getQueues(bytes, currentOffset, numberOfQueues);
        currentOffset = queues.getIndex();

        DecoderReaderValidator.throwIfDataNotFullyRead( currentOffset, bytes.length );

        return CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName, protocolVersion)
                                                .setServices(services.getBytes())
                                                .setQueues(queues.getBytes());
    }

    private DynamicArrayIndexPair<Service> getServices(final byte[] bytes, int currentOffset, long numberOfServices)
    {
        final List<Service> l = new ArrayList<>();
        int offset = currentOffset;
        for(int i = 0; i < numberOfServices; ++i)
        {
            offset = addService(bytes, offset, l);
        }
        return DynamicArrayIndexPair.of(l, offset);
    }

    private int addService(final byte[] bytes, final int currentOffset, final List<Service> l)
    {
        int offset = currentOffset;
        final int nameSize = (int)ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_NAME_SIZE.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_NAME_SIZE.getNetworkSize();
        final String name = CasualMessageDecoderUtils.getAsString(bytes, offset, nameSize);
        offset += nameSize;
        final int categorySize = (int)ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_CATEGORY_SIZE.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_CATEGORY_SIZE.getNetworkSize();
        final String category = CasualMessageDecoderUtils.getAsString(bytes, offset, categorySize);
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

    private DynamicArrayIndexPair<Queue> getQueues(final byte[] bytes, int currentOffset, long numberOfServices)
    {
        final List<Queue> l = new ArrayList<>();
        int offset = currentOffset;
        for(int i = 0; i < numberOfServices; ++i)
        {
            offset = addQueue(bytes, offset, l);
        }
        return DynamicArrayIndexPair.of(l, offset);
    }

    private int addQueue(final byte[] bytes, int currentOffset, final List<Queue> l)
    {
        int offset = currentOffset;
        final int nameSize = (int)ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize();
        final String name = CasualMessageDecoderUtils.getAsString(bytes, offset, nameSize);
        offset += nameSize;
        final long retries = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize();
        final Queue.Builder q = Queue.createBuilder()
                                     .withName(name)
                                     .withProtocolVersion(protocolVersion)
                                     .withRetries(retries);
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_4 ))
        {
            final long retryDelay = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.QUEUES_ELEMENT_RETRY_DELAY.getNetworkSize()).getLong();
            offset += DiscoveryReplySizes.QUEUES_ELEMENT_RETRY_DELAY.getNetworkSize();
            boolean enqueueEnabled = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.QUEUES_ELEMENT_ENQUEUE_ENABLED.getNetworkSize()).get() > 0;
            offset += DiscoveryReplySizes.QUEUES_ELEMENT_ENQUEUE_ENABLED.getNetworkSize();
            boolean dequeueEnabled = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.QUEUES_ELEMENT_DEQUEUE_ENABLED.getNetworkSize()).get() > 0;
            offset += DiscoveryReplySizes.QUEUES_ELEMENT_DEQUEUE_ENABLED.getNetworkSize();
            q.withRetryDelay(retryDelay)
             .withEnqueueEnabled(enqueueEnabled)
             .withDequeueEnabled(dequeueEnabled);
        }
        l.add(q.build());
        return offset;
    }
}
