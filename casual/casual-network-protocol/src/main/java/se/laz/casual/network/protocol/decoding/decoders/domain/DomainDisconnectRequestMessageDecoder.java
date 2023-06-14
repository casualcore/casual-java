/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.domain;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectRequestMessage;
import se.laz.casual.network.protocol.messages.parseinfo.ConnectRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

public final class DomainDisconnectRequestMessageDecoder implements NetworkDecoder<DomainDisconnectRequestMessage>
{
    private DomainDisconnectRequestMessageDecoder()
    {}

    public static NetworkDecoder<DomainDisconnectRequestMessage> of()
    {
        return new DomainDisconnectRequestMessageDecoder();
    }

    @Override
    public DomainDisconnectRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        DomainDisconnectRequestMessage msg = getMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public DomainDisconnectRequestMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        return DomainDisconnectRequestMessage.of(execution);
    }

    @Override
    public DomainDisconnectRequestMessage readSingleBuffer(final ByteBuf buffer)
    {
        return getMessage(buffer);
    }

    private DomainDisconnectRequestMessage getMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        return DomainDisconnectRequestMessage.of(execution);
    }

}
