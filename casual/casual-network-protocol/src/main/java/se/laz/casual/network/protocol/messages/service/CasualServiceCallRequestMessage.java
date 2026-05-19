/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.api.xa.XID;
import se.laz.casual.jca.SpanId;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.encoding.utils.HeaderEncoder;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.ServiceCallRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;
import static se.laz.casual.network.ProtocolVersion.VERSION_1_5;

/**
 * Created by aleph on 2017-03-14.
 */
public class CasualServiceCallRequestMessage implements CasualNetworkTransmittable
{
    private static final Logger log = Logger.getLogger( CasualServiceCallRequestMessage.class.getName() );

    private UUID execution;
    private String serviceName;
    private long timeout;
    private SpanId parentSpan;
    private String parentName;
    private Xid xid;
    private Flag<AtmiFlags> xatmiFlags;
    private ServiceBuffer serviceBuffer;
    private ProtocolVersion protocolVersion;

    // not part of the message
    // used for testing
    // so that we can get chunks without having to have a huge message
    // Defaults to Integer.MAX_VALUE
    private int maxMessageSize = Integer.MAX_VALUE;

    private CasualServiceCallRequestMessage()
    {}

    @Override
    public CasualNWMessageType getType()
    {
        if( protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5) )
        {
            return CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_5;
        }
        if( protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3) )
        {
            return CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_3;
        }
        return CasualNWMessageType.SERVICE_CALL_REQUEST;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] serviceNameBytes = serviceName.getBytes(StandardCharsets.UTF_8);
        final byte[] parentNameBytes = parentName.getBytes(StandardCharsets.UTF_8);
        final List<byte[]> serviceBytes = serviceBuffer.toNetworkBytes();

        long messageSize = CommonSizes.EXECUTION.getNetworkSize() +
                           ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize() + serviceNameBytes.length +
                           ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize() + parentNameBytes.length +
                           XIDUtils.getXIDNetworkSize(xid) +
                           ServiceCallRequestSizes.FLAGS.getNetworkSize() +
                           ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);

        final List<byte[]> headersBytes  = HeaderEncoder.convertMapToBytes( serviceBuffer.getHeaders() );

        final long headersBytesSize = ByteUtils.sumNumberOfBytes( headersBytes );

        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ))
        {
            messageSize += CommonSizes.HEADER_SIZE.getNetworkSize() +
                    (CommonSizes.HEADER_ELEMENT_SIZE.getNetworkSize() * (long)headersBytes.size()) + headersBytesSize;
        }
        else if( headersBytesSize != 0 )
        {
            log.warning( ()-> "Headers provided will be lost, they are only supported with protocol version >=1.5. " +
                    "They are not expected with connections using protocol version: " +
                    protocolVersion.getVersionAsString() );
        }

        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
            messageSize += ServiceCallRequestSizes.HAS_VALUE.getNetworkSize();
            if(timeout > 0)
            {
                messageSize += ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize();
            }
            messageSize += ServiceCallRequestSizes.PARENT_SPAN.getNetworkSize();
        }
        else
        {
            messageSize += ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize();
        }
        return (messageSize <= getMaxMessageSize()) ? toNetworkBytesFitsInOneBuffer((int)messageSize, serviceNameBytes, parentNameBytes, serviceBytes, headersBytes)
                                                    : toNetworkBytesMultipleBuffers(serviceNameBytes, parentNameBytes, serviceBuffer, headersBytes);
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public UUID getExecution()
    {
        return execution;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public SpanId getParentSpan()
    {
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
            return parentSpan;
        }
        throw new CasualProtocolException("parentSpan is not supported in protocol version: " + protocolVersion);
    }

    public String getParentName()
    {
        return parentName;
    }

    public Xid getXid()
    {
        return XID.of(xid);
    }

    public Flag<AtmiFlags> getXatmiFlags()
    {
        return Flag.of(xatmiFlags);
    }

    /**
     * Defaults to Integer.MAX_VALUE
     * @return max message size.
     */
    public int getMaxMessageSize()
    {
        return maxMessageSize;
    }

    /**
     * Note, not immutable
     * @return the service buffer.
     */
    public ServiceBuffer getServiceBuffer()
    {
        return serviceBuffer;
    }

    @Override
    public boolean equals( Object o )
    {
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        CasualServiceCallRequestMessage that = (CasualServiceCallRequestMessage) o;
        return timeout == that.timeout && Objects.equals( execution, that.execution ) &&
                Objects.equals( serviceName, that.serviceName ) && Objects.equals( parentSpan, that.parentSpan ) && Objects.equals( parentName, that.parentName ) &&
                Objects.equals( xid, that.xid ) && Objects.equals( xatmiFlags, that.xatmiFlags );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( execution, serviceName, timeout, parentSpan, parentName, xid, xatmiFlags );
    }

    @Override
    public String toString()
    {
        return "CasualServiceCallRequestMessage{" +
                "execution=" + execution +
                ", serviceName='" + serviceName + '\'' +
                ", timeout=" + timeout +
                ", parentSpan=" + (parentSpan == null ? null : parentSpan.asHex()) +
                ", parentName='" + parentName + '\'' +
                ", xid=" + xid +
                ", xatmiFlags=" + xatmiFlags +
                ", serviceBuffer=" + serviceBuffer +
                '}';
    }

    public static class Builder
    {
        private UUID execution;
        private String serviceName;
        private long timeout;
        // optional
        private SpanId parentSpan;
        private String parentName = "";
        private Xid xid;
        private Flag<AtmiFlags> xatmiFlags;
        private ServiceBuffer serviceBuffer;
        private ProtocolVersion protocolVersion;

        public Builder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder setServiceName(String serviceName)
        {
            this.serviceName = serviceName;
            return this;
        }

        public Builder setTimeout(long timeout)
        {
            this.timeout = timeout;
            return this;
        }

        public Builder setParentSpan(SpanId parentSpan)
        {
            this.parentSpan = parentSpan;
            return this;
        }

        public Builder setParentName(String parentName)
        {
            this.parentName = parentName;
            return this;
        }

        public Builder setXid(Xid xid)
        {
            this.xid = xid;
            return this;
        }

        public Builder setXatmiFlags(Flag<AtmiFlags> xatmiFlags)
        {
            this.xatmiFlags = xatmiFlags;
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

        public CasualServiceCallRequestMessage build()
        {
            Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
            CasualServiceCallRequestMessage r = new CasualServiceCallRequestMessage();
            r.execution = execution;
            r.serviceName = serviceName;
            r.timeout = timeout;
            r.parentSpan = parentSpan;
            r.parentName = parentName;
            r.xid = XID.of(xid);
            r.xatmiFlags = xatmiFlags;
            r.serviceBuffer = serviceBuffer;
            r.protocolVersion = protocolVersion;
            return r;
        }
    }

    private List<byte[]> toNetworkBytesFitsInOneBuffer(int messageSize, final byte[] serviceNameBytes, final byte[] parentNameBytes, final List<byte[]> serviceBytes, final List<byte[]> headersBytes )
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putLong(serviceNameBytes.length)
         .put(serviceNameBytes);
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
            byte hasValue = (byte)((timeout > 0) ? 1: 0);
            b.put(hasValue);
            if(timeout > 0)
            {
                b.putLong(timeout);
            }
            b.put(parentSpan.getId());
        }
        else
        {
            b.putLong(timeout);
        }
         b.putLong(parentNameBytes.length)
          .put(parentNameBytes);
        CasualEncoderUtils.writeXID(xid, b);
        b.putLong(xatmiFlags.getFlagValue())
         .putLong(serviceBytes.get(0).length)
         .put(serviceBytes.get(0));
        serviceBytes.remove(0);
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

    private List<byte[]> toNetworkBytesMultipleBuffers(final byte[] serviceNameBytes, final byte[] parentNameBytes, final ServiceBuffer serviceBuffer, final List<byte[]> headersBytes )
    {
        final List<byte[]> l = new ArrayList<>();
        final ByteBuffer executionBuffer = ByteBuffer.allocate(CommonSizes.EXECUTION.getNetworkSize());
        CasualEncoderUtils.writeUUID(execution, executionBuffer);
        l.add(executionBuffer.array());
        l.add(CasualEncoderUtils.writeLong(serviceNameBytes.length));
        l.add(serviceNameBytes);
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
            byte hasValue = (byte)((timeout > 0) ? 1 : 0);
            l.add(CasualEncoderUtils.writeByte(hasValue));
            if(timeout > 0)
            {
                l.add(CasualEncoderUtils.writeLong(timeout));
            }
            l.add(parentSpan.getId());
        }
        else
        {
            l.add(CasualEncoderUtils.writeLong(timeout));
        }
        l.add(CasualEncoderUtils.writeLong(parentNameBytes.length));
        l.add(parentNameBytes);
        final ByteBuffer xidByteBuffer = ByteBuffer.allocate(XIDUtils.getXIDNetworkSize(xid));
        CasualEncoderUtils.writeXID(xid, xidByteBuffer);
        l.add(xidByteBuffer.array());
        l.add(CasualEncoderUtils.writeLong(xatmiFlags.getFlagValue()));
        l.addAll(CasualEncoderUtils.writeServiceBuffer(serviceBuffer));
        if( protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ) )
        {
            l.addAll( HeaderEncoder.encodeHeadersBytesAsList( headersBytes ) );
        }
        return l;
    }

}
