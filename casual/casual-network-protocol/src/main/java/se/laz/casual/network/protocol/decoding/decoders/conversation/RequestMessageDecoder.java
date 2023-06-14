/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.conversation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.conversation.Duplex;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.conversation.Request;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-16.
 */
public final class RequestMessageDecoder implements NetworkDecoder<Request>
{
    private RequestMessageDecoder()
    {}

    public static NetworkDecoder<Request> of()
    {
        return new RequestMessageDecoder();
    }

    @Override
    public Request readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        Request msg = createMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public Request readChunked(final ReadableByteChannel channel)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Request readSingleBuffer(final ByteBuf buffer)
    {
        return createMessage(buffer);
    }

    private static Request createMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        Duplex duplex = Duplex.unmarshall(buffer.readShort());
        int resultCode = buffer.readInt();
        long userCode  = buffer.readLong();
        int serviceBufferTypeSize = (int) buffer.readLong();
        final String serviceTypeName = CasualMessageDecoderUtils.readAsString(buffer, serviceBufferTypeSize);
        int serviceBufferPayloadSize = (int) buffer.readLong();
        final byte[] payloadData = new byte[serviceBufferPayloadSize];
        buffer.readBytes(payloadData);
        final List<byte[]> serviceBufferPayload = new ArrayList<>();
        serviceBufferPayload.add(payloadData);
        final ServiceBuffer serviceBuffer = ServiceBuffer.of(serviceTypeName, serviceBufferPayload);
        return Request.createBuilder()
                .setExecution(execution)
                .setDuplex(duplex)
                .setResultCode(resultCode)
                .setUserCode(userCode)
                .setServiceBuffer(serviceBuffer)
                .build();
    }

}
