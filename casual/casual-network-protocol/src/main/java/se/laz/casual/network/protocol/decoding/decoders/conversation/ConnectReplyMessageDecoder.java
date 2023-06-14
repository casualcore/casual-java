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
import se.laz.casual.network.protocol.messages.conversation.ConnectReply;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationConnectReplySizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-16.
 */
public final class ConnectReplyMessageDecoder implements NetworkDecoder<ConnectReply>
{
    private ConnectReplyMessageDecoder()
    {}

    public static NetworkDecoder<ConnectReply> of()
    {
        return new ConnectReplyMessageDecoder();
    }

    @Override
    public ConnectReply readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        ConnectReply msg = createMessage(buffer);
        buffer.release();
        return msg;

    }

    @Override
    public ConnectReply readChunked(final ReadableByteChannel channel)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ConnectReply readSingleBuffer(final ByteBuf buffer)
    {
        return createMessage(buffer);
    }

    private static ConnectReply createMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        int resultCode = buffer.readInt();
        return ConnectReply.createBuilder()
                .setExecution(execution)
                .setResultCode(resultCode)
                .build();
    }

}
