/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


/**
 * Created by aleph on 2017-03-09.
 * Convenience class to send complete messages on the wire
 */
public class CasualNWMessageImpl<T extends CasualNetworkTransmittable> implements CasualNWMessage<T>
{
    private final UUID correlationId;
    private final T message;
    private CasualNWMessageImpl(final UUID correlationId, final T message)
    {
        this.correlationId = correlationId;
        this.message = message;
    }

    public static <T extends CasualNetworkTransmittable> CasualNWMessageImpl<T> of(final UUID correlationId, final T message)
    {
        return new CasualNWMessageImpl<>(correlationId, message);
    }

    @Override
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
    @Override
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
    @Override
    public UUID getCorrelationId()
    {
        return correlationId;
    }

    @Override
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
        CasualNWMessageImpl<?> that = (CasualNWMessageImpl<?>) o;
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
        final StringBuilder sb = new StringBuilder("CasualNWMessageImpl{");
        sb.append("correlationId=").append(correlationId);
        sb.append(", message=").append(message);
        sb.append('}');
        return sb.toString();
    }
}
