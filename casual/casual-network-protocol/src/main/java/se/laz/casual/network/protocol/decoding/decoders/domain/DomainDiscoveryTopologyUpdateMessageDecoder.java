/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.domain.DomainDiscoveryTopologyUpdateMessage;
import se.laz.casual.network.protocol.messages.parseinfo.DiscoveryTopologyUpdateRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

public final class DomainDiscoveryTopologyUpdateMessageDecoder implements NetworkDecoder<DomainDiscoveryTopologyUpdateMessage>
{
    private DomainDiscoveryTopologyUpdateMessageDecoder()
    {}

    public static NetworkDecoder<DomainDiscoveryTopologyUpdateMessage> of()
    {
        return new DomainDiscoveryTopologyUpdateMessageDecoder();
    }

    @Override
    public DomainDiscoveryTopologyUpdateMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return getMessage(b.array());
    }

    @Override
    public DomainDiscoveryTopologyUpdateMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        final int domainsSize = (int) ByteUtils.readFully(channel, DiscoveryTopologyUpdateRequestSizes.DOMAINS_SIZE.getNetworkSize()).getLong();
        final UUID domainId = CasualMessageDecoderUtils.readUUID(channel);
        final int domainNameSize = (int) ByteUtils.readFully(channel, DiscoveryTopologyUpdateRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        final String domainName = CasualMessageDecoderUtils.readString(channel, domainNameSize);
        return DomainDiscoveryTopologyUpdateMessage.createBuilder()
                                                   .withExecution(execution)
                                                   .withDomainsSize(domainsSize)
                                                   .withDomainId(domainId)
                                                   .withDomainName(domainName)
                                                   .build();
    }

    @Override
    public DomainDiscoveryTopologyUpdateMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private DomainDiscoveryTopologyUpdateMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, DiscoveryTopologyUpdateRequestSizes.EXECUTION.getNetworkSize()));
        currentOffset +=  DiscoveryTopologyUpdateRequestSizes.EXECUTION.getNetworkSize();
        long domainsSize = ByteBuffer.wrap(bytes, currentOffset , DiscoveryTopologyUpdateRequestSizes.DOMAINS_SIZE.getNetworkSize()).getLong();
        currentOffset +=  DiscoveryTopologyUpdateRequestSizes.DOMAINS_SIZE.getNetworkSize();
        final UUID domainId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DiscoveryTopologyUpdateRequestSizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += DiscoveryTopologyUpdateRequestSizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , DiscoveryTopologyUpdateRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryTopologyUpdateRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, domainNameSize);
        return DomainDiscoveryTopologyUpdateMessage.createBuilder()
                                                   .withExecution(execution)
                                                   .withDomainsSize(domainsSize)
                                                   .withDomainId(domainId)
                                                   .withDomainName(domainName)
                                                   .build();
    }

}
