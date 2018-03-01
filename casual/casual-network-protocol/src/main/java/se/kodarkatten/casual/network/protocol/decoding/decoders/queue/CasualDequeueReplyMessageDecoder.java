package se.kodarkatten.casual.network.protocol.decoding.decoders.queue;

import se.kodarkatten.casual.api.queue.QueueMessage;
import se.kodarkatten.casual.api.util.Pair;
import se.kodarkatten.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.kodarkatten.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.DequeueReplySizes;
import se.kodarkatten.casual.network.protocol.messages.queue.CasualDequeueReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.queue.DequeueMessage;
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer;
import se.kodarkatten.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
        return getMessage(b.array());
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
    public CasualDequeueReplyMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private static DequeueMessage readDequeueMessage(final AsynchronousByteChannel channel)
    {
        try
        {
            UUID msgId = CasualMessageDecoderUtils.readUUID(channel);
            int propertiesSize = (int) ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize()).get().getLong();
            String properties = CasualMessageDecoderUtils.readString(channel, propertiesSize);
            int replyDataSize = (int) ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize()).get().getLong();
            String replyData = CasualMessageDecoderUtils.readString(channel, replyDataSize);
            long availableSinceEpoc = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize()).get().getLong();
            ServiceBuffer serviceBuffer = CasualMessageDecoderUtils.readServiceBuffer(channel, Integer.MAX_VALUE);
            long redelivered = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize()).get().getLong();
            long timeStampSinceEpoc = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize()).get().getLong();
            return DequeueMessage.of(QueueMessage.createBuilder()
                                                 .withId(msgId)
                                                 .withCorrelationInformation(properties)
                                                 .withReplyQueue(replyData)
                                                 .withAvailableSince(availableSinceEpoc)
                                                 .withTimestamp(timeStampSinceEpoc)
                                                 .withRedelivered(redelivered)
                                                 .withPayload(serviceBuffer)
                                                 .build());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualProtocolException("failed reading a DequeueMessage for a CasualDequeueReplyMessage", e);
        }
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

    private static CasualDequeueReplyMessage getMessage(byte[] bytes)
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
                                        .withExecution(execution)
                                        .withMessages(l)
                                        .build();
    }
}
