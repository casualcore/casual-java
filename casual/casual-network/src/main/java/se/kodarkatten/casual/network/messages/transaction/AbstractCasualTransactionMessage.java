package se.kodarkatten.casual.network.messages.transaction;

import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by aleph on 2017-04-03.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public abstract class AbstractCasualTransactionMessage implements CasualNetworkTransmittable
{
    protected final UUID execution;
    protected final Xid xid;
    protected final int resourceId;

    // not part of the message
    // used for testing
    // so that we can get chunks without having to have a huge message
    // Defaults to Integer.MAX_VALUE
    private int maxMessageSize = Integer.MAX_VALUE;

    protected AbstractCasualTransactionMessage(final UUID execution, final Xid xid, int resourceId)
    {
        this.execution = execution;
        this.xid = xid;
        this.resourceId = resourceId;
    }

    public int getMaxMessageSize()
    {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize)
    {
        this.maxMessageSize = maxMessageSize;
    }

    public UUID getExecution()
    {
        return execution;
    }

    public Xid getXid()
    {
        return XID.of(xid);
    }

    public int getResourceId()
    {
        return resourceId;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return getTransactionType();
    }

    /**
     * These are always tiny, you will only ever get one byte[]
     * @return
     */
    @Override
    public List<byte[]> toNetworkBytes()
    {
        final int messageSize = getMessageSize();
        return (messageSize <= getMaxMessageSize()) ? toNetworkBytesFitsInOneBuffer(messageSize)
                                                    : toNetworkBytesMultipleBuffers();
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
        AbstractCasualTransactionMessage that = (AbstractCasualTransactionMessage) o;
        return resourceId == that.resourceId &&
               Objects.equals(execution, that.execution) &&
               Objects.equals(xid, that.xid);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, xid, resourceId);
    }

    protected abstract CasualNWMessageType getTransactionType();
    protected abstract int getMessageExtraDataSize();
    protected abstract void createNetworkBytesFitsInOneBuffer(ByteBuffer b);
    protected abstract void createNetworkBytesMultipleBuffers(List<byte[]> l);

    private int getMessageSize()
    {
        return CommonSizes.EXECUTION.getNetworkSize() + XIDUtils.getXIDNetworkSize(xid) +
               CommonSizes.TRANSACTION_RESOURCE_ID.getNetworkSize() + getMessageExtraDataSize();
    }

    private List<byte[]> toNetworkBytesFitsInOneBuffer(int messageSize)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualNetworkWriterUtils.writeUUID(execution, b);
        CasualNetworkWriterUtils.writeXID(xid, b);
        b.putInt(resourceId);
        createNetworkBytesFitsInOneBuffer(b);
        l.add(b.array());
        return l;
    }

    private List<byte[]> toNetworkBytesMultipleBuffers()
    {
        final List<byte[]> l = new ArrayList<>();
        final ByteBuffer executionBuffer = ByteBuffer.allocate(CommonSizes.EXECUTION.getNetworkSize());
        CasualNetworkWriterUtils.writeUUID(execution, executionBuffer);
        l.add(executionBuffer.array());
        final ByteBuffer xidByteBuffer = ByteBuffer.allocate(XIDUtils.getXIDNetworkSize(xid));
        CasualNetworkWriterUtils.writeXID(xid, xidByteBuffer);
        l.add(xidByteBuffer.array());
        l.add(CasualNetworkWriterUtils.writeInt(resourceId));
        createNetworkBytesMultipleBuffers(l);
        return l;
    }

}
