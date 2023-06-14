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
import se.laz.casual.api.util.Pair;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.EnqueueRequestSizes;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueRequestMessage;
import se.laz.casual.network.protocol.messages.queue.EnqueueMessage;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

public class CasualEnqueueRequestMessageDecoder implements NetworkDecoder<CasualEnqueueRequestMessage>
{
    private CasualEnqueueRequestMessageDecoder()
    { }

    public static CasualEnqueueRequestMessageDecoder of()
    {
        return new CasualEnqueueRequestMessageDecoder();
    }

    @Override
    public CasualEnqueueRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        CasualEnqueueRequestMessage msg = getMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public CasualEnqueueRequestMessage readChunked(final ReadableByteChannel channel)
    {
        UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        int queueNameSize = (int) ByteUtils.readFully(channel, EnqueueRequestSizes.NAME_SIZE.getNetworkSize()).getLong();
        String queueName = CasualMessageDecoderUtils.readString(channel, queueNameSize);
        Xid xid = XIDUtils.readXid(channel);
        return CasualEnqueueRequestMessage.createBuilder()
                                          .withExecution(execution)
                                          .withQueueName(queueName)
                                          .withXid(xid)
                                          .withMessage(readEnqueueMessage(channel))
                                          .build();
    }

    @Override
    public CasualEnqueueRequestMessage readSingleBuffer(final ByteBuf buffer)
    {
        return getMessage(buffer);
    }

    private static CasualEnqueueRequestMessage getMessage(final ByteBuf buffer)
    {
        UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        int queueNameSize = (int)buffer.readLong();
        String queueName = CasualMessageDecoderUtils.readAsString(buffer, queueNameSize);
        Xid xid = CasualMessageDecoderUtils.readXid(buffer);
        EnqueueMessage msg = readEnqueueMessage(buffer);
        return CasualEnqueueRequestMessage.createBuilder()
                                          .withExecution(execution)
                                          .withQueueName(queueName)
                                          .withXid(xid)
                                          .withMessage(msg)
                                          .build();
    }

    private static EnqueueMessage readEnqueueMessage(final ReadableByteChannel channel)
    {
        UUID msgId = CasualMessageDecoderUtils.readUUID(channel);
        int propertiesSize = (int) ByteUtils.readFully(channel, EnqueueRequestSizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize()).getLong();
        String properties = CasualMessageDecoderUtils.readString(channel, propertiesSize);
        int replyDataSize = (int) ByteUtils.readFully(channel, EnqueueRequestSizes.MESSAGE_REPLY_SIZE.getNetworkSize()).getLong();
        String replyData = CasualMessageDecoderUtils.readString(channel, replyDataSize);
        long availableSinceEpoc = ByteUtils.readFully(channel, EnqueueRequestSizes.MESSAGE_AVAILABLE.getNetworkSize()).getLong();
        ServiceBuffer serviceBuffer = CasualMessageDecoderUtils.readServiceBuffer(channel, Integer.MAX_VALUE);
        return EnqueueMessage.of(QueueMessage.createBuilder()
                                             .withId(msgId)
                                             .withCorrelationInformation(properties)
                                             .withReplyQueue(replyData)
                                             .withAvailableSince(availableSinceEpoc)
                                             .withPayload(serviceBuffer)
                                             .build());
    }

    private static EnqueueMessage readEnqueueMessage(final ByteBuf buffer)
    {
        UUID msgId = CasualMessageDecoderUtils.readUUID(buffer);
        int propertiesSize = (int)buffer.readLong();
        String properties = CasualMessageDecoderUtils.readAsString(buffer, propertiesSize);
        int replyDataSize = (int)buffer.readLong();
        String replyData = CasualMessageDecoderUtils.readAsString(buffer, replyDataSize);
        long availableSinceEpoc = buffer.readLong();
        ServiceBuffer serviceBuffer = CasualMessageDecoderUtils.readServiceBuffer(buffer);
        return EnqueueMessage.of(QueueMessage.createBuilder()
                                             .withId(msgId)
                                             .withCorrelationInformation(properties)
                                             .withReplyQueue(replyData)
                                             .withAvailableSince(availableSinceEpoc)
                                             .withPayload(serviceBuffer)
                                             .build());
    }

}
