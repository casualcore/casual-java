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
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        CasualDomainConnectReplyMessage msg = getMessage(buffer);
        buffer.release();
        return msg;
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
    public CasualDomainConnectReplyMessage readSingleBuffer(final ByteBuf buffer)
    {
        return getMessage(buffer);
    }

    private CasualDomainConnectReplyMessage getMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        final UUID domainId = CasualMessageDecoderUtils.readUUID(buffer);
        final int domainNameSize = (int)buffer.readLong();
        final String domainName = CasualMessageDecoderUtils.readAsString(buffer, domainNameSize);
        long protocol = buffer.readLong();
        return CasualDomainConnectReplyMessage.createBuilder()
                                              .withExecution(execution)
                                              .withDomainId(domainId)
                                              .withDomainName(domainName)
                                              .withProtocolVersion(protocol)
                                              .build();
    }

}
