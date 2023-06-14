/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.CasualNWMessageHeader;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-02.
 */
public final class CasualNWMessageHeaderDecoder
{
    private CasualNWMessageHeaderDecoder()
    {}

    public static CasualNWMessageHeader fromNetworkBytes(final byte[] message)
    {
        Objects.requireNonNull(message, "byte[] is null");
        if(message.length != MessageHeaderSizes.getHeaderNetworkSize())
        {
            throw new CasualProtocolException("Expected network header size of: " + MessageHeaderSizes.getHeaderNetworkSize() + " but got: " + message.length);
        }
        ByteBuf buffer = Unpooled.wrappedBuffer(message);
        final CasualNWMessageType type = CasualNWMessageType.unmarshal((int)buffer.readLong());
        final UUID correlationId = CasualMessageDecoderUtils.readUUID(buffer);
        buffer.release();
        final long payloadSize = getPayloadSize(message);
        return CasualNWMessageHeader.createBuilder()
                                     .setType(type)
                                     .setCorrelationId(correlationId)
                                     .setPayloadSize(payloadSize)
                                     .build();
    }

    private static long getPayloadSize(final byte[] message)
    {
        final ByteBuffer payloadSize = ByteBuffer.wrap(message,  MessageHeaderSizes.HEADER_TYPE.getNetworkSize() +
                                                                 MessageHeaderSizes.HEADER_CORRELATION.getNetworkSize(),
                                                                 MessageHeaderSizes.HEADER_PAYLOAD_SIZE.getNetworkSize());
        return payloadSize.getLong();
    }


}
