package se.kodarkatten.casual.network.messages.reply.domain.service;

import se.kodarkatten.casual.api.flags.ErrorState;
import se.kodarkatten.casual.api.flags.TransactionState;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.common.ServiceBuffer;
import se.kodarkatten.casual.network.messages.parseinfo.ServiceCallReplySizes;
import se.kodarkatten.casual.network.utils.ByteUtils;
import se.kodarkatten.casual.network.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-21.
 */
public class CasualServiceCallReplyMessage implements CasualNetworkTransmittable
{
    private UUID execution;
    private int callDescriptor;
    private ErrorState error;
    private long userSuppliedError;
    private Xid xid;
    private TransactionState transactionState;
    private ServiceBuffer serviceBuffer;

    // not part of the message
    // used for testing
    // so that we can get chunks without having to have a huge message
    // Defaults to Integer.MAX_VALUE
    private int maxMessageSize = Integer.MAX_VALUE;

    private CasualServiceCallReplyMessage()
    {}

    public static Builder createBuilder()
    {
        return new Builder();
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.SERVICE_CALL_REPLY;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final long messageSize = ServiceCallReplySizes.EXECUTION.getNetworkSize() +
            ServiceCallReplySizes.CALL_DESCRIPTOR.getNetworkSize() +
            ServiceCallReplySizes.CALL_ERROR.getNetworkSize() + ServiceCallReplySizes.CALL_CODE.getNetworkSize() +
            XIDUtils.getXIDNetworkSize(xid) +
            ServiceCallReplySizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + + ServiceCallReplySizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);
        return (messageSize <= getMaxMessageSize()) ? toNetworkBytesFitsInOneBuffer((int)messageSize, serviceNameBytes, parentNameBytes, serviceBytes)
            : toNetworkBytesMultipleBuffers(serviceNameBytes, parentNameBytes, serviceBytes);
    }

    public UUID getExecution()
    {
        return execution;
    }

    public int getCallDescriptor()
    {
        return callDescriptor;
    }

    public ErrorState getError()
    {
        return error;
    }

    public long getUserSuppliedError()
    {
        return userSuppliedError;
    }

    public Xid getXid()
    {
        return XID.of(xid);
    }

    public TransactionState getTransactionState()
    {
        return transactionState;
    }

    /**
     * Note, mutable since payload can be huge
     * @return
     */
    public ServiceBuffer getServiceBuffer()
    {
        return serviceBuffer;
    }

    public int getMaxMessageSize()
    {
        return maxMessageSize;
    }

    public CasualServiceCallReplyMessage setMaxMessageSize(int maxMessageSize)
    {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    public static class Builder
    {
        private UUID execution;
        private int callDescriptor;
        private ErrorState error;
        private long userSuppliedError;
        private XID xid;
        private TransactionState transactionState;
        private ServiceBuffer serviceBuffer;

        public Builder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder setCallDescriptor(int callDescriptor)
        {
            this.callDescriptor = callDescriptor;
            return this;
        }

        public Builder setError(ErrorState error)
        {
            this.error = error;
            return this;
        }

        public Builder setUserSuppliedError(long userSuppliedError)
        {
            this.userSuppliedError = userSuppliedError;
            return this;
        }

        public Builder setXid(XID xid)
        {
            this.xid = xid;
            return this;
        }

        public Builder setTransactionState(TransactionState transactionState)
        {
            this.transactionState = transactionState;
            return this;
        }

        public Builder setServiceBuffer(ServiceBuffer serviceBuffer)
        {
            this.serviceBuffer = serviceBuffer;
            return this;
        }
        public CasualServiceCallReplyMessage build()
        {
            CasualServiceCallReplyMessage msg = new CasualServiceCallReplyMessage();
            msg.execution = execution;
            msg.callDescriptor = callDescriptor;
            msg.error = error;
            msg.userSuppliedError = userSuppliedError;
            msg.xid = XID.of(xid);
            msg.transactionState = transactionState;
            msg.serviceBuffer = serviceBuffer;
            return msg;
        }
    }

}
