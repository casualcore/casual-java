/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.queue;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.util.time.InstantUtil;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class QueueMessage implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final String correlationInformation;
    private final String replyQueue;
    private final CasualBuffer payload;
    private final long redelivered;
    private final Instant availableSince;
    private final Instant timestamp;
    private QueueMessage(UUID id, String correlationInformation, String replyQueue, CasualBuffer payload, long redelivered, Instant availableSince, Instant timestamp)
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

    public static QueueMessage of(final CasualBuffer payload, final String correlationInformation, final QueueInfo replyQueue, final Instant availableSince)
    {
        return createBuilder().withPayload(payload)
                              .withCorrelationInformation(correlationInformation)
                              .withReplyQueue(replyQueue.getQueueName())
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

    public Instant getAvailableSince()
    {
        return availableSince;
    }

    public Instant getTimestamp()
    {
        return timestamp;
    }

    public static Builder createBuilder()
    {
        return new Builder();
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
        private Instant availableSince;
        private Instant timestamp;

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

        public Builder withAvailableSince(long nanoSecondsSinceEpoc)
        {
            this.availableSince = InstantUtil.fromNanos(nanoSecondsSinceEpoc);
            return this;
        }

        public Builder withAvailableSince(Instant availableSince)
        {
            this.availableSince = availableSince;
            return this;
        }

        public Builder withTimestamp(long nanoSecondsSinceEpoc)
        {
            this.timestamp = InstantUtil.fromNanos(nanoSecondsSinceEpoc);
            return this;
        }

        public Builder withTimestamp(Instant timestamp)
        {
            this.timestamp = timestamp;
            return this;
        }

        public QueueMessage build()
        {
            availableSince = null == availableSince ? LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset()) : availableSince;
            timestamp = null == timestamp ? LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset()) : timestamp;
            id = null == id ? new UUID(0,0) : id;
            Objects.requireNonNull(payload, "payload can not be null");
            return new QueueMessage(id, correlationInformation, replyQueue, payload, redelivered, availableSince, timestamp);
        }
    }
}
