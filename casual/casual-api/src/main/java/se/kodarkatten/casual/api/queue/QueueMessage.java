package se.kodarkatten.casual.api.queue;

import se.kodarkatten.casual.api.buffer.CasualBuffer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

public final class QueueMessage
{
    private final UUID id;
    private final String correlationInformation;
    private final String replyQueue;
    private final CasualBuffer payload;
    private final long redelivered;
    private final LocalDateTime availableSince;
    private final LocalDateTime timestamp;
    private QueueMessage(UUID id, String correlationInformation, String replyQueue, CasualBuffer payload, long redelivered, LocalDateTime availableSince, LocalDateTime timestamp)
    {
        this.id = id;
        this.correlationInformation = correlationInformation;
        this.replyQueue = replyQueue;
        this.payload = payload;
        this.redelivered = redelivered;
        this.availableSince = availableSince;
        this.timestamp = timestamp;
    }

    public static QueueMessage of(final CasualBuffer payload)
    {
        return createBuilder().withPayload(payload)
                              .build();
    }

    public static QueueMessage of(final CasualBuffer payload, final String correlationInformation, final QueueInfo replyQueue, final LocalDateTime availableSince)
    {
        return createBuilder().withPayload(payload)
                              .withCorrelationInformation(correlationInformation)
                              .withReplyQueue(replyQueue.getCompositeName())
                              .withAvailableSince(availableSince)
                              .build();
    }

    public UUID getId()
    {
        return id;
    }

    public String getCorrelationInformation()
    {
        return correlationInformation;
    }

    public String getReplyQueue()
    {
        return replyQueue;
    }

    public CasualBuffer getPayload()
    {
        return payload;
    }

    public long getRedelivered()
    {
        return redelivered;
    }

    public LocalDateTime getAvailableSince()
    {
        return availableSince;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    // expressions should not be too complex
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
        QueueMessage that = (QueueMessage) o;
        return redelivered == that.redelivered &&
            Objects.equals(id, that.id) &&
            Objects.equals(correlationInformation, that.correlationInformation) &&
            Objects.equals(replyQueue, that.replyQueue) &&
            Objects.equals(availableSince, that.availableSince) &&
            Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, correlationInformation, replyQueue, redelivered, availableSince, timestamp);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("QueueMessage{");
        sb.append("id=").append(id);
        sb.append(", correlationInformation='").append(correlationInformation).append('\'');
        sb.append(", replyQueue='").append(replyQueue).append('\'');
        sb.append(", payload=").append(payload);
        sb.append(", redelivered=").append(redelivered);
        sb.append(", availableSince=").append(availableSince);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder
    {
        private UUID id;
        private String correlationInformation = "";
        private String replyQueue = "";
        private CasualBuffer payload;
        private long redelivered;
        private LocalDateTime availableSince;
        private LocalDateTime timestamp;

        public Builder withId(UUID id)
        {
            this.id = id;
            return this;
        }

        public Builder withCorrelationInformation(String correlationInformation)
        {
            this.correlationInformation = correlationInformation;
            return this;
        }

        public Builder withReplyQueue(String replyQueue)
        {
            this.replyQueue = replyQueue;
            return this;
        }

        public Builder withPayload(CasualBuffer payload)
        {
            this.payload = payload;
            return this;
        }

        public Builder withRedelivered(long redelivered)
        {
            this.redelivered = redelivered;
            return this;
        }

        public Builder withAvailableSince(long milliSecondsSinceEpoc)
        {
            this.availableSince = Instant.ofEpochMilli(milliSecondsSinceEpoc).atZone(ZoneId.systemDefault()).toLocalDateTime();
            return this;
        }

        public Builder withAvailableSince(LocalDateTime availableSince)
        {
            this.availableSince = availableSince;
            return this;
        }

        public Builder withTimestamp(long milliSecondsSinceEpoc)
        {
            this.timestamp = Instant.ofEpochMilli(milliSecondsSinceEpoc).atZone(ZoneId.systemDefault()).toLocalDateTime();
            return this;
        }

        public Builder withTimestamp(LocalDateTime timestamp)
        {
            this.timestamp = timestamp;
            return this;
        }

        public QueueMessage build()
        {
            availableSince = null == availableSince ? LocalDateTime.now(OffsetDateTime.now().getOffset()) : availableSince;
            timestamp = null == timestamp ? LocalDateTime.now(OffsetDateTime.now().getOffset()) : timestamp;
            id = null == id ? new UUID(0,0) : id;
            Objects.requireNonNull(payload, "payload can not be null");
            return new QueueMessage(id, correlationInformation, replyQueue, payload, redelivered, availableSince, timestamp);
        }
    }
}
