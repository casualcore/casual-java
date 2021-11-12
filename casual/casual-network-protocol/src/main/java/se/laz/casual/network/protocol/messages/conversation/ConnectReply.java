/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.conversation;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationConnectReplySizes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static se.laz.casual.api.network.protocol.messages.CasualNWMessageType.CONVERSATION_CONNECT_REPLY;

public class ConnectReply implements CasualNetworkTransmittable
{
    private final UUID execution;
    private int resultCode;

    private ConnectReply(UUID execution, int resultCode)
    {
        this.execution = execution;
        this.resultCode = resultCode;
    }

    public UUID getExecution()
    {
        return execution;
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
                ConversationConnectReplySizes.RESULT_CODE.getNetworkSize();
        return toNetworkBytes(messageSize);
    }

    private List<byte[]> toNetworkBytes(int messageSize)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putInt(resultCode);
        l.add(b.array());
        return l;
    }

    public static final class ConnectReplyBuilder
    {
        private UUID execution;
        private int resultCode;

        private ConnectReplyBuilder()
        {}

        public ConnectReplyBuilder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public ConnectReplyBuilder setResultCode(int resultCode)
        {
            this.resultCode = resultCode;
            return this;
        }

        public ConnectReply build()
        {
            return new ConnectReply(execution, resultCode);
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
        return resultCode == that.resultCode && Objects.equals(execution, that.execution);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, resultCode);
    }

    @Override
    public String toString()
    {
        return "ConnectReply{" +
                "execution=" + execution +
                ", resultCode=" + resultCode +
                '}';
    }
}
