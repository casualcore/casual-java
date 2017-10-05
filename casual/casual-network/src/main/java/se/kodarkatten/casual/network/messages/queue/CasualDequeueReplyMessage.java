package se.kodarkatten.casual.network.messages.queue;

import se.kodarkatten.casual.network.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.parseinfo.DequeueReplySizes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;

public final class CasualDequeueReplyMessage implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final List<DequeueMessage> messages;

    private CasualDequeueReplyMessage(final UUID execution, final List<DequeueMessage> messages)
    {
        this.execution = execution;
        this.messages = messages;
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
        CasualNetworkWriterUtils.writeUUID(execution, partialContent);
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
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CasualDequeueReplyMessage that = (CasualDequeueReplyMessage) o;
        return Objects.equals(execution, that.execution) &&
            Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, messages);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualDequeueReplyMessage{");
        sb.append("execution=").append(execution);
        sb.append(", messages=").append(messages);
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
        return messages.stream().collect(Collectors.toList());
    }

    public static final class Builder
    {
        private UUID execution;
        private List<DequeueMessage> messages;

        private Builder()
        {
        }

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

        public CasualDequeueReplyMessage build()
        {
            Objects.requireNonNull(execution, "execution is not allowed to be null");
            Objects.requireNonNull(messages, "messages is not allowed to be null, can be empty though");
            return new CasualDequeueReplyMessage(execution, messages.stream().collect(Collectors.toList()));
        }
    }
}
