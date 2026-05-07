/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service;

import se.laz.casual.api.buffer.CasualHeaders;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.api.xa.XID;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.encoding.utils.HeaderEncoder;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.ServiceCallReplySizes;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;
import static se.laz.casual.network.ProtocolVersion.VERSION_1_5;

public class CasualServiceCallReplyMessage implements CasualNetworkTransmittable
{
    private UUID execution;
    private ErrorState error;
    private long userDefinedCode;
    // note: xid does not exist in reply starting with protocol version 1.3
    private Xid xid;
    private TransactionState transactionState;
    private ServiceBuffer serviceBuffer;
    private ProtocolVersion protocolVersion;
    private CasualHeaders headers;

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
        if( protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5) )
        {
            return CasualNWMessageType.SERVICE_CALL_REPLY_V_1_5;
        }
        if( protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3) )
        {
            return CasualNWMessageType.SERVICE_CALL_REPLY_V_1_3;
        }
        return CasualNWMessageType.SERVICE_CALL_REPLY;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final List<byte[]> serviceBytes = serviceBuffer.toNetworkBytes();

        long messageSize = CommonSizes.EXECUTION.getNetworkSize() +
                           ServiceCallReplySizes.CALL_ERROR.getNetworkSize() + ServiceCallReplySizes.CALL_CODE.getNetworkSize() +
                           ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize() +
                           ServiceCallReplySizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + ServiceCallReplySizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);
        if(protocolVersion.isLessThan( VERSION_1_3 ) )
        {
            messageSize += XIDUtils.getXIDNetworkSize(xid);
        }
        List<byte[]> headersBytes = HeaderEncoder.convertMapToBytes( headers );
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ))
        {
            messageSize += CommonSizes.HEADER_SIZE.getNetworkSize() +
                    (CommonSizes.HEADER_ELEMENT_SIZE.getNetworkSize() * (long)headersBytes.size()) + ByteUtils.sumNumberOfBytes( headersBytes );
        }
        return (messageSize <= getMaxMessageSize()) ? toNetworkBytesFitsInOneBuffer((int)messageSize, serviceBytes, headersBytes)
                                                    : toNetworkBytesMultipleBuffers(serviceBuffer, headersBytes);
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
        if(protocolVersion.isLessThan( VERSION_1_3 ) )
        {
            return XID.of(xid);
        }
        throw new CasualProtocolException("xid is not supported for protocol version: " + protocolVersion);
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

    public CasualHeaders getHeaders()
    {
        return headers;
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
            transactionState == that.transactionState &&
            Objects.equals( headers, that.headers );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution);
    }

    @Override
    public String toString()
    {
        return "CasualServiceCallReplyMessage{" +
                "execution=" + execution +
                ", error=" + error +
                ", userDefinedCode=" + userDefinedCode +
                ", xid=" + xid +
                ", transactionState=" + transactionState +
                ", serviceBuffer=" + serviceBuffer +
                ", protocolVersion=" + protocolVersion +
                ", headers=" + headers +
                ", maxMessageSize=" + maxMessageSize +
                '}';
    }

    public static class Builder
    {
        private UUID execution;
        private ErrorState error;
        private long userSuppliedError;
        private Xid xid;
        private TransactionState transactionState;
        private ServiceBuffer serviceBuffer;
        private ProtocolVersion protocolVersion;
        private CasualHeaders headers = CasualHeaders.empty();

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

        public Builder setProtocolVersion(ProtocolVersion protocolVersion)
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder setHeaders( CasualHeaders headers )
        {
            this.headers = headers;
            return this;
        }

        public CasualServiceCallReplyMessage build()
        {
            Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
            CasualServiceCallReplyMessage msg = new CasualServiceCallReplyMessage();
            msg.execution = execution;
            msg.error = error;
            msg.userDefinedCode = userSuppliedError;
            msg.protocolVersion = protocolVersion;
            if(protocolVersion.isLessThan( VERSION_1_3 ) )
            {
                msg.xid = XID.of(xid);
            }
            else if(null != xid)
            {
                throw new CasualProtocolException("xid can not be set in protocol version: " + protocolVersion);
            }
            msg.headers = headers;
            msg.transactionState = transactionState;
            msg.serviceBuffer = serviceBuffer;
            return msg;
        }
    }

    private List<byte[]> toNetworkBytesFitsInOneBuffer(int messageSize, List<byte[]> serviceBytes, List<byte[]> headersBytes )
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putInt(error.getValue())
         .putLong(userDefinedCode);
        if(protocolVersion.isLessThan( VERSION_1_3 ) )
        {
            CasualEncoderUtils.writeXID(xid, b);
        }
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

        if( protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ) )
        {
            HeaderEncoder.writeHeaderBytes( b, headersBytes );
        }

        l.add(b.array());
        return l;
    }

    private List<byte[]> toNetworkBytesMultipleBuffers(final ServiceBuffer serviceBuffer, List<byte[]> headersBytes)
    {
        final List<byte[]> l = new ArrayList<>();
        final ByteBuffer executionBuffer = ByteBuffer.allocate(CommonSizes.EXECUTION.getNetworkSize());
        CasualEncoderUtils.writeUUID(execution, executionBuffer);
        l.add(executionBuffer.array());
        l.add(CasualEncoderUtils.writeInt(error.getValue()));
        l.add(CasualEncoderUtils.writeLong(userDefinedCode));
        if(protocolVersion.isLessThan( VERSION_1_3 ) )
        {
            // note we put the transaction state as well here
            final ByteBuffer xidByteBuffer = ByteBuffer.allocate(XIDUtils.getXIDNetworkSize(xid) + ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize());
            CasualEncoderUtils.writeXID(xid, xidByteBuffer);
            xidByteBuffer.put((byte) (transactionState.getId() & 0xff));
            l.add(xidByteBuffer.array());
        }
        else
        {
            ByteBuffer transactionStateBuffer = ByteBuffer.allocate(ServiceCallReplySizes.TRANSACTION_STATE.getNetworkSize());
            transactionStateBuffer.put((byte) (transactionState.getId() & 0xff));
            l.add(transactionStateBuffer.array());
        }
        l.addAll(CasualEncoderUtils.writeServiceBuffer(serviceBuffer));
        if( protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ) )
        {
            l.addAll( HeaderEncoder.encodeHeadersBytesAsList( headersBytes ) );
        }
        return l;
    }

}
