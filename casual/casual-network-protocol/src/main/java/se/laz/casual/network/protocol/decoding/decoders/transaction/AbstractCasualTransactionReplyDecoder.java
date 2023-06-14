/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.transaction;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.util.Pair;
import se.laz.casual.api.xa.XAReturnCode;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

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
        ByteBuf buffer = Unpooled.wrappedBuffer(ByteUtils.readFully(channel, messageSize).array());
        T msg = createReplyMessage(buffer);
        buffer.release();
        return msg;
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
    public T readSingleBuffer(final ByteBuf buffer)
    {
        return createReplyMessage(buffer);
    }

    protected abstract T createTransactionReplyMessage(UUID execution, Xid xid, int resourceId, XAReturnCode r);


    private T createReplyMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        final Xid xid = CasualMessageDecoderUtils.readXid(buffer);
        final int resourceId = buffer.readInt();
        final int xaReturnCode = buffer.readInt();
        final XAReturnCode r = XAReturnCode.unmarshal(xaReturnCode);
        return createTransactionReplyMessage(execution, xid, resourceId, r);
    }

}
