/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.queue;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.DequeueReplySizes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CasualDequeueReplyMessage implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final List<DequeueMessage> messages;
    private final ProtocolVersion protocolVersion;
    private final ErrorState code;

    private CasualDequeueReplyMessage(final UUID execution, final List<DequeueMessage> messages, ProtocolVersion protocolVersion, ErrorState code)
    {
        this.execution = execution;
        this.messages = messages;
        this.protocolVersion = protocolVersion;
        this.code = code;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.DEQUEUE_REPLY;
    }

    @Override
    public List<byte[]> toNetworkBytes()
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
        return Objects.equals(getExecution(), that.getExecution()) && Objects.equals(getMessages(), that.getMessages()) && protocolVersion == that.protocolVersion && code == that.code;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getExecution(), getMessages(), protocolVersion, code);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualDequeueReplyMessage{");
        sb.append("execution=").append(execution);
        sb.append(", messages=").append(messages);
        if(ProtocolVersion.isProtocolVersionOneGreaterOrEqualToOneThree(protocolVersion))
        {
            sb.append(", code=").append(code);
        }
        sb.append('}');
        return sb.toString();
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

    public ErrorState getCode()
    {
        if(ProtocolVersion.isProtocolVersionOneGreaterOrEqualToOneThree(protocolVersion))
        {
            return code;
        }
        throw new CasualRuntimeException("code is not available in protocol version: " + protocolVersion);
    }

    public static final class Builder
    {
        private UUID execution;
        private List<DequeueMessage> messages;
        private ProtocolVersion protocolVersion;
        private ErrorState code;

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

        public Builder withCode(final ErrorState code)
        {
            this.code = code;
            return this;
        }

        public CasualDequeueReplyMessage build()
        {
            Objects.requireNonNull(execution, "execution is not allowed to be null");
            Objects.requireNonNull(messages, "messages is not allowed to be null, can be empty though");
            Objects.requireNonNull(protocolVersion, "protocolVersion is not allowed to be null");
            return new CasualDequeueReplyMessage(execution, new ArrayList<>(messages), protocolVersion, code);
        }
    }
}
