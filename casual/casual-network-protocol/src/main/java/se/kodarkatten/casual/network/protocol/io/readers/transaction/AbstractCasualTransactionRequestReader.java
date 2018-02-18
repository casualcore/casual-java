package se.kodarkatten.casual.network.protocol.io.readers.transaction;


import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.api.util.Pair;
import se.kodarkatten.casual.network.protocol.io.readers.NetworkReader;
import se.kodarkatten.casual.network.protocol.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.protocol.utils.ByteUtils;
import se.kodarkatten.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-04-03.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public abstract class AbstractCasualTransactionRequestReader<T extends CasualNetworkTransmittable> implements NetworkReader<T>
{
    @Override
    public T readSingleBuffer(final AsynchronousByteChannel channel, int messageSize)
    {
        final CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return createRequestMessage(msgFuture.get().array());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualProtocolException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

    @Override
    public T readChunked(final AsynchronousByteChannel channel)
    {
        try
        {
            final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
            final Xid xid = XIDUtils.readXid(channel);
            final int resourceId = ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize()).get().getInt();
            final int flagValue = (int)ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_FLAGS.getNetworkSize()).get().getLong();
            final Flag<XAFlags> flags = new Flag.Builder<XAFlags>(flagValue).build();
            return createTransactionRequestMessage(execution, xid, resourceId, flags);
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualProtocolException("failed reading CasualTransactionMessage", e);
        }
    }

    @Override
    public T readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        return createRequestMessage(ByteUtils.readFully(channel, messageSize).array());
    }

    @Override
    public T readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
        final Xid xid = XIDUtils.readXid(channel);
        final int resourceId = ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize()).getInt();
        final int flagValue = (int)ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_FLAGS.getNetworkSize()).getLong();
        final Flag<XAFlags> flags = new Flag.Builder<XAFlags>(flagValue).build();
        return createTransactionRequestMessage(execution, xid, resourceId, flags);
    }

    @Override
    public T readSingleBuffer(final byte[] data)
    {
        return createRequestMessage(data);
    }

    protected abstract T createTransactionRequestMessage(final UUID execution, final Xid xid, int resourceId, final Flag<XAFlags> flags);

    private T createRequestMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();

        Pair<Integer, Xid> xidInfo = CasualNetworkReaderUtils.readXid(data, currentOffset);
        currentOffset = xidInfo.first();
        final Xid xid = xidInfo.second();
        final ByteBuffer resourceIdBuffer = ByteBuffer.wrap(data, currentOffset, CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize());
        final int resourceId = resourceIdBuffer.getInt();
        currentOffset += CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize();
        final ByteBuffer flagBuffer = ByteBuffer.wrap(data, currentOffset, CommonSizes.TRANSACTION_RESOURCE_FLAGS.getNetworkSize());
        final int flagValue = (int)flagBuffer.getLong();
        final Flag<XAFlags> flags = new Flag.Builder<XAFlags>(flagValue).build();
        return createTransactionRequestMessage(execution, xid, resourceId, flags);
    }





}
