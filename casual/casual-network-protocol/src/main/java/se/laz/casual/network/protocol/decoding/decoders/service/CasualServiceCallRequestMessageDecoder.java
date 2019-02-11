/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.service;

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
     * @return
     */
    public static int getMaxPayloadSingleBufferByteSize()
    {
        return maxPayloadSingleBufferByteSize;
    }

    /**
     * If not set, defaults to Integer.MAX_VALUE
     * Can be used in testing to force chunked reading
     * by for instance setting it to 1
     * @return
     */
    public static void setMaxPayloadSingleBufferByteSize(int maxPayloadSingleBufferByteSize)
    {
        CasualServiceCallRequestMessageDecoder.maxPayloadSingleBufferByteSize = maxPayloadSingleBufferByteSize;
    }

    @Override
    public CasualServiceCallRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return createMessage(b.array());
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
    public CasualServiceCallRequestMessage readSingleBuffer(byte[] data)
    {
        return createMessage(data);
    }

    private static CasualServiceCallRequestMessage createMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, ServiceCallRequestSizes.EXECUTION.getNetworkSize()));
        currentOffset += ServiceCallRequestSizes.EXECUTION.getNetworkSize();

        int serviceNameLen = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.SERVICE_NAME_SIZE.getNetworkSize();
        final String serviceName = CasualMessageDecoderUtils.getAsString(data, currentOffset, serviceNameLen);
        currentOffset += serviceNameLen;

        long timeout  = ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.SERVICE_TIMEOUT.getNetworkSize();

        final int parentNameSize = (int)ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.PARENT_NAME_SIZE.getNetworkSize();
        final String parentName = CasualMessageDecoderUtils.getAsString(data, currentOffset, parentNameSize);
        currentOffset += parentNameSize;

        Pair<Integer, Xid> xidInfo = CasualMessageDecoderUtils.readXid(data, currentOffset);
        currentOffset = xidInfo.first();
        final Xid xid = xidInfo.second();

        int flags = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.FLAGS.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.FLAGS.getNetworkSize();
        int serviceBufferTypeSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize();
        final String serviceTypeName = CasualMessageDecoderUtils.getAsString(data, currentOffset, serviceBufferTypeSize);
        currentOffset += serviceBufferTypeSize;
        // this can be huge, ie not fitting into one ByteBuffer
        // but since the whole message fits into Integer.MAX_VALUE that is not true of this message
        int serviceBufferPayloadSize = (int) ByteBuffer.wrap(data, currentOffset, ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize()).getLong();
        currentOffset += ServiceCallRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize();
        final byte[] payloadData = Arrays.copyOfRange(data, currentOffset, currentOffset + serviceBufferPayloadSize);
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
