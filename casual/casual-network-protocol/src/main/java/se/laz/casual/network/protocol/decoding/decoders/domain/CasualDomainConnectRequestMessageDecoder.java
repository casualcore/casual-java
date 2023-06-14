/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
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
    public CasualDomainConnectRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        CasualDomainConnectRequestMessage msg =  getMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public CasualDomainConnectRequestMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        final UUID domainId = CasualMessageDecoderUtils.readUUID(channel);
        final int domainNameSize = (int) ByteUtils.readFully(channel, ConnectRequestSizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        final String domainName = CasualMessageDecoderUtils.readString(channel, domainNameSize);
        final List<Long> protocols = readProtocols(channel);
        return CasualDomainConnectRequestMessage.createBuilder()
                                                .withExecution(execution)
                                                .withDomainId(domainId)
                                                .withDomainName(domainName)
                                                .withProtocols(protocols)
                                                .build();
    }

    @Override
    public CasualDomainConnectRequestMessage readSingleBuffer(final ByteBuf buffer)
    {
        return getMessage(buffer);
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

    private CasualDomainConnectRequestMessage getMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        final UUID domainId = CasualMessageDecoderUtils.readUUID(buffer);
        final int domainNameSize = (int)buffer.readLong();
        byte[] domainNameBuffer = new byte[domainNameSize];
        buffer.readBytes(domainNameBuffer);
        final String domainName = CasualMessageDecoderUtils.getAsString(domainNameBuffer);
        long numberOfProtocols = buffer.readLong();
        List<Long> protocols = new ArrayList<>();
        for(; numberOfProtocols > 0; --numberOfProtocols)
        {
            long version = buffer.readLong();
            protocols.add(version);
        }
        return CasualDomainConnectRequestMessage.createBuilder()
                                                .withExecution(execution)
                                                .withDomainId(domainId)
                                                .withDomainName(domainName)
                                                .withProtocols(protocols)
                                                .build();
    }

}
