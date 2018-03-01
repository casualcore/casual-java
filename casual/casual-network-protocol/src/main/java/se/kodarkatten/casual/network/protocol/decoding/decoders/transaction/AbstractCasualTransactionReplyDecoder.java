package se.kodarkatten.casual.network.protocol.decoding.decoders.transaction;

import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.api.util.Pair;
import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.kodarkatten.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.protocol.utils.ByteUtils;
import se.kodarkatten.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public abstract class AbstractCasualTransactionReplyDecoder<T extends CasualNetworkTransmittable> implements NetworkDecoder<T>
{
    @Override
    public T readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        return createReplyMessage(ByteUtils.readFully(channel, messageSize).array());
    }

    @Override
    public T readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        final Xid xid = XIDUtils.readXid(channel);
        final int resourceId = ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize()).getInt();
        final int xaReturnCode = ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_STATE.getNetworkSize()).getInt();
        final XAReturnCode r = XAReturnCode.unmarshal(xaReturnCode);
        return createTransactionReplyMessage(execution, xid, resourceId, r);
    }

    @Override
    public T readSingleBuffer(final byte[] data)
    {
        return createReplyMessage(data);
    }

    protected abstract T createTransactionReplyMessage(UUID execution, Xid xid, int resourceId, XAReturnCode r);


    private T createReplyMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();

        Pair<Integer, Xid> xidInfo = CasualMessageDecoderUtils.readXid(data, currentOffset);
        currentOffset = xidInfo.first();
        final Xid xid = xidInfo.second();

        final ByteBuffer resourceIdBuffer = ByteBuffer.wrap(data, currentOffset, CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize());
        final int resourceId = resourceIdBuffer.getInt();
        currentOffset += CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize();
        final int xaReturnCode = ByteBuffer.wrap(data, currentOffset, CommonSizes.TRANSACTION_RESOURCE_STATE.getNetworkSize()).getInt();
        final XAReturnCode r = XAReturnCode.unmarshal(xaReturnCode);
        return createTransactionReplyMessage(execution, xid, resourceId, r);
    }

}
