/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.conversation;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.conversation.Duplex;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.conversation.Request;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
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
        return createMessage(b.array());
    }

    @Override
    public Request readChunked(final ReadableByteChannel channel)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Request readSingleBuffer(byte[] data)
    {
        return createMessage(data);
    }

    private static Request createMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, ConversationRequestSizes.EXECUTION.getNetworkSize()));
        currentOffset += ConversationRequestSizes.EXECUTION.getNetworkSize();

        Duplex duplex = Duplex.unmarshall(ByteBuffer.wrap(data, currentOffset, ConversationRequestSizes.DUPLEX.getNetworkSize()).getShort());
        currentOffset += ConversationRequestSizes.DUPLEX.getNetworkSize();

        int resultCode  = ByteBuffer.wrap(data, currentOffset, ConversationRequestSizes.RESULT_CODE.getNetworkSize()).getInt();
        currentOffset += ConversationRequestSizes.RESULT_CODE.getNetworkSize();

        long userCode  = ByteBuffer.wrap(data, currentOffset, ConversationRequestSizes.USER_CODE.getNetworkSize()).getLong();
        currentOffset += ConversationRequestSizes.USER_CODE.getNetworkSize();

        int serviceBufferTypeSize = (int) ByteBuffer.wrap(data, currentOffset, ConversationRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += ConversationRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize();
        final String serviceTypeName = CasualMessageDecoderUtils.getAsString(data, currentOffset, serviceBufferTypeSize);
        currentOffset += serviceBufferTypeSize;
        int serviceBufferPayloadSize = (int) ByteBuffer.wrap(data, currentOffset, ConversationRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize()).getLong();
        currentOffset += ConversationRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize();
        final byte[] payloadData = Arrays.copyOfRange(data, currentOffset, currentOffset + serviceBufferPayloadSize);
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
