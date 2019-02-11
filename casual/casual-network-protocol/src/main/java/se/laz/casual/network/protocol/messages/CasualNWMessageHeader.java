/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by aleph on 2017-02-23.
 */
public final class CasualNWMessageHeader
{
    private final CasualNWMessageType type;
    private final UUID correlationId;
    private final long payloadSize;

    private CasualNWMessageHeader(final CasualNWMessageType type, final UUID correlationId, long payloadSize)
    {
        this.type = type;
        this.correlationId = correlationId;
        this.payloadSize = payloadSize;
    }

    public static CasualNWMessageHeader of(CasualNWMessageHeader header)
    {
        return   CasualNWMessageHeader.createBuilder()
                                      .setType(header.getType())
                                      .setCorrelationId(header.getCorrelationId())
                                      .setPayloadSize(header.getPayloadSize())
                                      .build();
    }

    public CasualNWMessageType getType()
    {
        return type;
    }

    public UUID getCorrelationId()
    {
        return correlationId;
    }

    public long getPayloadSize()
    {
        return payloadSize;
    }

    public byte[] toNetworkBytes()
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(MessageHeaderSizes.getHeaderNetworkSize());
        return byteBuffer.putLong(CasualNWMessageType.marshal(type))
                  .putLong(correlationId.getMostSignificantBits())
                  .putLong(correlationId.getLeastSignificantBits())
                  .putLong(payloadSize).array();
    }



    public static CasualNWMessageHeaderBuilder createBuilder()
    {
        return new CasualNWMessageHeaderBuilder();
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
        CasualNWMessageHeader that = (CasualNWMessageHeader) o;
        return payloadSize == that.payloadSize &&
            type == that.type &&
            Objects.equals(correlationId, that.correlationId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, correlationId, payloadSize);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualNWMessageHeader{");
        sb.append("type=").append(type);
        sb.append(", correlationId=").append(correlationId);
        sb.append(", payloadSize=").append(payloadSize);
        sb.append('}');
        return sb.toString();
    }

    public static class CasualNWMessageHeaderBuilder
    {
        private CasualNWMessageType type;
        private UUID correlationId;
        private long payloadSize;

        private CasualNWMessageHeaderBuilder()
        {}

        public CasualNWMessageHeaderBuilder setType(CasualNWMessageType type)
        {
            this.type = type;
            return this;
        }

        public CasualNWMessageHeaderBuilder setPayloadSize(long payloadSize)
        {
            this.payloadSize = payloadSize;
            return this;
        }

        public CasualNWMessageHeaderBuilder setCorrelationId(UUID correlationId)
        {
            this.correlationId = new UUID(correlationId.getMostSignificantBits(), correlationId.getLeastSignificantBits());
            return this;
        }

        public CasualNWMessageHeader build()
        {
            assertSanity();
            return new CasualNWMessageHeader(type, correlationId, payloadSize);
        }



        private void assertSanity()
        {
            if(null == correlationId)
            {
                throw new CasualProtocolException("Correlation ID is null");
            }
            if(0 == payloadSize)
            {
                throw new CasualProtocolException("0 payload size");
            }
        }
    }

}
