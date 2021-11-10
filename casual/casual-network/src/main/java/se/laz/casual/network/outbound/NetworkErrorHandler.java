/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.Channel;

import java.util.logging.Logger;

public final class NetworkErrorHandler
{
    private static final Logger LOG = Logger.getLogger(NetworkErrorHandler.class.getName());
    private NetworkErrorHandler()
    {}

    public static void notifyListenerIfNotConnected(Channel channel, NetworkListener networkListener)
    {
        if(!channel.isActive())
        {
            LOG.finest("network connection gone, informing listener");
            networkListener.disconnected();
        }
    }
}
