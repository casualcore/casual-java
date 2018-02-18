package se.kodarkatten.casual.network.protocol.io.readers.queue;

import se.kodarkatten.casual.api.util.Pair;
import se.kodarkatten.casual.network.protocol.io.readers.NetworkReader;
import se.kodarkatten.casual.network.protocol.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.DequeueRequestSizes;
import se.kodarkatten.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;
import se.kodarkatten.casual.network.protocol.utils.ByteUtils;
import se.kodarkatten.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// I don't agree here, in these cases it makes it more readable
@SuppressWarnings("squid:UselessParenthesesCheck")
public final class CasualDequeueRequestMessageReader implements NetworkReader<CasualDequeueRequestMessage>
{
    private CasualDequeueRequestMessageReader()
    {}

    public static CasualDequeueRequestMessageReader of()
    {
        return new CasualDequeueRequestMessageReader();
    }

    @Override
    public CasualDequeueRequestMessage readSingleBuffer(final AsynchronousByteChannel channel, int messageSize)
    {
        CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return getMessage(msgFuture.get().array());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualProtocolException("failed reading CasualDequeueRequestMessage", e);
        }
    }

    @Override
    public CasualDequeueRequestMessage readChunked(final AsynchronousByteChannel channel)
    {
        try
        {
            UUID execution = CasualNetworkReaderUtils.readUUID(channel);
            int queueNameSize = (int) ByteUtils.readFully(channel, DequeueRequestSizes.NAME_SIZE.getNetworkSize()).get().getLong();
            String queueName = CasualNetworkReaderUtils.readString(channel, queueNameSize);
            Xid xid = XIDUtils.readXid(channel);
            int selectorPropertiesSize = (int) ByteUtils.readFully(channel, DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize()).get().getLong();
            String selectorProperties = (0 == selectorPropertiesSize) ? "" : CasualNetworkReaderUtils.readString(channel, selectorPropertiesSize);
            UUID selectorId = CasualNetworkReaderUtils.readUUID(channel);
            boolean block = (1 == (int) ByteUtils.readFully(channel, DequeueRequestSizes.BLOCK.getNetworkSize()).get().get());
            return CasualDequeueRequestMessage.createBuilder()
                                              .withExecution(execution)
                                              .withQueueName(queueName)
                                              .withXid(xid)
                                              .withSelectorProperties(selectorProperties)
                                              .withSelectorUUID(selectorId)
                                              .withBlock(block)
                                              .build();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualProtocolException("failed reading CasualDequeueRequestMessage", e);
        }
    }

    @Override
    public CasualDequeueRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return getMessage(b.array());
    }

    @Override
    public CasualDequeueRequestMessage readChunked(final ReadableByteChannel channel)
    {
        UUID execution = CasualNetworkReaderUtils.readUUID(channel);
        int queueNameSize = (int) ByteUtils.readFully(channel, DequeueRequestSizes.NAME_SIZE.getNetworkSize()).getLong();
        String queueName = CasualNetworkReaderUtils.readString(channel, queueNameSize);
        Xid xid = XIDUtils.readXid(channel);
        int selectorPropertiesSize = (int) ByteUtils.readFully(channel, DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize()).getLong();
        String selectorProperties = (0 == selectorPropertiesSize) ? "" : CasualNetworkReaderUtils.readString(channel, selectorPropertiesSize);
        UUID selectorId = CasualNetworkReaderUtils.readUUID(channel);
        boolean block = (1 == (int) ByteUtils.readFully(channel, DequeueRequestSizes.BLOCK.getNetworkSize()).get());
        return CasualDequeueRequestMessage.createBuilder()
                                          .withExecution(execution)
                                          .withQueueName(queueName)
                                          .withXid(xid)
                                          .withSelectorProperties(selectorProperties)
                                          .withSelectorUUID(selectorId)
                                          .withBlock(block)
                                          .build();
    }

    @Override
    public CasualDequeueRequestMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private static CasualDequeueRequestMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        int queueNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueRequestSizes.NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueRequestSizes.NAME_SIZE.getNetworkSize();
        String queueName = CasualNetworkReaderUtils.getAsString(bytes, currentOffset, queueNameSize);
        currentOffset += queueNameSize;
        Pair<Integer, Xid> xidInfo = CasualNetworkReaderUtils.readXid(bytes, currentOffset);
        currentOffset = xidInfo.first();
        Xid xid = xidInfo.second();
        int selectorPropertiesSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize();
        String selectorProperties = (0 == selectorPropertiesSize) ? "" : CasualNetworkReaderUtils.getAsString(bytes, currentOffset, selectorPropertiesSize);
        currentOffset += selectorPropertiesSize;
        UUID selectorId = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DequeueRequestSizes.SELECTOR_ID_SIZE.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        boolean block = (1 == (int)ByteBuffer.wrap(bytes, currentOffset , DequeueRequestSizes.BLOCK.getNetworkSize()).get());
        return CasualDequeueRequestMessage.createBuilder()
                                          .withExecution(execution)
                                          .withQueueName(queueName)
                                          .withXid(xid)
                                          .withSelectorProperties(selectorProperties)
                                          .withSelectorUUID(selectorId)
                                          .withBlock(block)
                                          .build();
    }
}
