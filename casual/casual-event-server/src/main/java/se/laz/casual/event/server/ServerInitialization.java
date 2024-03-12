/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

public interface ServerInitialization
{
    Channel init(EventServerConnectionInformation connectionInformation, ChannelGroup connectedClients);
}
