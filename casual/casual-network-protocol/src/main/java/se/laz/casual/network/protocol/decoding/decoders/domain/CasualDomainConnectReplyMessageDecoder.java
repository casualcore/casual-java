/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.laz.casual.network.protocol.messages.parseinfo.ConnectReplySizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

public final class CasualDomainConnectReplyMessageDecoder implements NetworkDecoder<CasualDomainConnectReplyMessage>
{
    private CasualDomainConnectReplyMessageDecoder()
    {}

    public static NetworkDecoder<CasualDomainConnectReplyMessage> of()
    {
        return new CasualDomainConnectReplyMessageDecoder();
    }

    @Override
    public CasualDomainConnectReplyMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return getMessage(b.array());
    }

    @Override
    public CasualDomainConnectReplyMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        final UUID domainId = CasualMessageDecoderUtils.readUUID(channel);
        final int domainNameSize = (int) ByteUtils.readFully(channel, ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        final String domainName = CasualMessageDecoderUtils.readString(channel, domainNameSize);
        final long protocol = ByteUtils.readFully(channel, ConnectReplySizes.PROTOCOL_VERSION_SIZE.getNetworkSize()).getLong();
        return CasualDomainConnectReplyMessage.createBuilder()
                                              .withExecution(execution)
                                              .withDomainId(domainId)
                                              .withDomainName(domainName)
                                              .withProtocolVersion(protocol)
                                              .build();
    }

    @Override
    public CasualDomainConnectReplyMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private CasualDomainConnectReplyMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, ConnectReplySizes.EXECUTION.getNetworkSize()));
        currentOffset +=  ConnectReplySizes.EXECUTION.getNetworkSize();
        final UUID domainId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + ConnectReplySizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += ConnectReplySizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConnectReplySizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, domainNameSize);
        currentOffset += domainNameSize;
        long protocol = ByteBuffer.wrap(bytes, currentOffset, ConnectReplySizes.PROTOCOL_VERSION_SIZE.getNetworkSize()).getLong();
        return CasualDomainConnectReplyMessage.createBuilder()
                                              .withExecution(execution)
                                              .withDomainId(domainId)
                                              .withDomainName(domainName)
                                              .withProtocolVersion(protocol)
                                              .build();
    }

}
