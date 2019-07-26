/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.queue;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.api.util.time.InstantUtil;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.DequeueReplySizes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class DequeueMessage
{
    private final QueueMessage msg;
    private final ServiceBuffer payload;
    private DequeueMessage(final QueueMessage msg)
    {
        this.msg = msg;
        this.payload = ServiceBuffer.of(msg.getPayload());
    }

    public static DequeueMessage of(final QueueMessage msg)
    {
        Objects.requireNonNull(msg, "msg can not be null");
        return new DequeueMessage(msg);
    }

    public UUID getId()
    {
        return msg.getId();
    }

    public String getCorrelationInformation()
    {
        return msg.getCorrelationInformation();
    }

    public String getReplyQueue()
    {
        return msg.getReplyQueue();
    }

    public ServiceBuffer getPayload()
    {
        return payload;
    }

    public Instant getAvailableSince()
    {
        return msg.getAvailableSince();
    }

    public long getNumberOfRedelivered()
    {
        return msg.getRedelivered();
    }

    public Instant getTimestamp()
    {
        return msg.getTimestamp();
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
        DequeueMessage that = (DequeueMessage) o;
        return Objects.equals(msg, that.msg);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(msg);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("DequeueMessage{");
        sb.append("msg=").append(msg);
        sb.append('}');
        return sb.toString();
    }

    public List<byte[]> toNetworkBytes()
    {
        final byte[] propertiesBytes = getCorrelationInformation().getBytes(StandardCharsets.UTF_8);
        final byte[] replyDataBytes = getReplyQueue().getBytes(StandardCharsets.UTF_8);

        ByteBuffer partialBuffer = ByteBuffer.allocate(DequeueReplySizes.MESSAGE_ID.getNetworkSize() + DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize() +
                                                       DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize() + DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize() +
                                                       propertiesBytes.length + replyDataBytes.length);
        CasualEncoderUtils.writeUUID(getId(), partialBuffer);
        partialBuffer.putLong(propertiesBytes.length);
        partialBuffer.put(propertiesBytes);
        partialBuffer.putLong(replyDataBytes.length);
        partialBuffer.put(replyDataBytes);
        partialBuffer.putLong(InstantUtil.toNanos(getAvailableSince()));

        List<byte[]> l = new ArrayList<>();
        l.add(partialBuffer.array());
        l.addAll(CasualEncoderUtils.writeServiceBuffer(payload));

        ByteBuffer redeliveredAndTimestampBuffer = ByteBuffer.allocate(DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize() + DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize());
        redeliveredAndTimestampBuffer.putLong(getNumberOfRedelivered());
        redeliveredAndTimestampBuffer.putLong(InstantUtil.toNanos(getTimestamp()));
        l.add(redeliveredAndTimestampBuffer.array());

        return l;
    }
}
