package se.kodarkatten.casual.network.messages.queue;

import se.kodarkatten.casual.network.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.messages.parseinfo.DequeueReplySizes;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

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
    private final UUID id;
    private final String properties;
    private final String replyData;
    private final ServiceBuffer payload;
    private final LocalDateTime availableForDequeueSince;
    private final long numberOfRedelivered;
    private final LocalDateTime timestamp;
    private DequeueMessage(final UUID id, final String properties, final String replyData, final ServiceBuffer payload, final LocalDateTime availableForDequeueSince, long numberOfRedelivered, LocalDateTime timestamp)
    {
        this.id = id;
        this.properties = properties;
        this.replyData = replyData;
        this.payload = payload;
        this.availableForDequeueSince = availableForDequeueSince;
        this.numberOfRedelivered = numberOfRedelivered;
        this.timestamp = timestamp;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public UUID getId()
    {
        return id;
    }

    public String getProperties()
    {
        return properties;
    }

    public String getReplyData()
    {
        return replyData;
    }

    public ServiceBuffer getPayload()
    {
        return payload;
    }

    public LocalDateTime getAvailableForDequeueSince()
    {
        return availableForDequeueSince;
    }

    public long getNumberOfRedelivered()
    {
        return numberOfRedelivered;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
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
        DequeueMessage that = (DequeueMessage) o;
        return numberOfRedelivered == that.numberOfRedelivered &&
            Objects.equals(id, that.id) &&
            Objects.equals(properties, that.properties) &&
            Objects.equals(replyData, that.replyData) &&
            Objects.equals(payload, that.payload) &&
            Objects.equals(availableForDequeueSince, that.availableForDequeueSince) &&
            Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, properties, replyData, payload, availableForDequeueSince, numberOfRedelivered, timestamp);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("DequeueMessage{");
        sb.append("id=").append(id);
        sb.append(", properties='").append(properties).append('\'');
        sb.append(", replyData='").append(replyData).append('\'');
        sb.append(", payload=").append(payload);
        sb.append(", availableForDequeueSince=").append(availableForDequeueSince);
        sb.append(", numberOfRedelivered=").append(numberOfRedelivered);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }

    public List<byte[]> toNetworkBytes()
    {
        final byte[] propertiesBytes = properties.getBytes(StandardCharsets.UTF_8);
        final byte[] replyDataBytes = replyData.getBytes(StandardCharsets.UTF_8);

        ByteBuffer partialBuffer = ByteBuffer.allocate(DequeueReplySizes.MESSAGE_ID.getNetworkSize() + DequeueReplySizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize() +
                                                       DequeueReplySizes.MESSAGE_REPLY_SIZE.getNetworkSize() + DequeueReplySizes.MESSAGE_AVAILABLE_SINCE_EPOC.getNetworkSize() +
                                                       propertiesBytes.length + replyDataBytes.length);
        CasualNetworkWriterUtils.writeUUID(id, partialBuffer);
        partialBuffer.putLong(propertiesBytes.length);
        partialBuffer.put(propertiesBytes);
        partialBuffer.putLong(replyDataBytes.length);
        partialBuffer.put(replyDataBytes);
        partialBuffer.putLong(availableForDequeueSince.toEpochSecond(OffsetDateTime.now().getOffset()));

        List<byte[]> l = new ArrayList<>();
        l.add(partialBuffer.array());
        l.addAll(CasualNetworkWriterUtils.writeServiceBuffer(payload));

        ByteBuffer redeliveredAndTimestampBuffer = ByteBuffer.allocate(DequeueReplySizes.MESSAGE_REDELIVERED_COUNT.getNetworkSize() + DequeueReplySizes.MESSAGE_TIMESTAMP_SINCE_EPOC.getNetworkSize());
        redeliveredAndTimestampBuffer.putLong(numberOfRedelivered);
        redeliveredAndTimestampBuffer.putLong(timestamp.toEpochSecond(OffsetDateTime.now().getOffset()));
        l.add(redeliveredAndTimestampBuffer.array());

        return l;
    }

    public static final class Builder
    {
        private UUID id;
        private String properties;
        private String replyData;
        private ServiceBuffer payload;
        private long availableForDequeueSinceEpoc;
        private long numberOfRedelivered;
        private long timestampSinceEpoc;

        private Builder()
        {}

        public Builder withId(final UUID id)
        {
            this.id = id;
            return this;
        }

        public Builder withProperties(final String properties)
        {
            this.properties = properties;
            return this;
        }

        public Builder withTimestamp(long timestamp)
        {
            this.timestampSinceEpoc = timestamp;
            return this;
        }

        public Builder withReplyData(final String replyData)
        {
            this.replyData = replyData;
            return this;
        }

        public Builder withPayload(final ServiceBuffer payload)
        {
            this.payload = payload;
            return this;
        }

        public Builder withAvailableForDequeueSince(long availableForDequeueSince)
        {
            this.availableForDequeueSinceEpoc = availableForDequeueSince;
            return this;
        }

        public Builder withNumberOfRedeliveries(long numberOfRedeliveries)
        {
            this.numberOfRedelivered = numberOfRedeliveries;
            return this;
        }

        public DequeueMessage build()
        {
            Objects.requireNonNull(id, "id is not allowed to be null");
            Objects.requireNonNull(properties, "properties is not allowed to be null");
            Objects.requireNonNull(replyData, "replyData is not allowed to be null");
            Objects.requireNonNull(payload, "payload is not allowed to be null");
            LocalDateTime availableForDequeueSince = LocalDateTime.ofEpochSecond(availableForDequeueSinceEpoc, 0 , OffsetDateTime.now().getOffset());
            LocalDateTime timestamp = LocalDateTime.ofEpochSecond(timestampSinceEpoc, 0 , OffsetDateTime.now().getOffset());
            return new DequeueMessage(id, properties, replyData, payload, availableForDequeueSince, numberOfRedelivered, timestamp);
        }
    }
}
