package se.laz.casual.event.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

public interface ServerInitialization
{
    Channel init(EventServerConnectionInformation connectionInformation, ChannelGroup connectedClients);
}
