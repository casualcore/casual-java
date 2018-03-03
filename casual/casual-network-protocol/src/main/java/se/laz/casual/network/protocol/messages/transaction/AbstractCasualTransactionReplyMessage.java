/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.transaction;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.xa.XAReturnCode;
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
public abstract class AbstractCasualTransactionReplyMessage extends AbstractCasualTransactionMessage
{
    private final XAReturnCode xaReturnCode;

    protected AbstractCasualTransactionReplyMessage(final UUID execution, final Xid xid, int resourceId, final XAReturnCode xaReturnCode)
    {
        super(execution, xid, resourceId);
        this.xaReturnCode = xaReturnCode;
    }

    public XAReturnCode getTransactionReturnCode()
    {
        return xaReturnCode;
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
        AbstractCasualTransactionReplyMessage that = (AbstractCasualTransactionReplyMessage) o;
        return resourceId == that.resourceId &&
               xaReturnCode == that.xaReturnCode &&
               Objects.equals(execution, that.execution) &&
               Objects.equals(xid, that.xid);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, xid, resourceId, xaReturnCode);
    }

    @Override
    protected abstract CasualNWMessageType getTransactionType();

    @Override
    protected int getMessageExtraDataSize()
    {
        return CommonSizes.TRANSACTION_RESOURCE_STATE.getNetworkSize();
    }

    @Override
    protected void createNetworkBytesFitsInOneBuffer(ByteBuffer b)
    {
        b.putInt(xaReturnCode.getId());
    }

    @Override
    protected void createNetworkBytesMultipleBuffers(List<byte[]> l)
    {
        l.add(CasualEncoderUtils.writeInt(xaReturnCode.getId()));
    }
}
