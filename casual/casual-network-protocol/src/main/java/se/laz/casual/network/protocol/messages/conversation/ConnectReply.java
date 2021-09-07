/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.conversation;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.network.protocol.messages.Routable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationConnectReplySizes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.CONVERSATION_CONNECT_REPLY;

public class ConnectReply implements CasualNetworkTransmittable, Routable
{
    private final UUID execution;
    private final List<UUID> routes;
    private final List<UUID> recordingNodes;
    private int resultCode;

    private ConnectReply(UUID execution, List<UUID> routes, List<UUID> recordingNodes, int resultCode)
    {
        this.execution = execution;
        this.routes = routes;
        this.recordingNodes = recordingNodes;
        this.resultCode = resultCode;
    }

    public UUID getExecution()
    {
        return execution;
    }

    public List<UUID> getRecordingNodes()
    {
        return recordingNodes;
    }

    public int getResultCode()
    {
        return resultCode;
    }

    public static ConnectReplyBuilder createBuilder()
    {
        return new ConnectReplyBuilder();
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CONVERSATION_CONNECT_REPLY;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final int messageSize = ConversationConnectReplySizes.EXECUTION.getNetworkSize() +
                ConversationConnectReplySizes.ROUTES_SIZE.getNetworkSize() + routes.size() * ConversationConnectReplySizes.RECORDING_ELEMENT_SIZE.getNetworkSize() +
                ConversationConnectReplySizes.RECORDING_SIZE.getNetworkSize() + recordingNodes.size() * ConversationConnectReplySizes.RECORDING_ELEMENT_SIZE.getNetworkSize() +
                ConversationConnectReplySizes.RESULT_CODE.getNetworkSize();
        return toNetworkBytes(messageSize);
    }

    private List<byte[]> toNetworkBytes(int messageSize)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putLong(routes.size());
        routes.forEach(uuid -> CasualEncoderUtils.writeUUID(uuid, b));
        b.putLong(recordingNodes.size());
        recordingNodes.forEach(uuid -> CasualEncoderUtils.writeUUID(uuid, b));
        b.putInt(resultCode);
        l.add(b.array());
        return l;
    }

    @Override
    public List<UUID> getRoutes()
    {
        return Collections.unmodifiableList(routes);
    }

    @Override
    public void setRoutes(List<UUID> routes)
    {
        this.routes.clear();
        this.routes.addAll(routes);
    }

    public static final class ConnectReplyBuilder
    {
        private UUID execution;
        private List<UUID> routes;
        private List<UUID> recordingNodes;
        private int resultCode;

        private ConnectReplyBuilder()
        {}

        public ConnectReplyBuilder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public ConnectReplyBuilder setRoutes(List<UUID> routes)
        {
            this.routes = routes;
            return this;
        }

        public ConnectReplyBuilder setRecordingNodes(List<UUID> recordingNodes)
        {
            this.recordingNodes = recordingNodes;
            return this;
        }

        public ConnectReplyBuilder setResultCode(int resultCode)
        {
            this.resultCode = resultCode;
            return this;
        }

        public ConnectReply build()
        {
            return new ConnectReply(execution, routes, recordingNodes, resultCode);
        }
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
        ConnectReply that = (ConnectReply) o;
        return resultCode == that.resultCode && Objects.equals(execution, that.execution) && Objects.equals(routes, that.routes) && Objects.equals(recordingNodes, that.recordingNodes);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, routes, recordingNodes, resultCode);
    }

    @Override
    public String toString()
    {
        return "ConnectReply{" +
                "execution=" + execution +
                ", routes=" + routes +
                ", recordingNodes=" + recordingNodes +
                ", resultCode=" + resultCode +
                '}';
    }
}
