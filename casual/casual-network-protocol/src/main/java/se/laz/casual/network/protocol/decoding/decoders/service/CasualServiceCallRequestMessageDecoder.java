/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.util.Pair;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ServiceCallRequestSizes;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-16.
 */
public final class CasualServiceCallRequestMessageDecoder implements NetworkDecoder<CasualServiceCallRequestMessage>
{
    private static int maxPayloadSingleBufferByteSize = Integer.MAX_VALUE;
    private CasualServiceCallRequestMessageDecoder()
    {}

    public static NetworkDecoder<CasualServiceCallRequestMessage> of()
    {
        return new CasualServiceCallRequestMessageDecoder();
    }

    /**
     * Number of maximum bytes before any chunk reading takes place
     * Defaults to Integer.MAX_VALUE
     * @return maximum number of bytes for a single buffer payload.
     */
    public static int getMaxPayloadSingleBufferByteSize()
    {
        return maxPayloadSingleBufferByteSize;
    }

    /**
     * If not set, defaults to Integer.MAX_VALUE
     * Can be used in testing to force chunked reading
     * by for instance setting it to 1
     * @param maxPayloadSingleBufferByteSize maximum number of bytes for a single buffer payload.
     */
    public static void setMaxPayloadSingleBufferByteSize(int maxPayloadSingleBufferByteSize)
    {
        CasualServiceCallRequestMessageDecoder.maxPayloadSingleBufferByteSize = maxPayloadSingleBufferByteSize;
    }

    @Override
    public CasualServiceCallRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        ByteBuf buffer = Unpooled.wrappedBuffer(b.array());
        CasualServiceCallRequestMessage msg = createMessage(buffer);
        buffer.release();
        return msg;
    }

    @Override
    public CasualServiceCallRequestMessage readChunked(final ReadableByteChannel channel)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        final int serviceNameSize = (int) ByteUtils.readFully(channel, ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize()).getLong();
        final String serviceName = CasualMessageDecoderUtils.readString(channel, serviceNameSize);
        final long serviceTimeout = ByteUtils.readFully(channel, ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize()).getLong();
        final int parentNameSize = (int) ByteUtils.readFully(channel, ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize()).getLong();
        final String parentName = CasualMessageDecoderUtils.readString(channel, parentNameSize);
        final Xid xid = XIDUtils.readXid(channel);
        final int flags = (int) ByteUtils.readFully(channel, ServiceCallRequestSizes.FLAGS.getNetworkSize()).getLong();
        final ServiceBuffer buffer = CasualMessageDecoderUtils.readServiceBuffer(channel, getMaxPayloadSingleBufferByteSize());
        return CasualServiceCallRequestMessage.createBuilder()
                                              .setExecution(execution)
                                              .setServiceName(serviceName)
                                              .setTimeout(serviceTimeout)
                                              .setParentName(parentName)
                                              .setXid(xid)
                                              .setXatmiFlags(new Flag.Builder<AtmiFlags>(flags).build())
                                              .setServiceBuffer(buffer)
                                              .build();
    }

    @Override
    public CasualServiceCallRequestMessage readSingleBuffer(final ByteBuf buffer)
    {
        return createMessage(buffer);
    }

    private static CasualServiceCallRequestMessage createMessage(final ByteBuf buffer)
    {
        final UUID execution = CasualMessageDecoderUtils.readUUID(buffer);
        int serviceNameSize = (int)buffer.readLong();
        byte[] serviceNameBuffer = new byte[serviceNameSize];
        buffer.readBytes(serviceNameBuffer);
        final String serviceName = CasualMessageDecoderUtils.getAsString(serviceNameBuffer);
        long timeout  = buffer.readLong();
        final int parentNameSize = (int)buffer.readLong();
        byte[] parentNameBuffer = new byte[parentNameSize];
        buffer.readBytes(parentNameBuffer);
        final String parentName = CasualMessageDecoderUtils.getAsString(parentNameBuffer);
        final Xid xid = CasualMessageDecoderUtils.readXid(buffer);
        int flags = (int) buffer.readLong();
        int serviceBufferTypeSize = (int) buffer.readLong();
        byte[] serviceBufferTypeBuffer = new byte[serviceBufferTypeSize];
        buffer.readBytes(serviceBufferTypeBuffer);
        final String serviceTypeName = CasualMessageDecoderUtils.getAsString(serviceBufferTypeBuffer);
        int serviceBufferPayloadSize = (int) buffer.readLong();
        final byte[] payloadData = new byte[serviceBufferPayloadSize];
        buffer.readBytes(payloadData);
        final List<byte[]> serviceBufferPayload = new ArrayList<>();
        serviceBufferPayload.add(payloadData);
        final ServiceBuffer serviceBuffer = ServiceBuffer.of(serviceTypeName, serviceBufferPayload);
        return CasualServiceCallRequestMessage.createBuilder()
                                              .setExecution(execution)
                                              .setServiceName(serviceName)
                                              .setTimeout(timeout)
                                              .setParentName(parentName)
                                              .setXid(xid)
                                              .setXatmiFlags(new Flag.Builder<AtmiFlags>(flags).build())
                                              .setServiceBuffer(serviceBuffer)
                                              .build();
    }

}
