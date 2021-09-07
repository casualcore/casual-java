/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.conversation;

import se.laz.casual.api.util.Pair;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.conversation.ConnectReply;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationConnectReplySizes;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.ConversationRoutes;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-16.
 */
public final class ConnectReplyMessageDecoder implements NetworkDecoder<ConnectReply>
{
    private ConnectReplyMessageDecoder()
    {}

    public static NetworkDecoder<ConnectReply> of()
    {
        return new ConnectReplyMessageDecoder();
    }

    @Override
    public ConnectReply readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return createMessage(b.array());
    }

    @Override
    public ConnectReply readChunked(final ReadableByteChannel channel)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ConnectReply readSingleBuffer(byte[] data)
    {
        return createMessage(data);
    }

    private static ConnectReply createMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, ConversationConnectReplySizes.EXECUTION.getNetworkSize()));
        currentOffset += ConversationConnectReplySizes.EXECUTION.getNetworkSize();

        int numberOfRoutes = (int)ByteBuffer.wrap(data, currentOffset, ConversationConnectReplySizes.RECORDING_SIZE.getNetworkSize()).getLong();
        currentOffset += ConversationConnectReplySizes.RECORDING_SIZE.getNetworkSize();
        Pair<Integer, List<UUID>> routePair = ConversationRoutes.getRoutes(numberOfRoutes, data, currentOffset);
        currentOffset = routePair.first();
        List<UUID> routes = routePair.second();

        int numberOfRecordings = (int)ByteBuffer.wrap(data, currentOffset, ConversationConnectReplySizes.RECORDING_SIZE.getNetworkSize()).getLong();
        currentOffset += ConversationConnectReplySizes.RECORDING_SIZE.getNetworkSize();
        Pair<Integer, List<UUID>> recodingPair = ConversationRoutes.getRoutes(numberOfRecordings, data, currentOffset);
        currentOffset = recodingPair.first();
        List<UUID> recordings = recodingPair.second();

        int resultCode = ByteBuffer.wrap(data, currentOffset, ConversationConnectReplySizes.RESULT_CODE.getNetworkSize()).getInt();

        return ConnectReply.createBuilder()
                .setExecution(execution)
                .setRecordingNodes(recordings)
                .setRoutes(routes)
                .setResultCode(resultCode)
                .build();
    }

}
