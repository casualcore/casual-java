/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.xa.XID;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
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

/**
 * Created by aleph on 2017-03-14.
 */
public final class CasualServiceCallRequestMessage implements CasualNetworkTransmittable
{
    private UUID execution;
    private String serviceName;
    private long timeout;
    private String parentName;
    private Xid xid;
    private Flag<AtmiFlags> xatmiFlags;
    private ServiceBuffer serviceBuffer;

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
        return CasualNWMessageType.SERVICE_CALL_REQUEST;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] serviceNameBytes = serviceName.getBytes(StandardCharsets.UTF_8);
        final byte[] parentNameBytes = parentName.getBytes(StandardCharsets.UTF_8);
        final List<byte[]> serviceBytes = serviceBuffer.toNetworkBytes();
        final long messageSize = ServiceCallRequestSizes.EXECUTION.getNetworkSize() +
                                 ServiceCallRequestSizes.CALL_DESCRIPTOR.getNetworkSize() +
                                 ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize() + serviceNameBytes.length +
                                 ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize() +
                                 ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize() + parentNameBytes.length +
                                 XIDUtils.getXIDNetworkSize(xid) +
                                 ServiceCallRequestSizes.FLAGS.getNetworkSize() +
                                 ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);
        return (messageSize <= getMaxMessageSize()) ? toNetworkBytesFitsInOneBuffer((int)messageSize, serviceNameBytes, parentNameBytes, serviceBytes)
                                                    : toNetworkBytesMultipleBuffers(serviceNameBytes, parentNameBytes, serviceBuffer);
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
     * @return
     */
    public int getMaxMessageSize()
    {
        return maxMessageSize;
    }

    /**
     * Use in testing to force chunking of message
     * @param maxMessageSize
     * @return
     */
    public CasualServiceCallRequestMessage setMaxMessageSize(int maxMessageSize)
    {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    /**
     * Note, not immutable
     * @return
     */
    public ServiceBuffer getServiceBuffer()
    {
        return serviceBuffer;
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
        CasualServiceCallRequestMessage that = (CasualServiceCallRequestMessage) o;
        return Objects.equals(execution, that.execution) &&
            Objects.equals(serviceName, that.serviceName) &&
            Objects.equals(parentName, that.parentName) &&
            Objects.equals(xid, that.xid) &&
            Objects.equals(xatmiFlags, that.xatmiFlags);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, serviceName, parentName, xid, xatmiFlags);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualServiceCallRequestMessage{");
        sb.append("execution=").append(execution);
        sb.append(", serviceName='").append(serviceName).append('\'');
        sb.append(", timeout=").append(timeout);
        sb.append(", parentName='").append(parentName).append('\'');
        sb.append(", xid=").append(xid);
        sb.append(", xatmiFlags=").append(xatmiFlags.getFlagValue());
        sb.append(", serviceBuffer=").append(serviceBuffer);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder
    {
        private UUID execution;
        private String serviceName;
        private long timeout;
        // optional
        private String parentName = "";
        private Xid xid;
        private Flag<AtmiFlags> xatmiFlags;
        private ServiceBuffer serviceBuffer;

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
        public CasualServiceCallRequestMessage build()
        {
            CasualServiceCallRequestMessage r = new CasualServiceCallRequestMessage();
            r.execution = execution;
            r.serviceName = serviceName;
            r.timeout = timeout;
            r.parentName = parentName;
            r.xid = XID.of(xid);
            r.xatmiFlags = xatmiFlags;
            r.serviceBuffer = serviceBuffer;
            return r;
        }
    }



    private List<byte[]> toNetworkBytesFitsInOneBuffer(int messageSize, final byte[] serviceNameBytes, final byte[] parentNameBytes, final List<byte[]> serviceBytes)
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
        b.putLong(xatmiFlags.getFlagValue())
         .putLong(serviceBytes.get(0).length)
         .put(serviceBytes.get(0));
        serviceBytes.remove(0);
        final long payloadSize = ByteUtils.sumNumberOfBytes(serviceBytes);
        b.putLong(payloadSize);
        serviceBytes.forEach(b::put);
        l.add(b.array());
        return l;
    }

    private List<byte[]> toNetworkBytesMultipleBuffers(final byte[] serviceNameBytes, final byte[] parentNameBytes, final ServiceBuffer serviceBuffer)
    {
        final List<byte[]> l = new ArrayList<>();
        final ByteBuffer executionBuffer = ByteBuffer.allocate(ServiceCallRequestSizes.EXECUTION.getNetworkSize());
        CasualEncoderUtils.writeUUID(execution, executionBuffer);
        l.add(executionBuffer.array());
        l.add(CasualEncoderUtils.writeLong(serviceNameBytes.length));
        l.add(serviceNameBytes);
        l.add(CasualEncoderUtils.writeLong(timeout));
        l.add(CasualEncoderUtils.writeLong(parentNameBytes.length));
        l.add(parentNameBytes);
        final ByteBuffer xidByteBuffer = ByteBuffer.allocate(XIDUtils.getXIDNetworkSize(xid));
        CasualEncoderUtils.writeXID(xid, xidByteBuffer);
        l.add(xidByteBuffer.array());
        l.add(CasualEncoderUtils.writeLong(xatmiFlags.getFlagValue()));
        l.addAll(CasualEncoderUtils.writeServiceBuffer(serviceBuffer));
        return l;
    }

}
