/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface InitFunction
{
    Channel init(final InetSocketAddress address, EventObserver eventObserver, boolean enableLogHandler);
}
