/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.conversation;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
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
import java.util.Collections;
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
    private final Flag<AtmiFlags> xatmiFlags;
    private final ServiceBuffer serviceBuffer;
    private final List<UUID> recordingNodes;

    // private constructor, only used by the builder of this class
    @SuppressWarnings("squid:S00107")
    private ConnectRequest(UUID execution, String serviceName, long timeout, String parentName, Xid xid, Flag<AtmiFlags> xatmiFlags, ServiceBuffer serviceBuffer, List<UUID> recordingNodes)
    {
        this.execution = execution;
        this.serviceName = serviceName;
        this.timeout = timeout;
        this.parentName = parentName;
        this.xid = xid;
        this.xatmiFlags = xatmiFlags;
        this.serviceBuffer = serviceBuffer;
        this.recordingNodes = recordingNodes;
    }

    public List<UUID> getRecordingNodes()
    {
        return Collections.unmodifiableList(recordingNodes);
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
                ConversationConnectRequestSizes.FLAGS.getNetworkSize() +
                ConversationConnectRequestSizes.RECORDING_SIZE.getNetworkSize() + recordingNodes.size() * ConversationConnectRequestSizes.RECORDING_ELEMENT_SIZE.getNetworkSize() +
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

    public Flag<AtmiFlags> getXatmiFlags()
    {
        return Flag.of(xatmiFlags);
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
        private String parentName;
        private Xid xid;
        private Flag<AtmiFlags> xatmiFlags;
        private ServiceBuffer serviceBuffer;
        private List<UUID> recordingNodes = new ArrayList<>();

        private ConnectRequestBuilder()
        {}

        public ConnectRequestBuilder setExecution(UUID execution)
        {         this.execution = execution;
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

        public ConnectRequestBuilder setXatmiFlags(Flag<AtmiFlags> xatmiFlags)
        {
            this.xatmiFlags = xatmiFlags;
            return this;
        }

        public ConnectRequestBuilder setServiceBuffer(ServiceBuffer serviceBuffer)
        {
            this.serviceBuffer = serviceBuffer;
            return this;
        }

        public ConnectRequestBuilder setRecordingNodes(List<UUID> recordingNodes)
        {
            this.recordingNodes = recordingNodes;
            return this;
        }

        public ConnectRequest build()
        {
            return new ConnectRequest(execution,serviceName,timeout,parentName,xid,xatmiFlags,serviceBuffer,recordingNodes);
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
        b.putLong(xatmiFlags.getFlagValue());
        b.putLong(recordingNodes.size());
        recordingNodes.forEach(uuid -> CasualEncoderUtils.writeUUID(uuid, b));
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
        return timeout == that.timeout && Objects.equals(execution, that.execution) && Objects.equals(serviceName, that.serviceName) && Objects.equals(parentName, that.parentName) && Objects.equals(xid, that.xid) && Objects.equals(xatmiFlags, that.xatmiFlags) && Objects.equals(recordingNodes, that.recordingNodes);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, serviceName, timeout, parentName, xid, xatmiFlags, recordingNodes);
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
                ", xatmiFlags=" + xatmiFlags +
                ", serviceBuffer=" + serviceBuffer +
                ", routes=" + recordingNodes +
                '}';
    }
}
