/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.xa.XID;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ServiceCallReplySizes;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-21.
 */
public class CasualServiceCallReplyMessage implements CasualNetworkTransmittable
{
    private UUID execution;
    private ErrorState error;
    private long userDefinedCode;
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
        final List<byte[]> serviceBytes = serviceBuffer.toNetworkBytes();

        final long messageSize = ServiceCallReplySizes.EXECUTION.getNetworkSize() +
                                 ServiceCallReplySizes.CALL_ERROR.getNetworkSize() + ServiceCallReplySizes.CALL_CODE.getNetworkSize() +
                                 XIDUtils.getXIDNetworkSize(xid) +
                                 ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize() +
                                 ServiceCallReplySizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + ServiceCallReplySizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);
        return (messageSize <= getMaxMessageSize()) ? toNetworkBytesFitsInOneBuffer((int)messageSize, serviceBytes)
                                                    : toNetworkBytesMultipleBuffers(serviceBuffer);
    }

    public UUID getExecution()
    {
        return execution;
    }

    public ErrorState getError()
    {
        return error;
    }

    public long getUserDefinedCode()
    {
        return userDefinedCode;
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
     * It may also be null to handle empty payload.
     * 
     * @return service buffer or null if not present.
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
        CasualServiceCallReplyMessage that = (CasualServiceCallReplyMessage) o;
        return userDefinedCode == that.userDefinedCode &&
            Objects.equals(execution, that.execution) &&
            error == that.error &&
            Objects.equals(xid, that.xid) &&
            transactionState == that.transactionState;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualServiceCallReplyMessage{");
        sb.append("execution=").append(execution);
        sb.append(", error=").append(error);
        sb.append(", userDefinedCode=").append(userDefinedCode);
        sb.append(", xid=").append(xid);
        sb.append(", transactionState=").append(transactionState);
        sb.append(", serviceBuffer=").append(serviceBuffer);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder
    {
        private UUID execution;
        private ErrorState error;
        private long userSuppliedError;
        private Xid xid;
        private TransactionState transactionState;
        private ServiceBuffer serviceBuffer;

        public Builder setExecution(UUID execution)
        {
            this.execution = execution;
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

        public Builder setXid(Xid xid)
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
            msg.error = error;
            msg.userDefinedCode = userSuppliedError;
            msg.xid = XID.of(xid);
            msg.transactionState = transactionState;
            msg.serviceBuffer = serviceBuffer;
            return msg;
        }
    }

    private List<byte[]> toNetworkBytesFitsInOneBuffer(int messageSize, List<byte[]> serviceBytes)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putInt(error.getValue())
         .putLong(userDefinedCode);
        CasualEncoderUtils.writeXID(xid, b);
        if (serviceBytes.isEmpty())
        {
            b.put((byte) (transactionState.getId() & 0xff))
             .putLong(0);
        }
        else
        {
            b.put((byte) (transactionState.getId() & 0xff))
             .putLong(serviceBytes.get(0).length)
             .put(serviceBytes.get(0));
            serviceBytes.remove(0);
        }
        final long payloadSize = ByteUtils.sumNumberOfBytes(serviceBytes);
        b.putLong(payloadSize);
        serviceBytes.forEach(b::put);
        l.add(b.array());
        return l;
    }

    private List<byte[]> toNetworkBytesMultipleBuffers(final ServiceBuffer serviceBuffer)
    {
        final List<byte[]> l = new ArrayList<>();
        final ByteBuffer executionBuffer = ByteBuffer.allocate(ServiceCallReplySizes.EXECUTION.getNetworkSize());
        CasualEncoderUtils.writeUUID(execution, executionBuffer);
        l.add(executionBuffer.array());
        l.add(CasualEncoderUtils.writeInt(error.getValue()));
        l.add(CasualEncoderUtils.writeLong(userDefinedCode));
        // note we put the transaction state as well here
        final ByteBuffer xidByteBuffer = ByteBuffer.allocate(XIDUtils.getXIDNetworkSize(xid) + ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize());
        CasualEncoderUtils.writeXID(xid, xidByteBuffer);
        xidByteBuffer.put((byte)(transactionState.getId() & 0xff ));
        l.add(xidByteBuffer.array());
        l.addAll(CasualEncoderUtils.writeServiceBuffer(serviceBuffer));
        return l;
    }

}
