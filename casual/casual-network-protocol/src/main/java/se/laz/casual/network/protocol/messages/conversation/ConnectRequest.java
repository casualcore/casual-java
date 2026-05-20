/*
 * Copyright (c) 2021 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.conversation;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.conversation.Duplex;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.xa.XID;
import se.laz.casual.jca.SpanId;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationConnectRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;

public class ConnectRequest implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final String serviceName;
    private final SpanId parentSpan;
    private final ProtocolVersion protocolVersion;
    private long timeout;
    private final String parentName;
    private final Xid xid;
    private final Duplex duplex;
    private final ServiceBuffer serviceBuffer;

    // private constructor, only used by the builder of this class
    @SuppressWarnings("squid:S00107")
    private ConnectRequest(UUID execution, String serviceName, long timeout, SpanId parentSpan, String parentName, Xid xid, Duplex duplex, ServiceBuffer serviceBuffer, ProtocolVersion protocolVersion)
    {
        this.execution = execution;
        this.serviceName = serviceName;
        this.timeout = timeout;
        this.parentSpan = parentSpan;
        this.parentName = parentName;
        this.xid = xid;
        this.duplex = duplex;
        this.serviceBuffer = serviceBuffer;
        this.protocolVersion = protocolVersion;
    }
    @Override
    public CasualNWMessageType getType()
    {
        return protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 )
                ? CasualNWMessageType.CONVERSATION_CONNECT_V_1_3
                : CasualNWMessageType.CONVERSATION_CONNECT;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] serviceNameBytes = serviceName.getBytes(StandardCharsets.UTF_8);
        final byte[] parentNameBytes = parentName.getBytes(StandardCharsets.UTF_8);
        final List<byte[]> serviceBytes = serviceBuffer.toNetworkBytes();
        long messageSize = CommonSizes.EXECUTION.getNetworkSize() +
                ConversationConnectRequestSizes.SERVICE_NAME_SIZE.getNetworkSize() + serviceNameBytes.length +
                ConversationConnectRequestSizes.PARENT_NAME_SIZE.getNetworkSize() + parentNameBytes.length +
                XIDUtils.getXIDNetworkSize(xid) +
                ConversationConnectRequestSizes.DUPLEX.getNetworkSize() +
                ConversationConnectRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + ConversationConnectRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
            messageSize += ConversationConnectRequestSizes.HAS_VALUE.getNetworkSize();
            if(timeout > 0)
            {
                messageSize += ConversationConnectRequestSizes.SERVICE_TIMEOUT.getNetworkSize();
            }
            messageSize += ConversationConnectRequestSizes.PARENT_SPAN_SIZE.getNetworkSize();
        }
        else
        {
            messageSize += ConversationConnectRequestSizes.SERVICE_TIMEOUT.getNetworkSize();
        }
        return toNetworkBytes((int)messageSize, serviceNameBytes, parentNameBytes, serviceBytes);
    }

    public static ConnectRequestBuilder createBuilder()
    {
        return new ConnectRequestBuilder();
    }

    public String getParentName()
    {
        return parentName;
    }

    public Xid getXid()
    {
        return XID.of(xid);
    }

    public ServiceBuffer getServiceBuffer()
    {
        return serviceBuffer;
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

    public static final class ConnectRequestBuilder
    {
        private UUID execution;
        private String serviceName;
        private long timeout;
        private SpanId parentSpan;
        private String parentName = "";
        private Xid xid;
        private Duplex duplex;
        private ServiceBuffer serviceBuffer;
        private ProtocolVersion protocolVersion;

        private ConnectRequestBuilder()
        {}

        public ConnectRequestBuilder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public ConnectRequestBuilder setServiceName(String serviceName)
        {
            this.serviceName = serviceName;
            return this;
        }

        public ConnectRequestBuilder setTimeout(long timeout)
        {
            this.timeout = timeout;
            return this;
        }

        public ConnectRequestBuilder setParentSpan(SpanId parentSpan)
        {
            this.parentSpan = parentSpan;
            return this;
        }

        public ConnectRequestBuilder setParentName(String parentName)
        {
            this.parentName = parentName;
            return this;
        }

        public ConnectRequestBuilder setXid(Xid xid)
        {
            this.xid = xid;
            return this;
        }

        public ConnectRequestBuilder setDuplex(Duplex duplex)
        {
            this.duplex = duplex;
            return this;
        }

        public ConnectRequestBuilder setServiceBuffer(ServiceBuffer serviceBuffer)
        {
            this.serviceBuffer = serviceBuffer;
            return this;
        }

        public ConnectRequestBuilder setProtocolVersion(ProtocolVersion protocolVersion)
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public ConnectRequest build()
        {
            if(null == serviceBuffer)
            {
                serviceBuffer = ServiceBuffer.nullBuffer();
            }
            return new ConnectRequest(execution, serviceName, timeout, parentSpan, parentName,xid, duplex, serviceBuffer, protocolVersion);
        }
    }

    private List<byte[]> toNetworkBytes(int messageSize, final byte[] serviceNameBytes, final byte[] parentNameBytes, final List<byte[]> serviceBytes)
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
        b.putShort(duplex.getValue());
        b.putLong(serviceBytes.get(0).length).put(serviceBytes.get(0));
        serviceBytes.remove(0);
        final long payloadSize = ByteUtils.sumNumberOfBytes(serviceBytes);
        b.putLong(payloadSize);
        serviceBytes.forEach(b::put);
        l.add(b.array());
        return l;
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
        ConnectRequest that = (ConnectRequest) o;
        return timeout == that.timeout && Objects.equals(execution, that.execution) &&
                Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(parentName, that.parentName) && Objects.equals(parentSpan, that.parentSpan) &&
                Objects.equals(xid, that.xid) && Objects.equals(duplex, that.duplex);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, serviceName, timeout, parentName, xid, duplex);
    }

    @Override
    public String toString()
    {
        return "ConnectRequest{" +
                "execution=" + execution +
                ", serviceName='" + serviceName + '\'' +
                ", timeout=" + timeout +
                ", parentName='" + parentName + '\'' +
                ", parentSpan='" + parentSpan + '\'' +
                ", xid=" + xid +
                ", duplex=" + duplex +
                ", serviceBuffer=" + serviceBuffer +
                '}';
    }
}
