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
import se.laz.casual.api.util.Pair;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.conversation.ConnectRequest;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationConnectRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class ConnectRequestMessageDecoder implements NetworkDecoder<ConnectRequest>
{
    private ConnectRequestMessageDecoder()
    {}

    public static NetworkDecoder<ConnectRequest> of()
    {
        return new ConnectRequestMessageDecoder();
    }

    @Override
    public ConnectRequest readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        ConnectRequest msg = createMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public ConnectRequest readChunked(final ReadableByteChannel channel)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ConnectRequest readSingleBuffer(final ByteBuf buffer)
    {
        return createMessage(buffer);
    }

    private static ConnectRequest createMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        int serviceNameLen = (int)buffer.readLong();
        final String serviceName = CasualMessageDecoderUtils.readAsString(buffer, serviceNameLen);
        long timeout  = buffer.readLong();
        final int parentNameSize = (int)buffer.readLong();
        final String parentName = CasualMessageDecoderUtils.readAsString(buffer, parentNameSize);
        final Xid xid = CasualMessageDecoderUtils.readXid(buffer);

        Duplex duplex = Duplex.unmarshall(buffer.readShort());
        int serviceBufferTypeSize = (int) buffer.readLong();
        final String serviceTypeName = CasualMessageDecoderUtils.readAsString(buffer, serviceBufferTypeSize);
        int serviceBufferPayloadSize = (int)buffer.readLong();
        final byte[] payloadData = new byte[serviceBufferPayloadSize];
        buffer.readBytes(payloadData);
        final List<byte[]> serviceBufferPayload = new ArrayList<>();
        if(payloadData.length > 0)
        {
            serviceBufferPayload.add(payloadData);
        }
        final ServiceBuffer serviceBuffer = ServiceBuffer.of(serviceTypeName, serviceBufferPayload);
        return ConnectRequest.createBuilder()
                .setExecution(execution)
                .setServiceName(serviceName)
                .setTimeout(timeout)
                .setParentName(parentName)
                .setXid(xid)
                .setDuplex(duplex)
                .setServiceBuffer(serviceBuffer)
                .build();
    }

}
