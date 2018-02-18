package se.kodarkatten.casual.network.protocol.messages.queue;

import se.kodarkatten.casual.api.queue.QueueMessage;
import se.kodarkatten.casual.network.protocol.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.DequeueReplySizes;
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.nio.ByteBuffer;

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

    public LocalDateTime getAvailableSince()
    {
        return msg.getAvailableSince();
    }

    public long getNumberOfRedelivered()
    {
        return msg.getRedelivered();
    }

    public LocalDateTime getTimestamp()
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
        CasualNetworkWriterUtils.writeUUID(getId(), partialBuffer);
        partialBuffer.putLong(propertiesBytes.length);
        partialBuffer.put(propertiesBytes);
        partialBuffer.putLong(replyDataBytes.length);
        partialBuffer.put(replyDataBytes);
        partialBuffer.putLong(getAvailableSince().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli());

        List<byte[]> l = new ArrayList<>();
        l.add(partialBuffer.array());
        l.addAll(CasualNetworkWriterUtils.writeServiceBuffer(payload));

        ByteBuffer redeliveredAndTimestampBuffer = ByteBuffer.allocate(DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize() + DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize());
        redeliveredAndTimestampBuffer.putLong(getNumberOfRedelivered());
        redeliveredAndTimestampBuffer.putLong(getTimestamp().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli());
        l.add(redeliveredAndTimestampBuffer.array());

        return l;
    }
}
