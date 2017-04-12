package se.kodarkatten.casual.network.io.readers.transaction;


import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.network.io.readers.NetworkReader;
import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.utils.ByteUtils;
import se.kodarkatten.casual.network.utils.Pair;
import se.kodarkatten.casual.network.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-04-03.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public abstract class AbstractCasualTransactionRequestReader<T> implements NetworkReader<T>
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
            throw new CasualTransportException("failed reading CasualServiceCallRequestMessage", e);
        }
    }

    @Override
    public T readChunked(final AsynchronousByteChannel channel)
    {
        try
        {
            final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
            final Xid xid = XIDUtils.readXid(channel);
            final long resourceId = ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize()).get().getLong();
            final int flagValue = (int)ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_FLAGS.getNetworkSize()).get().getLong();
            final Flag<XAFlags> flags = new Flag.Builder(flagValue).build();
            return createTransactionRequestMessage(execution, xid, resourceId, flags);
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualTransactionMessage", e);
        }
    }

    protected abstract T createTransactionRequestMessage(final UUID execution, final Xid xid, long resourceId, final Flag<XAFlags> flags);

    private T createRequestMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();

        Pair<Integer, Xid> xidInfo = CasualNetworkReaderUtils.readXid(data, currentOffset);
        currentOffset = xidInfo.first();
        final Xid xid = xidInfo.second();
        final ByteBuffer resourceIdBuffer = ByteBuffer.wrap(data, currentOffset, CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize());
        final long resourceId = resourceIdBuffer.getLong();
        currentOffset += CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize();
        final ByteBuffer flagBuffer = ByteBuffer.wrap(data, currentOffset, CommonSizes.TRANSACTION_RESOURCE_FLAGS.getNetworkSize());
        final int flagValue = (int)flagBuffer.getLong();
        final Flag<XAFlags> flags = new Flag.Builder(flagValue).build();
        return createTransactionRequestMessage(execution, xid, resourceId, flags);
    }





}
