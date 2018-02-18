package se.kodarkatten.casual.network.protocol.messages.queue;

import se.kodarkatten.casual.api.queue.QueueMessage;
import se.kodarkatten.casual.network.protocol.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.EnqueueRequestSizes;
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.nio.ByteBuffer;

public final class EnqueueMessage
{
    private final QueueMessage msg;
    private final ServiceBuffer payload;
    private EnqueueMessage(final QueueMessage msg)
    {
        this.msg = msg;
        this.payload = ServiceBuffer.of(msg.getPayload());
    }

    public static EnqueueMessage of(final QueueMessage msg)
    {
        Objects.requireNonNull(msg, "msg can not be null");
        return new EnqueueMessage(msg);
    }

    public ServiceBuffer getPayload()
    {
        return payload;
    }

    public String getCorrelationInformation()
    {
        return msg.getCorrelationInformation();
    }

    public UUID getId()
    {
        return msg.getId();
    }

    public LocalDateTime getAvailableSince()
    {
        return msg.getAvailableSince();
    }

    public String getReplyQueue()
    {
        return msg.getReplyQueue();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(msg);
    }

    // Expressions should not be too complex
    @SuppressWarnings("squid:S1067")
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
        EnqueueMessage that = (EnqueueMessage) o;
        return Objects.equals(msg.getCorrelationInformation(), that.msg.getCorrelationInformation()) &&
            Objects.equals(msg.getId(), that.msg.getId()) &&
            Objects.equals(msg.getAvailableSince(), that.msg.getAvailableSince()) &&
            Objects.equals(msg.getReplyQueue(), that.msg.getReplyQueue());
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("EnqueueMessage{");
        sb.append("msg=").append(msg);
        sb.append('}');
        return sb.toString();
    }

    public List<byte[]> toNetworkBytes()
    {
        final byte[] propertiesBytes = getCorrelationInformation().getBytes(StandardCharsets.UTF_8);
        final byte[] replyDataBytes = getReplyQueue().getBytes(StandardCharsets.UTF_8);

        ByteBuffer partialContent = ByteBuffer.allocate(EnqueueRequestSizes.MESSAGE_ID.getNetworkSize() + EnqueueRequestSizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize() +
                                                  EnqueueRequestSizes.MESSAGE_REPLY_SIZE.getNetworkSize() + propertiesBytes.length + replyDataBytes.length +
                                                  EnqueueRequestSizes.MESSAGE_AVAILABLE.getNetworkSize());
        CasualNetworkWriterUtils.writeUUID(getId(), partialContent);
        partialContent.putLong(propertiesBytes.length);
        partialContent.put(propertiesBytes);
        partialContent.putLong(replyDataBytes.length);
        partialContent.put(replyDataBytes);
        final long v = getAvailableSince().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
        partialContent.putLong(v);

        List<byte[]> l = new ArrayList<>();
        l.add(partialContent.array());
        l.addAll(CasualNetworkWriterUtils.writeServiceBuffer(payload));
        return l;
    }

}
