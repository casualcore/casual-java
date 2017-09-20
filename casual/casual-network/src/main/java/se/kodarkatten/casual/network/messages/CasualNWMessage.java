package se.kodarkatten.casual.network.messages;

import se.kodarkatten.casual.network.utils.ByteUtils;

import java.util.*;

/**
 * Created by aleph on 2017-03-09.
 * Convenience class to send complete messages on the wire
 */
public class CasualNWMessage<T extends CasualNetworkTransmittable>
{
    private final UUID correlationId;
    private final T message;
    private CasualNWMessage(final UUID correlationId, final T message)
    {
        this.correlationId = correlationId;
        this.message = message;
    }

    public static <T extends CasualNetworkTransmittable> CasualNWMessage<T> of(final UUID correlationId, final T message)
    {
        return new CasualNWMessage<>(correlationId, message);
    }

    public CasualNWMessageType getType()
    {
        return message.getType();
    }

    /**
     * Creates a complete network transmittable unit including header
     * Note, the header is always alone in the first byte[]
     * Then the actual message is in the rest of the byte[] ( one or more depending on if the whole message fits into one byte[] or not)
     * @return
     */
    public List<byte[]> toNetworkBytes()
    {
        final List<byte[]> payload =  message.toNetworkBytes();
        final long payloadSize = ByteUtils.sumNumberOfBytes(payload);
        CasualNWMessageHeader header = CasualNWMessageHeader.createBuilder()
                                                            .setCorrelationId(getCorrelationId())
                                                            .setType(getType())
                                                            .setPayloadSize(payloadSize)
                                                            .build();
        final List<byte[]> completeMessage = new ArrayList<>();
        completeMessage.add(header.toNetworkBytes());
        completeMessage.addAll(payload);
        return completeMessage;
    }

    public UUID getCorrelationId()
    {
        return correlationId;
    }

    public T getMessage()
    {
        return message;
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
        CasualNWMessage<?> that = (CasualNWMessage<?>) o;
        return Objects.equals(correlationId, that.correlationId) &&
            Objects.equals(message, that.message);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(correlationId, message);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualNWMessage{");
        sb.append("correlationId=").append(correlationId);
        sb.append(", message=").append(message);
        sb.append('}');
        return sb.toString();
    }
}
