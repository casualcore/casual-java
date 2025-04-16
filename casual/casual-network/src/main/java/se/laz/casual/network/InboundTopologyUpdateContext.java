/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Objects;
import java.util.UUID;

/**
 * Contains the currently connected clients, inbound or reverse inbound,
 * that should be informed of topology update events
 */
public class InboundTopologyUpdateContext
{
    // note: DefaultChannelGroup is backed by ConcurrentMap
    private static final ChannelGroup connectedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private InboundTopologyUpdateContext()
    {}
    public static void add(Channel channel)
    {
        Objects.requireNonNull(channel, "channel cannot be null");
        connectedClients.add(channel);
    }

    public static void remove(Channel channel)
    {
        Objects.requireNonNull(channel, "channel cannot be null");
        connectedClients.remove(channel);
    }

    public static void sendTopologyUpdate(UUID executionId)
    {
        for (Channel channel : connectedClients)
        {
            if (channel.isWritable())
            {
                InboundMessageSender.sendDomainDiscoveryImplicitUpdate(channel,executionId);
            }
        }
    }

    public static void clear()
    {
        connectedClients.clear();
    }

}
