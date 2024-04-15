package se.laz.casual.event.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface InitFunction
{
    Channel init(final InetSocketAddress address, EventObserver eventObserver, boolean enableLogHandler);
}
