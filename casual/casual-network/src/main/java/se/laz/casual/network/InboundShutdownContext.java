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

import java.util.logging.Logger;

public class InboundShutdownContext
{
    private static final Logger log = Logger.getLogger(InboundShutdownContext.class.getName());
    private static final ChannelGroup connectedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static synchronized void add(Channel channel)
    {
        connectedClients.add(channel);
    }

    public static synchronized void remove(Channel channel)
    {
        connectedClients.remove(channel);
    }

    public static void prepareShutdown()
    {
        for(Channel channel : connectedClients)
        {
            if(channel.isWritable())
            {
                log.finest(() -> "server going down, sending domain topology update message to " + channel);
                InboundCleaner.sendDomainDisconnect(channel);
            }
        }
    }

}
