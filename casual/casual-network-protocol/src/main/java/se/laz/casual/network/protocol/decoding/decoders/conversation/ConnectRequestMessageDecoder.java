/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.conversation;

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
        return createMessage(b.array());
    }

    @Override
    public ConnectRequest readChunked(final ReadableByteChannel channel)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ConnectRequest readSingleBuffer(byte[] data)
    {
        return createMessage(data);
    }

    private static ConnectRequest createMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, ConversationConnectRequestSizes.EXECUTION.getNetworkSize()));
        currentOffset += ConversationConnectRequestSizes.EXECUTION.getNetworkSize();

        int serviceNameLen = (int)ByteBuffer.wrap(data, currentOffset, ConversationConnectRequestSizes.SERVICE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConversationConnectRequestSizes.SERVICE_NAME_SIZE.getNetworkSize();
        final String serviceName = CasualMessageDecoderUtils.getAsString(data, currentOffset, serviceNameLen);
        currentOffset += serviceNameLen;

        long timeout  = ByteBuffer.wrap(data, currentOffset, ConversationConnectRequestSizes.SERVICE_TIMEOUT.getNetworkSize()).getLong();
        currentOffset += ConversationConnectRequestSizes.SERVICE_TIMEOUT.getNetworkSize();

        final int parentNameSize = (int)ByteBuffer.wrap(data, currentOffset, ConversationConnectRequestSizes.PARENT_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConversationConnectRequestSizes.PARENT_NAME_SIZE.getNetworkSize();
        final String parentName = CasualMessageDecoderUtils.getAsString(data, currentOffset, parentNameSize);
        currentOffset += parentNameSize;

        Pair<Integer, Xid> xidInfo = CasualMessageDecoderUtils.readXid(data, currentOffset);
        currentOffset = xidInfo.first();
        final Xid xid = xidInfo.second();

        Duplex duplex = Duplex.unmarshall(ByteBuffer.wrap(data, currentOffset, ConversationConnectRequestSizes.DUPLEX.getNetworkSize()).getShort());
        currentOffset += ConversationConnectRequestSizes.DUPLEX.getNetworkSize();

        int serviceBufferTypeSize = (int) ByteBuffer.wrap(data, currentOffset, ConversationConnectRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConversationConnectRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize();
        final String serviceTypeName = CasualMessageDecoderUtils.getAsString(data, currentOffset, serviceBufferTypeSize);
        currentOffset += serviceBufferTypeSize;
        int serviceBufferPayloadSize = (int) ByteBuffer.wrap(data, currentOffset, ConversationConnectRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize()).getLong();
        currentOffset += ConversationConnectRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize();
        final byte[] payloadData = Arrays.copyOfRange(data, currentOffset, currentOffset + serviceBufferPayloadSize);
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
