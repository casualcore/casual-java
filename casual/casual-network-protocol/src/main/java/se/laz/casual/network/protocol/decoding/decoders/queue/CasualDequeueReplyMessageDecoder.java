/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.queue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.DequeueReplySizes;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueReplyMessage;
import se.laz.casual.network.protocol.messages.queue.DequeueMessage;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CasualDequeueReplyMessageDecoder implements NetworkDecoder<CasualDequeueReplyMessage>
{
    private CasualDequeueReplyMessageDecoder()
    {}

    public static CasualDequeueReplyMessageDecoder of()
    {
        return new CasualDequeueReplyMessageDecoder();
    }

    @Override
    public CasualDequeueReplyMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        CasualDequeueReplyMessage msg = getMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public CasualDequeueReplyMessage readChunked(final ReadableByteChannel channel)
    {
        UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        int numberOfMessages = (int) ByteUtils.readFully(channel, DequeueReplySizes.NUMBER_OF_MESSAGES.getNetworkSize()).getLong();
        List<DequeueMessage> l = new ArrayList<>();
        for(int i = 0; i < numberOfMessages; ++i)
        {
            l.add(readDequeueMessage(channel));
        }
        return CasualDequeueReplyMessage.createBuilder()
                                        .withExecution(execution)
                                        .withMessages(l)
                                        .build();
    }

    @Override
    public CasualDequeueReplyMessage readSingleBuffer(final ByteBuf buffer)
    {
        return getMessage(buffer);
    }

    private static DequeueMessage readDequeueMessage(final ReadableByteChannel channel)
    {
        UUID msgId = CasualMessageDecoderUtils.readUUID(channel);
        int propertiesSize = (int) ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize()).getLong();
        String properties = CasualMessageDecoderUtils.readString(channel, propertiesSize);
        int replyDataSize = (int) ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize()).getLong();
        String replyData = CasualMessageDecoderUtils.readString(channel, replyDataSize);
        long availableSinceEpoc = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize()).getLong();
        ServiceBuffer serviceBuffer = CasualMessageDecoderUtils.readServiceBuffer(channel, Integer.MAX_VALUE);
        long redelivered = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize()).getLong();
        long timestampSinceEpoc = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize()).getLong();
        return DequeueMessage.of(QueueMessage.createBuilder()
                                             .withId(msgId)
                                             .withCorrelationInformation(properties)
                                             .withReplyQueue(replyData)
                                             .withAvailableSince(availableSinceEpoc)
                                             .withTimestamp(timestampSinceEpoc)
                                             .withRedelivered(redelivered)
                                             .withPayload(serviceBuffer)
                                             .build());
    }

    private static DequeueMessage readDequeueMessage(ByteBuf buffer)
    {
        UUID msgId = CasualMessageDecoderUtils.readUUID(buffer);
        int propertiesSize = (int)buffer.readLong();
        String properties = CasualMessageDecoderUtils.readAsString(buffer, propertiesSize);
        int replyDataSize = (int)buffer.readLong();
        String replyData = CasualMessageDecoderUtils.readAsString(buffer, replyDataSize);
        long availableSinceEpoc = buffer.readLong();
        ServiceBuffer serviceBuffer = CasualMessageDecoderUtils.readServiceBuffer(buffer);

        long redelivered = buffer.readLong();
        long timestampSinceEpoc = buffer.readLong();
        return  DequeueMessage.of(QueueMessage.createBuilder()
                                              .withId(msgId)
                                              .withCorrelationInformation(properties)
                                              .withReplyQueue(replyData)
                                              .withAvailableSince(availableSinceEpoc)
                                              .withTimestamp(timestampSinceEpoc)
                                              .withRedelivered(redelivered)
                                              .withPayload(serviceBuffer)
                                              .build());
    }

    private static CasualDequeueReplyMessage getMessage(final ByteBuf buffer)
    {
        UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        long numberOfMessages = buffer.readLong();
        List<DequeueMessage> l = new ArrayList<>();
        for(int i = 0; i < numberOfMessages; ++i)
        {
            l.add(readDequeueMessage(buffer));
        }
        return CasualDequeueReplyMessage.createBuilder()
                                        .withExecution(execution)
                                        .withMessages(l)
                                        .build();
    }
}
