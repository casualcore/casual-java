/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.conversation;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.conversation.Duplex;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.xa.XID;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
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

public class ConnectRequest implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final String serviceName;
    private long timeout;
    private final String parentName;
    private final Xid xid;
    private final Duplex duplex;
    private final ServiceBuffer serviceBuffer;

    // private constructor, only used by the builder of this class
    @SuppressWarnings("squid:S00107")
    private ConnectRequest(UUID execution, String serviceName, long timeout, String parentName, Xid xid, Duplex duplex, ServiceBuffer serviceBuffer)
    {
        this.execution = execution;
        this.serviceName = serviceName;
        this.timeout = timeout;
        this.parentName = parentName;
        this.xid = xid;
        this.duplex = duplex;
        this.serviceBuffer = serviceBuffer;
    }
    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.CONVERSATION_CONNECT;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] serviceNameBytes = serviceName.getBytes(StandardCharsets.UTF_8);
        final byte[] parentNameBytes = parentName.getBytes(StandardCharsets.UTF_8);
        final List<byte[]> serviceBytes = serviceBuffer.toNetworkBytes();
        final long messageSize = ConversationConnectRequestSizes.EXECUTION.getNetworkSize() +
                ConversationConnectRequestSizes.CALL_DESCRIPTOR.getNetworkSize() +
                ConversationConnectRequestSizes.SERVICE_NAME_SIZE.getNetworkSize() + serviceNameBytes.length +
                ConversationConnectRequestSizes.SERVICE_TIMEOUT.getNetworkSize() +
                ConversationConnectRequestSizes.PARENT_NAME_SIZE.getNetworkSize() + parentNameBytes.length +
                XIDUtils.getXIDNetworkSize(xid) +
                ConversationConnectRequestSizes.DUPLEX.getNetworkSize() +
                ConversationConnectRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + ConversationConnectRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);
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
        private String parentName = "";
        private Xid xid;
        private Duplex duplex;
        private ServiceBuffer serviceBuffer;

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

        public ConnectRequest build()
        {
            if(null == serviceBuffer)
            {
                serviceBuffer = ServiceBuffer.nullBuffer();
            }
            return new ConnectRequest(execution, serviceName, timeout, parentName,xid, duplex, serviceBuffer);
        }
    }

    private List<byte[]> toNetworkBytes(int messageSize, final byte[] serviceNameBytes, final byte[] parentNameBytes, final List<byte[]> serviceBytes)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putLong(serviceNameBytes.length)
                .put(serviceNameBytes)
                .putLong(timeout)
                .putLong(parentNameBytes.length)
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
        return timeout == that.timeout && Objects.equals(execution, that.execution) && Objects.equals(serviceName, that.serviceName) && Objects.equals(parentName, that.parentName) && Objects.equals(xid, that.xid) && Objects.equals(duplex, that.duplex);
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
                ", xid=" + xid +
                ", duplex=" + duplex +
                ", serviceBuffer=" + serviceBuffer +
                '}';
    }
}
