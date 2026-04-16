/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.queue;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.queue.QueueErrorCode;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.api.util.Pair;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.DequeueReplySizes;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueReplyMessage;
import se.laz.casual.network.protocol.messages.queue.DequeueMessage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;

public final class CasualDequeueReplyMessageDecoder implements NetworkDecoder<CasualDequeueReplyMessage>
{
    private final ProtocolVersion protocolVersion;

    private CasualDequeueReplyMessageDecoder(ProtocolVersion protocolVersion)
    {
        this.protocolVersion = protocolVersion;
    }

    public static CasualDequeueReplyMessageDecoder of(ProtocolVersion protocolVersion)
    {
        return new CasualDequeueReplyMessageDecoder(protocolVersion);
    }

    @Override
    public CasualDequeueReplyMessage readSingleBuffer(byte[] data)
    {
        return protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) ?
                getMessageProtocolVersionEqualOrGreaterToOneThree(data)
                : getMessage(data);
    }

    private static Pair<Integer, DequeueMessage> readDequeueMessage(final byte[] bytes, int offset)
    {
        int currentOffset = offset;
        UUID msgId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DequeueReplySizes.MESSAGE_ID.getNetworkSize()));
        currentOffset += DequeueReplySizes.MESSAGE_ID.getNetworkSize();
        int propertiesSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize();
        String properties = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, propertiesSize);
        currentOffset += propertiesSize;

        int replyDataSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize();
        String replyData = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, replyDataSize);
        currentOffset += replyDataSize;

        long availableSinceEpoc = ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize();

        Pair<Integer, ServiceBuffer> p = CasualMessageDecoderUtils.readServiceBuffer(bytes, currentOffset);
        currentOffset = p.first();

        long redelivered = ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize();
        long timestampSinceEpoc = ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize();
        DequeueMessage msg =  DequeueMessage.of(QueueMessage.createBuilder()
                                                                  .withId(msgId)
                                                                  .withCorrelationInformation(properties)
                                                                  .withReplyQueue(replyData)
                                                                  .withAvailableSince(availableSinceEpoc)
                                                                  .withTimestamp(timestampSinceEpoc)
                                                                  .withRedelivered(redelivered)
                                                                  .withPayload(p.second())
                                                                  .build());
        return Pair.of(currentOffset, msg);
    }

    private CasualDequeueReplyMessage getMessage(byte[] bytes)
    {
        int currentOffset = 0;
        UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        long numberOfMessages = ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.NUMBER_OF_MESSAGES.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.NUMBER_OF_MESSAGES.getNetworkSize();
        List<DequeueMessage> l = new ArrayList<>();
        for(int i = 0; i < numberOfMessages; ++i)
        {
            Pair<Integer, DequeueMessage> p = readDequeueMessage(bytes, currentOffset);
            currentOffset = p.first();
            l.add(p.second());
        }
        return CasualDequeueReplyMessage.createBuilder()
                                        .withProtocolVersion(protocolVersion)
                                        .withExecution(execution)
                                        .withMessages(l)
                                        .build();
    }

    private CasualDequeueReplyMessage getMessageProtocolVersionEqualOrGreaterToOneThree(byte[] bytes)
    {
        int currentOffset = 0;
        UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();

        byte value = ByteBuffer.wrap(bytes, currentOffset, DequeueReplySizes.HAS_VALUE.getNetworkSize()).get();
        currentOffset += DequeueReplySizes.HAS_VALUE.getNetworkSize();
        List<DequeueMessage> l = new ArrayList<>();
        if(value != 0)
        {
            Pair<Integer, DequeueMessage> p = readDequeueMessage(bytes, currentOffset);
            currentOffset = p.first();
            l.add(p.second());
        }
        final ByteBuffer callErrorBuffer = ByteBuffer.wrap(bytes, currentOffset, CommonSizes.CALL_ERROR.getNetworkSize());
        int callError = callErrorBuffer.getInt();

        return CasualDequeueReplyMessage.createBuilder()
                                        .withProtocolVersion(protocolVersion)
                                        .withExecution(execution)
                                        .withMessages(l)
                                        .withCode(QueueErrorCode.unmarshal(callError))
                                        .build();
    }
}
