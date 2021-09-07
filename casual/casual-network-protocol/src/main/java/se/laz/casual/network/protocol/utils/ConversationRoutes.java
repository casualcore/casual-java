/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils;

import se.laz.casual.api.util.Pair;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationConnectRequestSizes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class ConversationRoutes
{
    private ConversationRoutes()
    {}

    public static Pair<Integer, List<UUID>> getRoutes(int numberOfRoutes, byte[] data, int currentOffset)
    {
        List<UUID> routes = new ArrayList<>();
        for(int i = 0; i<numberOfRoutes; ++i)
        {
            UUID routeId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, currentOffset + ConversationConnectRequestSizes.RECORDING_ELEMENT_SIZE.getNetworkSize()));
            currentOffset += ConversationConnectRequestSizes.RECORDING_ELEMENT_SIZE.getNetworkSize();
            routes.add(routeId);
        }
        return Pair.of(currentOffset, routes);
    }
}
