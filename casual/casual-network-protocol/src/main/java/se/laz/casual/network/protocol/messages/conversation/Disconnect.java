/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.conversation;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationDisconnectSizes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Disconnect implements CasualNetworkTransmittable
{
    private final UUID execution;

    public Disconnect(UUID execution)
    {
        this.execution = execution;
    }
    public UUID getExecution()
    {
        return execution;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.CONVERSATION_DISCONNECT;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final int messageSize = ConversationDisconnectSizes.EXECUTION.getNetworkSize() +
                ConversationDisconnectSizes.EVENTS.getNetworkSize();
        return toNetworkBytes(messageSize);
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
        Disconnect request = (Disconnect) o;
        return Objects.equals(execution, request.execution);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution);
    }

    @Override
    public String toString()
    {
        return "Request{" +
                "execution=" + execution +
                '}';
    }

    public static DisconnectBuilder createBuilder()
    {
        return new DisconnectBuilder();
    }

    public static final class DisconnectBuilder
    {
        private UUID execution;

        private DisconnectBuilder()
        {}

        public DisconnectBuilder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Disconnect build()
        {
            return new Disconnect(execution);
        }
    }

    private List<byte[]> toNetworkBytes(int messageSize)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        l.add(b.array());
        return l;
    }

}
