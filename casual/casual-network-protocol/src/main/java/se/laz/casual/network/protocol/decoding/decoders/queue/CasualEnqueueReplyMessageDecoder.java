/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.queue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueReplyMessage;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

public class CasualEnqueueReplyMessageDecoder implements NetworkDecoder<CasualEnqueueReplyMessage>
{
    private CasualEnqueueReplyMessageDecoder()
    {}

    public static CasualEnqueueReplyMessageDecoder of()
    {
        return new CasualEnqueueReplyMessageDecoder();
    }

    @Override
    public CasualEnqueueReplyMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        CasualEnqueueReplyMessage msg = getMessage(buffer);
        return msg;
    }

    @Override
    public CasualEnqueueReplyMessage readChunked(final ReadableByteChannel channel)
    {
        UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        UUID id = CasualMessageDecoderUtils.readUUID(channel);
        return CasualEnqueueReplyMessage.createBuilder()
                                        .withExecution(execution)
                                        .withId(id)
                                        .build();
    }

    @Override
    public CasualEnqueueReplyMessage readSingleBuffer(final ByteBuf buffer)
    {
        return getMessage(buffer);
    }

    private static CasualEnqueueReplyMessage getMessage(final ByteBuf buffer)
    {
        UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        UUID id = CasualMessageDecoderUtils.readUUID(buffer);
        return CasualEnqueueReplyMessage.createBuilder()
                                        .withExecution(execution)
                                        .withId(id)
                                        .build();
    }

}
