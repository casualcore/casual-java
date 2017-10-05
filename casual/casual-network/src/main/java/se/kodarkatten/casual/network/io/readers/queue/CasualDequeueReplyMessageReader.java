package se.kodarkatten.casual.network.io.readers.queue;

import se.kodarkatten.casual.api.util.Pair;
import se.kodarkatten.casual.network.io.readers.NetworkReader;
import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.messages.parseinfo.DequeueReplySizes;
import se.kodarkatten.casual.network.messages.queue.CasualDequeueReplyMessage;
import se.kodarkatten.casual.network.messages.queue.DequeueMessage;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class CasualDequeueReplyMessageReader  implements NetworkReader<CasualDequeueReplyMessage>
{
    private CasualDequeueReplyMessageReader()
    {}

    public static CasualDequeueReplyMessageReader of()
    {
        return new CasualDequeueReplyMessageReader();
    }

    @Override
    public CasualDequeueReplyMessage readSingleBuffer(final AsynchronousByteChannel channel, int messageSize)
    {
        CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return getMessage(msgFuture.get().array());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualDequeueReplyMessage", e);
        }
    }

    @Override
    public CasualDequeueReplyMessage readChunked(final AsynchronousByteChannel channel)
    {
        try
        {
            UUID execution = CasualNetworkReaderUtils.readUUID(channel);
            int numberOfMessages = (int) ByteUtils.readFully(channel, DequeueReplySizes.NUMBER_OF_MESSAGES.getNetworkSize()).get().getLong();
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
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualDequeueReplyMessage", e);
        }
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
        UUID execution = CasualNetworkReaderUtils.readUUID(channel);
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

    private static DequeueMessage readDequeueMessage(final AsynchronousByteChannel channel)
    {
        try
        {
            UUID msgId = CasualNetworkReaderUtils.readUUID(channel);
            int propertiesSize = (int) ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize()).get().getLong();
            String properties = CasualNetworkReaderUtils.readString(channel, propertiesSize);
            int replyDataSize = (int) ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize()).get().getLong();
            String replyData = CasualNetworkReaderUtils.readString(channel, replyDataSize);
            long availableSinceEpoc = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize()).get().getLong();
            ServiceBuffer serviceBuffer = CasualNetworkReaderUtils.readServiceBuffer(channel, Integer.MAX_VALUE);
            long redelivered = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize()).get().getLong();
            long timeStampSinceEpoc = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize()).get().getLong();
            return DequeueMessage.createBuilder()
                                 .withId(msgId)
                                 .withProperties(properties)
                                 .withReplyData(replyData)
                                 .withPayload(serviceBuffer)
                                 .withAvailableForDequeueSince(availableSinceEpoc)
                                 .withNumberOfRedeliveries(redelivered)
                                 .withTimestamp(timeStampSinceEpoc)
                                 .build();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading a DequeueMessage for a CasualDequeueReplyMessage", e);
        }
    }

    private static DequeueMessage readDequeueMessage(final ReadableByteChannel channel)
    {
        UUID msgId = CasualNetworkReaderUtils.readUUID(channel);
        int propertiesSize = (int) ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize()).getLong();
        String properties = CasualNetworkReaderUtils.readString(channel, propertiesSize);
        int replyDataSize = (int) ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize()).getLong();
        String replyData = CasualNetworkReaderUtils.readString(channel, replyDataSize);
        long availableSinceEpoc = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize()).getLong();
        ServiceBuffer serviceBuffer = CasualNetworkReaderUtils.readServiceBuffer(channel, Integer.MAX_VALUE);
        long redelivered = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize()).getLong();
        long timestampSinceEpoc = ByteUtils.readFully(channel, DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize()).getLong();
        return DequeueMessage.createBuilder()
                             .withId(msgId)
                             .withProperties(properties)
                             .withReplyData(replyData)
                             .withPayload(serviceBuffer)
                             .withAvailableForDequeueSince(availableSinceEpoc)
                             .withNumberOfRedeliveries(redelivered)
                             .withTimestamp(timestampSinceEpoc)
                             .build();
    }

    private static Pair<Integer, DequeueMessage> readDequeueMessage(final byte[] bytes, int offset)
    {
        int currentOffset = offset;
        UUID msgId = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DequeueReplySizes.MESSAGE_ID.getNetworkSize()));
        currentOffset += DequeueReplySizes.MESSAGE_ID.getNetworkSize();
        int propertiesSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize();
        String properties = CasualNetworkReaderUtils.getAsString(bytes, currentOffset, propertiesSize);
        currentOffset += propertiesSize;

        int replyDataSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize();
        String replyData = CasualNetworkReaderUtils.getAsString(bytes, currentOffset, replyDataSize);
        currentOffset += replyDataSize;

        long availableSinceEpoc = ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize();

        Pair<Integer, ServiceBuffer> p = CasualNetworkReaderUtils.readServiceBuffer(bytes, currentOffset);
        currentOffset = p.first();

        long redelivered = ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize();
        long timestampSinceEpoc = ByteBuffer.wrap(bytes, currentOffset , DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize()).getLong();
        currentOffset += DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize();
        final DequeueMessage msg = DequeueMessage.createBuilder()
                                                 .withId(msgId)
                                                 .withProperties(properties)
                                                 .withReplyData(replyData)
                                                 .withPayload(p.second())
                                                 .withAvailableForDequeueSince(availableSinceEpoc)
                                                 .withNumberOfRedeliveries(redelivered)
                                                 .withTimestamp(timestampSinceEpoc)
                                                 .build();
        return Pair.of(currentOffset, msg);
    }

    private static CasualDequeueReplyMessage getMessage(byte[] bytes)
    {
        int currentOffset = 0;
        UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
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
