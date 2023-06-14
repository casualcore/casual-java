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
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectReplyMessage;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;

public final class DomainDisconnectReplyMessageDecoder implements NetworkDecoder<DomainDisconnectReplyMessage>
{
    private DomainDisconnectReplyMessageDecoder()
    {}

    public static NetworkDecoder<DomainDisconnectReplyMessage> of()
    {
        return new DomainDisconnectReplyMessageDecoder();
    }

    @Override
    public DomainDisconnectReplyMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        DomainDisconnectReplyMessage msg = getMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public DomainDisconnectReplyMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        return DomainDisconnectReplyMessage.of(execution);
    }

    @Override
    public DomainDisconnectReplyMessage readSingleBuffer(final ByteBuf buffer)
    {
        return getMessage(buffer);
    }

    private DomainDisconnectReplyMessage getMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        return DomainDisconnectReplyMessage.of(execution);
    }

}
