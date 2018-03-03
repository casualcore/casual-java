/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.queue;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.EnqueueRequestSizes;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CasualEnqueueRequestMessage implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final String queueName;
    private final Xid xid;
    private final EnqueueMessage message;
    private CasualEnqueueRequestMessage(final UUID execution, final String domainName, final Xid xid, final EnqueueMessage message)
    {
        this.execution = execution;
        this.queueName = domainName;
        this.xid = xid;
        this.message = message;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.ENQUEUE_REQUEST;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final byte[] queueNameBytes = queueName.getBytes(StandardCharsets.UTF_8);
        final int partialSize = EnqueueRequestSizes.EXECUTION.getNetworkSize() + EnqueueRequestSizes.NAME_SIZE.getNetworkSize() +
                                queueNameBytes.length + XIDUtils.getXIDNetworkSize(xid);
        ByteBuffer partialContent = ByteBuffer.allocate(partialSize);
        CasualEncoderUtils.writeUUID(execution, partialContent);
        partialContent.putLong(queueNameBytes.length);
        partialContent.put(queueNameBytes);
        CasualEncoderUtils.writeXID(xid, partialContent);
        List<byte[]> l = new ArrayList<>();
        l.add(partialContent.array());
        l.addAll(message.toNetworkBytes());
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
        CasualEnqueueRequestMessage that = (CasualEnqueueRequestMessage) o;
        return Objects.equals(execution, that.execution) &&
            Objects.equals(queueName, that.queueName) &&
            Objects.equals(xid, that.xid) &&
            Objects.equals(message, that.message);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, queueName, xid, message);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualEnqueueRequestMessage{");
        sb.append("execution=").append(execution);
        sb.append(", queueName='").append(queueName).append('\'');
        sb.append(", xid=").append(xid);
        sb.append(", message=").append(message);
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

    public String getQueueName()
    {
        return queueName;
    }

    public Xid getXid()
    {
        return xid;
    }

    public EnqueueMessage getMessage()
    {
        return message;
    }


    public static final class Builder
    {
        private UUID execution;
        private String queueName;
        private Xid xid;
        private EnqueueMessage message;

        private Builder()
        {}

        public Builder withExecution(final UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder withQueueName(final String queueName)
        {
            this.queueName = queueName;
            return this;
        }

        public Builder withXid(final Xid xid)
        {
            this.xid = xid;
            return this;
        }

        public Builder withMessage(final EnqueueMessage message)
        {
            this.message = message;
            return this;
        }

        public CasualEnqueueRequestMessage build()
        {
            Objects.requireNonNull(execution, "execution is not allowed to be null");
            Objects.requireNonNull(queueName, "queueName is not allowed to be null");
            Objects.requireNonNull(xid, "xid is not allowed to be null");
            Objects.requireNonNull(message, "message is not allowed to be null");
            return new CasualEnqueueRequestMessage(execution, queueName, xid, message);
        }
    }
}
