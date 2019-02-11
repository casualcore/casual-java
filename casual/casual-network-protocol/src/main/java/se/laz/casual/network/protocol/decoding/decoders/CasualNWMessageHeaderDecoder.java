/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders;

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
        final CasualNWMessageType type = getMessageType(message);
        final UUID correlationId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(message,
                                                                                         MessageHeaderSizes.HEADER_TYPE.getNetworkSize(),
                                                                                     MessageHeaderSizes.HEADER_TYPE.getNetworkSize() +
                                                                                         MessageHeaderSizes.HEADER_CORRELATION.getNetworkSize()));
        final long payloadSize = getPayloadSize(message);
        return  CasualNWMessageHeader.createBuilder()
                                     .setType(type)
                                     .setCorrelationId(correlationId)
                                     .setPayloadSize(payloadSize)
                                     .build();
    }

    private static CasualNWMessageType getMessageType(final byte[] message)
    {
        final ByteBuffer b = ByteBuffer.wrap(message, 0, MessageHeaderSizes.HEADER_TYPE.getNetworkSize());
        return CasualNWMessageType.unmarshal((int)b.getLong());
    }

    private static long getPayloadSize(final byte[] message)
    {
        final ByteBuffer payloadSize = ByteBuffer.wrap(message,  MessageHeaderSizes.HEADER_TYPE.getNetworkSize() +
                                                                 MessageHeaderSizes.HEADER_CORRELATION.getNetworkSize(),
                                                                 MessageHeaderSizes.HEADER_PAYLOAD_SIZE.getNetworkSize());
        return payloadSize.getLong();
    }
}
