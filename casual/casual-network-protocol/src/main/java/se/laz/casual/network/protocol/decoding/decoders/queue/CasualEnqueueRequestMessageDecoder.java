/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.queue;

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
        return getMessage(b.array());
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
    public CasualEnqueueRequestMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private static CasualEnqueueRequestMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        int queueNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , EnqueueRequestSizes.NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += EnqueueRequestSizes.NAME_SIZE.getNetworkSize();
        String queueName = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, queueNameSize);
        currentOffset += queueNameSize;
        Pair<Integer, Xid> xidInfo = CasualMessageDecoderUtils.readXid(bytes, currentOffset);
        currentOffset = xidInfo.first();
        Xid xid = xidInfo.second();

        EnqueueMessage msg = readEnqueueMessage(bytes, currentOffset);
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

    private static EnqueueMessage readEnqueueMessage(final byte[] bytes, int offset)
    {
        int currentOffset = offset;
        UUID msgId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset,  currentOffset +EnqueueRequestSizes.MESSAGE_ID.getNetworkSize()));
        currentOffset += EnqueueRequestSizes.MESSAGE_ID.getNetworkSize();
        int propertiesSize = (int)ByteBuffer.wrap(bytes, currentOffset , EnqueueRequestSizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize()).getLong();
        currentOffset += EnqueueRequestSizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize();
        String properties = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, propertiesSize);
        currentOffset += propertiesSize;

        int replyDataSize = (int)ByteBuffer.wrap(bytes, currentOffset , EnqueueRequestSizes.MESSAGE_REPLY_SIZE.getNetworkSize()).getLong();
        currentOffset += EnqueueRequestSizes.MESSAGE_REPLY_SIZE.getNetworkSize();
        String replyData = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, replyDataSize);
        currentOffset += replyDataSize;

        long availableSinceEpoc = ByteBuffer.wrap(bytes, currentOffset , EnqueueRequestSizes.MESSAGE_AVAILABLE.getNetworkSize()).getLong();
        currentOffset += EnqueueRequestSizes.MESSAGE_AVAILABLE.getNetworkSize();

        Pair<Integer, ServiceBuffer> p = CasualMessageDecoderUtils.readServiceBuffer(bytes, currentOffset);
        return EnqueueMessage.of(QueueMessage.createBuilder()
                                             .withId(msgId)
                                             .withCorrelationInformation(properties)
                                             .withReplyQueue(replyData)
                                             .withAvailableSince(availableSinceEpoc)
                                             .withPayload(p.second())
                                             .build());
    }

}
