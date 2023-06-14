/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.transaction;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.XAFlags;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public abstract class AbstractCasualTransactionRequestDecoder<T extends CasualNetworkTransmittable> implements NetworkDecoder<T>
{
    @Override
    public T readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        ByteBuf buffer = Unpooled.wrappedBuffer(ByteUtils.readFully(channel, messageSize).array());
        T msg = createRequestMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public T readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        final Xid xid = XIDUtils.readXid(channel);
        final int resourceId = ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize()).getInt();
        final int flagValue = (int)ByteUtils.readFully(channel, CommonSizes.TRANSACTION_RESOURCE_FLAGS.getNetworkSize()).getLong();
        final Flag<XAFlags> flags = new Flag.Builder<XAFlags>(flagValue).build();
        return createTransactionRequestMessage(execution, xid, resourceId, flags);
    }

    @Override
    public T readSingleBuffer(final ByteBuf buffer)
    {
        return createRequestMessage(buffer);
    }

    protected abstract T createTransactionRequestMessage(final UUID execution, final Xid xid, int resourceId, final Flag<XAFlags> flags);

    private T createRequestMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        final Xid xid = CasualMessageDecoderUtils.readXid(buffer);
        final int resourceId = buffer.readInt();
        final int flagValue = (int)buffer.readLong();
        final Flag<XAFlags> flags = new Flag.Builder<XAFlags>(flagValue).build();
        return createTransactionRequestMessage(execution, xid, resourceId, flags);
    }





}
