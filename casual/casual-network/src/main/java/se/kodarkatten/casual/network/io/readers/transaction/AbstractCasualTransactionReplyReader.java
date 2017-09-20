package se.kodarkatten.casual.network.io.readers.transaction;

import se.kodarkatten.casual.api.util.Pair;
import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.network.io.readers.NetworkReader;
import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.utils.ByteUtils;
import se.kodarkatten.casual.network.utils.XIDUtils;

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
public abstract class AbstractCasualTransactionReplyReader<T extends CasualNetworkTransmittable> implements NetworkReader<T>
{
    @Override
    public T readSingleBuffer(final AsynchronousByteChannel channel, int messageSize)
    {
        final CompletableFuture<ByteBuffer> msgFuture = ByteUtils.readFully(channel, messageSize);
        try
        {
            return createReplyMessage(msgFuture.get().array());
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
            final int xaReturnCode = (int)ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_STATE.getNetworkSize()).get().getLong();
            final XAReturnCode r = XAReturnCode.unmarshal(xaReturnCode);
            return createTransactionReplyMessage(execution, xid, resourceId, r);
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading CasualTransactionMessage", e);
        }
    }

    @Override
    public T readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        return createReplyMessage(ByteUtils.readFully(channel, messageSize).array());
    }

    public T readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualNetworkReaderUtils.readUUID(channel);
        final Xid xid = XIDUtils.readXid(channel);
        final long resourceId = ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize()).getLong();
        final int xaReturnCode = (int)ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_STATE.getNetworkSize()).getLong();
        final XAReturnCode r = XAReturnCode.unmarshal(xaReturnCode);
        return createTransactionReplyMessage(execution, xid, resourceId, r);
    }

    protected abstract T createTransactionReplyMessage(UUID execution, Xid xid, long resourceId, XAReturnCode r);


    private T createReplyMessage(final byte[] data)
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
        final int xaReturnCode = (int)ByteBuffer.wrap(data, currentOffset, CommonSizes.TRANSACTION_RESOURCE_STATE.getNetworkSize()).getLong();
        final XAReturnCode r = XAReturnCode.unmarshal(xaReturnCode);
        return createTransactionReplyMessage(execution, xid, resourceId, r);
    }

}
