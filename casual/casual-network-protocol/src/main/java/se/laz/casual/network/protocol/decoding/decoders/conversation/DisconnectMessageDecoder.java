/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.conversation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.conversation.Disconnect;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationDisconnectSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-16.
 */
public final class DisconnectMessageDecoder implements NetworkDecoder<Disconnect>
{
    private DisconnectMessageDecoder()
    {}

    public static NetworkDecoder<Disconnect> of()
    {
        return new DisconnectMessageDecoder();
    }

    @Override
    public Disconnect readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        Disconnect msg = createMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public Disconnect readChunked(final ReadableByteChannel channel)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Disconnect readSingleBuffer(final ByteBuf buffer)
    {
        return createMessage(buffer);
    }

    private static Disconnect createMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        return Disconnect.createBuilder()
                .setExecution(execution)
                .build();
    }

}
