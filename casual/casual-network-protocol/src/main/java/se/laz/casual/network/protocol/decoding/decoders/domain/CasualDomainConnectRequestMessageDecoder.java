/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.ConnectRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class CasualDomainConnectRequestMessageDecoder implements NetworkDecoder<CasualDomainConnectRequestMessage>
{
    private CasualDomainConnectRequestMessageDecoder()
    {}

    public static NetworkDecoder<CasualDomainConnectRequestMessage> of()
    {
        return new CasualDomainConnectRequestMessageDecoder();
    }

    @Override
    public CasualDomainConnectRequestMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private List<Long> readProtocols(final ReadableByteChannel channel)
    {
        long numberOfProtocols = ByteUtils.readFully(channel, ConnectRequestSizes.PROTOCOL_VERSION_SIZE.getNetworkSize()).getLong();
        List<Long> l = new ArrayList<>();
        for(; numberOfProtocols > 0; --numberOfProtocols)
        {
            Long version = ByteUtils.readFully(channel, ConnectRequestSizes.PROTOCOL_ELEMENT_SIZE.getNetworkSize()).getLong();
            l.add(version);
        }
        return l;
    }

    private CasualDomainConnectRequestMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset +=  CommonSizes.EXECUTION.getNetworkSize();
        final UUID domainId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + ConnectRequestSizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += ConnectRequestSizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, domainNameSize);
        currentOffset += domainNameSize;
        long numberOfProtocols = ByteBuffer.wrap(bytes, currentOffset , ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize();
        List<Long> protocols = new ArrayList<>();
        for(; numberOfProtocols > 0; --numberOfProtocols)
        {
            Long version = ByteBuffer.wrap(bytes, currentOffset, ConnectRequestSizes.PROTOCOL_ELEMENT_SIZE.getNetworkSize()).getLong();
            protocols.add(version);
            currentOffset += ConnectRequestSizes.PROTOCOL_ELEMENT_SIZE.getNetworkSize();
        }
        return CasualDomainConnectRequestMessage.createBuilder()
                                                .withExecution(execution)
                                                .withDomainId(domainId)
                                                .withDomainName(domainName)
                                                .withProtocols(protocols)
                                                .build();
    }

}
