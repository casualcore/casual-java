/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
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
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CasualEnqueueReplyMessage implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final UUID id;
    private final ProtocolVersion protocolVersion;
    // from 1.3
    private final QueueErrorCode code;

    private CasualEnqueueReplyMessage(final UUID execution, final UUID id, ProtocolVersion protocolVersion, QueueErrorCode code)
    {
        this.execution = execution;
        this.id = id;
        this.code = code;
        this.protocolVersion = protocolVersion;
    }
    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.ENQUEUE_REPLY;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        int size = CommonSizes.EXECUTION.getNetworkSize() +  CommonSizes.UUID_ID.getNetworkSize();
        size += ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion) ?  CommonSizes.CALL_ERROR.getNetworkSize() : 0;
        ByteBuffer b = ByteBuffer.allocate(size);
        CasualEncoderUtils.writeUUID(execution, b);
        CasualEncoderUtils.writeUUID(id, b);
        if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion))
        {
            CasualEncoderUtils.writeInt(code.getValue());
        }
        List<byte[]> l = new ArrayList<>();
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
        if (!(o instanceof CasualEnqueueReplyMessage that))
        {
            return false;
        }
        return code == that.code && Objects.equals(getExecution(), that.getExecution()) && Objects.equals(getId(), that.getId()) && protocolVersion == that.protocolVersion;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getExecution(), getId(), protocolVersion, code);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualEnqueueReplyMessage{");
        sb.append("execution=").append(execution);
        sb.append(", id=").append(id);
        sb.append(", protocolVersion=").append(protocolVersion);
        if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion))
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

    public UUID getId()
    {
        return id;
    }

    public QueueErrorCode getCode()
    {
        if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion))
        {
            return code;
        }
        throw new CasualProtocolException("code is not available in protocol version " + protocolVersion);
    }

    public static final class Builder
    {
        private UUID execution;
        private UUID id;
        private QueueErrorCode code;
        private ProtocolVersion protocolVersion;

        public Builder withExecution(final UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder withId(final UUID id)
        {
            this.id = id;
            return this;
        }

        public Builder withCode(QueueErrorCode code)
        {
            this.code = code;
            return this;
        }

        public Builder withProtocolVersion(ProtocolVersion protocolVersion)
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public CasualEnqueueReplyMessage build()
        {
            Objects.requireNonNull(execution, "execution is not allowed to be null");
            Objects.requireNonNull(id, "id is not allowed to be null");
            Objects.requireNonNull(protocolVersion, "protocolVersion is not allowed to be null");
            if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion))
            {
                Objects.requireNonNull(code, "code can not be null");
            }
            return new CasualEnqueueReplyMessage(execution, id, protocolVersion, code);
        }
    }
}
