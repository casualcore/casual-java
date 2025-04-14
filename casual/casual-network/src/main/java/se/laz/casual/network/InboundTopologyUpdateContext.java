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

import java.util.UUID;

public class InboundTopologyUpdateContext
{
    private static final ChannelGroup connectedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private InboundTopologyUpdateContext()
    {}
    public static synchronized void add(Channel channel)
    {
        connectedClients.add(channel);
    }

    public static synchronized void remove(Channel channel)
    {
        connectedClients.remove(channel);
    }

    public static void sendTopologyUpdate(UUID executionId)
    {
        for (Channel channel : connectedClients)
        {
            if (channel.isWritable())
            {
                InboundMsgSender.sendDomainDiscoveryImplicitUpdate(channel,executionId);
            }
        }
    }

    public static void clear()
    {
        connectedClients.clear();
    }

}
