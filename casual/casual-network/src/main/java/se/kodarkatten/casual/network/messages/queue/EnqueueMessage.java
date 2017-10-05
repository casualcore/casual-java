package se.kodarkatten.casual.network.messages.queue;

import se.kodarkatten.casual.network.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.messages.parseinfo.EnqueueRequestSizes;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

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
    private final UUID id;
    private final String properties;
    private final String replyData;
    private final ServiceBuffer payload;
    private final LocalDateTime availableForDequeueSince;
    private EnqueueMessage(final UUID id, final String properties, final String replyData, final ServiceBuffer payload, final LocalDateTime availableForDequeueSince)
    {
        this.id = id;
        this.properties = properties;
        this.replyData = replyData;
        this.payload = payload;
        this.availableForDequeueSince = availableForDequeueSince;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public ServiceBuffer getPayload()
    {
        return payload;
    }

    public String getProperties()
    {
        return properties;
    }

    public UUID getId()
    {
        return id;
    }

    public LocalDateTime getAvailableForDequeueSince()
    {
        return availableForDequeueSince;
    }

    public String getReplyData()
    {
        return replyData;
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
        return Objects.equals(id, that.id) &&
            Objects.equals(properties, that.properties) &&
            Objects.equals(replyData, that.replyData) &&
            Objects.equals(payload, that.payload) &&
            Objects.equals(availableForDequeueSince, that.availableForDequeueSince);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, properties, replyData, payload, availableForDequeueSince);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("EnqueueMessage{");
        sb.append("id=").append(id);
        sb.append(", properties='").append(properties).append('\'');
        sb.append(", replyData='").append(replyData).append('\'');
        sb.append(", payload=").append(payload);
        sb.append(", availableForDequeueSince=").append(availableForDequeueSince);
        sb.append('}');
        return sb.toString();
    }

    public List<byte[]> toNetworkBytes()
    {
        final byte[] propertiesBytes = properties.getBytes(StandardCharsets.UTF_8);
        final byte[] replyDataBytes = replyData.getBytes(StandardCharsets.UTF_8);

        ByteBuffer partialContent = ByteBuffer.allocate(EnqueueRequestSizes.MESSAGE_ID.getNetworkSize() + EnqueueRequestSizes.MESSAGE_PROPERTIES_SIZE.getNetworkSize() +
                                                  EnqueueRequestSizes.MESSAGE_REPLY_SIZE.getNetworkSize() + propertiesBytes.length + replyDataBytes.length +
                                                  EnqueueRequestSizes.MESSAGE_AVAILABLE.getNetworkSize());
        CasualNetworkWriterUtils.writeUUID(id, partialContent);
        partialContent.putLong(propertiesBytes.length);
        partialContent.put(propertiesBytes);
        partialContent.putLong(replyDataBytes.length);
        partialContent.put(replyDataBytes);
        partialContent.putLong(availableForDequeueSince.toEpochSecond(OffsetDateTime.now().getOffset()));

        List<byte[]> l = new ArrayList<>();
        l.add(partialContent.array());
        l.addAll(CasualNetworkWriterUtils.writeServiceBuffer(payload));
        return l;
    }

    public static final class Builder
    {
        private UUID id;
        private String properties;
        private String replyData;
        private ServiceBuffer payload;
        private long availableForDequeueSinceEpoc;

        private Builder()
        {}

        public Builder withProperties(final String properties)
        {
            this.properties = properties;
            return this;
        }

        public Builder withAvailableForDequeueSince(long availableForDequeueSince)
        {
            this.availableForDequeueSinceEpoc = availableForDequeueSince;
            return this;
        }

        public Builder withPayload(final ServiceBuffer payload)
        {
            this.payload = payload;
            return this;
        }

        public Builder withReplyData(final String replyData)
        {
            this.replyData = replyData;
            return this;
        }

        public Builder withId(final UUID id)
        {
            this.id = id;
            return this;
        }

        public EnqueueMessage build()
        {
            Objects.requireNonNull(id, "id is not allowed to be null");
            Objects.requireNonNull(properties, "properties is not allowed to be null");
            Objects.requireNonNull(replyData, "replyData is not allowed to be null");
            Objects.requireNonNull(payload, "payload is not allowed to be null");
            LocalDateTime availableForDequeueSince = LocalDateTime.ofEpochSecond(availableForDequeueSinceEpoc, 0 , OffsetDateTime.now().getOffset());
            return new EnqueueMessage(id, properties, replyData, payload, availableForDequeueSince);
        }
    }
}
