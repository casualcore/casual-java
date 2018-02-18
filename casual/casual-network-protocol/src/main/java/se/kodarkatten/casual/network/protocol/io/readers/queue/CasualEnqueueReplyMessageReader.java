package se.kodarkatten.casual.network.protocol.io.readers.queue;

import se.kodarkatten.casual.network.protocol.io.readers.NetworkReader;
import se.kodarkatten.casual.network.protocol.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.protocol.messages.queue.CasualEnqueueReplyMessage;
import se.kodarkatten.casual.network.protocol.utils.ByteUtils;

import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CasualEnqueueReplyMessageReader implements NetworkReader<CasualEnqueueReplyMessage>
{
    private CasualEnqueueReplyMessageReader()
    {}

    public static CasualEnqueueReplyMessageReader of()
    {
        return new CasualEnqueueReplyMessageReader();
    }

    @Override
    public CasualEnqueueReplyMessage readSingleBuffer(final AsynchronousByteChannel channel, int messageSize)
    {
        CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return getMessage(msgFuture.get().array());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualProtocolException("failed reading CasualEnqueueReplyMessage", e);
        }
    }

    @Override
    public CasualEnqueueReplyMessage readChunked(final AsynchronousByteChannel channel)
    {
        UUID execution = CasualNetworkReaderUtils.readUUID(channel);
        UUID id = CasualNetworkReaderUtils.readUUID(channel);
        return CasualEnqueueReplyMessage.createBuilder()
                                            .withExecution(execution)
                                            .withId(id)
                                            .build();
    }

    @Override
    public CasualEnqueueReplyMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return getMessage(b.array());
    }

    @Override
    public CasualEnqueueReplyMessage readChunked(final ReadableByteChannel channel)
    {
        UUID execution = CasualNetworkReaderUtils.readUUID(channel);
        UUID id = CasualNetworkReaderUtils.readUUID(channel);
        return CasualEnqueueReplyMessage.createBuilder()
                                        .withExecution(execution)
                                        .withId(id)
                                        .build();
    }

    @Override
    public CasualEnqueueReplyMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private static CasualEnqueueReplyMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        UUID id = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + CommonSizes.EXECUTION.getNetworkSize()));
        return CasualEnqueueReplyMessage.createBuilder()
                                        .withExecution(execution)
                                        .withId(id)
                                        .build();
    }

}
