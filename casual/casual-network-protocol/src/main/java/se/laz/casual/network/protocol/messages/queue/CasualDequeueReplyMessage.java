/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.queue;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.api.queue.QueueErrorCode;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.DequeueReplySizes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;

public class CasualDequeueReplyMessage implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final List<DequeueMessage> messages;
    private final ProtocolVersion protocolVersion;
    private final QueueErrorCode code;

    private CasualDequeueReplyMessage(final UUID execution, final List<DequeueMessage> messages, ProtocolVersion protocolVersion, QueueErrorCode code)
    {
        this.execution = execution;
        this.messages = messages;
        this.protocolVersion = protocolVersion;
        this.code = code;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 )
                ? CasualNWMessageType.DEQUEUE_REPLY_V_1_3
                : CasualNWMessageType.DEQUEUE_REPLY;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        return protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 )
                ? toNetworkBytesProtocolVersionGreaterOrEqualToOneThree()
                : toNetworkBytesProtocolVersionLessThanOneThree();
    }

    private List<byte[]> toNetworkBytesProtocolVersionLessThanOneThree()
    {
        ByteBuffer partialContent = ByteBuffer.allocate(DequeueReplySizes.EXECUTION.getNetworkSize() + DequeueReplySizes.NUMBER_OF_MESSAGES.getNetworkSize());
        CasualEncoderUtils.writeUUID(execution, partialContent);
        partialContent.putLong(messages.size());
        List<byte[]> l = new ArrayList<>();
        l.add(partialContent.array());
        for(DequeueMessage m : messages)
        {
            l.addAll(m.toNetworkBytes());
        }
        return l;
    }

    private List<byte[]> toNetworkBytesProtocolVersionGreaterOrEqualToOneThree()
    {
        // note: can only carry one message as opposed to protocol version < 1.3 where
        // it can contain n number of messages
        ByteBuffer partialContent = ByteBuffer.allocate(DequeueReplySizes.EXECUTION.getNetworkSize());
        CasualEncoderUtils.writeUUID(execution, partialContent);
        List<byte[]> l = new ArrayList<>();
        l.add(partialContent.array());
        byte[] hasValue = new byte[1];
        hasValue[0] = (byte)(messages.isEmpty() ? 0 : 1);
        l.add(hasValue);
        for(DequeueMessage m : messages)
        {
            l.addAll(m.toNetworkBytes());
        }
        l.add(ByteBuffer.allocate(DequeueReplySizes.CODE.getNetworkSize()).putInt(code.getValue()).array());
        return l;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CasualDequeueReplyMessage that))
        {
            return false;
        }
        return Objects.equals(getExecution(), that.getExecution()) && Objects.equals(getMessages(), that.getMessages())
                && protocolVersion == that.protocolVersion && code == that.code;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getExecution(), getMessages(), protocolVersion, code);
    }

    @Override
    public String toString()
    {
        return "CasualDequeueReplyMessage{" +
                "execution=" + execution +
                ", messages=" + messages +
                ", protocolVersion=" + protocolVersion +
                ", code=" + code +
                '}';
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public UUID getExecution()
    {
        return execution;
    }

    public List<DequeueMessage> getMessages()
    {
        return messages.stream().toList();
    }

    public QueueErrorCode getCode()
    {
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
        {
            return code;
        }
        throw new CasualProtocolException("code is not available in protocol version: " + protocolVersion);
    }

    public static final class Builder
    {
        private UUID execution;
        private List<DequeueMessage> messages;
        private ProtocolVersion protocolVersion;
        private QueueErrorCode code;

        public Builder withExecution(final UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder withMessages(final List<DequeueMessage> messages)
        {
            this.messages = messages;
            return this;
        }

        public Builder withProtocolVersion(final ProtocolVersion protocolVersion)
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder withCode(final QueueErrorCode code)
        {
            this.code = code;
            return this;
        }

        public CasualDequeueReplyMessage build()
        {
            Objects.requireNonNull(execution, "execution is not allowed to be null");
            Objects.requireNonNull(messages, "messages is not allowed to be null, can be empty though");
            Objects.requireNonNull(protocolVersion, "protocolVersion is not allowed to be null");
            if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 ) )
            {
                Objects.requireNonNull(code, "code can not be null");
                if(messages.size() > 1)
                {
                    throw new CasualProtocolException("for protocol version >= 1.3, only one message can be carried");
                }
            }
            return new CasualDequeueReplyMessage(execution, new ArrayList<>(messages), protocolVersion, code);
        }
    }
}
