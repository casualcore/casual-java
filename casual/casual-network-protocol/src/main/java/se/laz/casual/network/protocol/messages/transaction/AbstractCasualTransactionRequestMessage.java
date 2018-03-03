/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.transaction;

import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.XAFlags;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
public abstract class AbstractCasualTransactionRequestMessage extends AbstractCasualTransactionMessage
{
    private int flags;

    protected AbstractCasualTransactionRequestMessage(final UUID execution, final Xid xid, int resourceId, int flags)
    {
        super(execution, xid, resourceId);
        this.flags = flags;
    }

    public Flag<XAFlags> getFlags()
    {
        return new Flag.Builder<XAFlags>(flags).build();
    }

    @Override
    public CasualNWMessageType getType()
    {
        return getTransactionType();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        AbstractCasualTransactionRequestMessage that = (AbstractCasualTransactionRequestMessage) o;
        return resourceId == that.resourceId &&
               flags == that.flags &&
               Objects.equals(execution, that.execution) &&
               Objects.equals(xid, that.xid);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, xid, resourceId, flags);
    }

    @Override
    protected abstract CasualNWMessageType getTransactionType();

    @Override
    protected int getMessageExtraDataSize()
    {
        return CommonSizes.TRANSACTION_RESOURCE_FLAGS.getNetworkSize();
    }

    @Override
    protected void createNetworkBytesFitsInOneBuffer(ByteBuffer b)
    {
        b.putLong(flags);
    }

    @Override
    protected void createNetworkBytesMultipleBuffers(List<byte[]> l)
    {
        l.add(CasualEncoderUtils.writeLong(flags));
    }

}
