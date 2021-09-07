/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.conversation;

import se.laz.casual.api.util.Pair;
import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.conversation.Disconnect;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationDisconnectSizes;
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
public final class DisconnectMessageDecoder implements NetworkDecoder<Disconnect>
{
    private DisconnectMessageDecoder()
    {}

    public static NetworkDecoder<Disconnect> of()
    {
        return new DisconnectMessageDecoder();
    }

    @Override
    public Disconnect readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        final ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return createMessage(b.array());
    }

    @Override
    public Disconnect readChunked(final ReadableByteChannel channel)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Disconnect readSingleBuffer(byte[] data)
    {
        return createMessage(data);
    }

    private static Disconnect createMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, ConversationDisconnectSizes.EXECUTION.getNetworkSize()));
        currentOffset += ConversationDisconnectSizes.EXECUTION.getNetworkSize();

        int numberOfRoutes = (int)ByteBuffer.wrap(data, currentOffset, ConversationDisconnectSizes.ROUTES_SIZE.getNetworkSize()).getLong();
        currentOffset += ConversationDisconnectSizes.ROUTES_SIZE.getNetworkSize();
        Pair<Integer, List<UUID>> routePair = ConversationRoutes.getRoutes(numberOfRoutes, data, currentOffset);
        currentOffset = routePair.first();
        List<UUID> routes = routePair.second();

        long events = ByteBuffer.wrap(data, currentOffset, ConversationDisconnectSizes.EVENTS.getNetworkSize()).getLong();

        return Disconnect.createBuilder()
                .setExecution(execution)
                .setRoutes(routes)
                .setEvents(events)
                .build();
    }

}
