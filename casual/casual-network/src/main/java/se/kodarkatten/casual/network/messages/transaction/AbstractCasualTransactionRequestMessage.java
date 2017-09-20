package se.kodarkatten.casual.network.messages.transaction;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.network.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.parseinfo.CommonSizes;

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

    protected AbstractCasualTransactionRequestMessage(final UUID execution, final Xid xid, long resourceId, int flags)
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
        l.add(CasualNetworkWriterUtils.writeLong(flags));
    }

}
