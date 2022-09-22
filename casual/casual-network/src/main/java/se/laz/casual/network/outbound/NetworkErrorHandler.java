/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.Channel;
import se.laz.casual.network.connection.CasualConnectionException;

import java.util.List;
import java.util.logging.Logger;

public final class NetworkErrorHandler
{
    private static final Logger LOG = Logger.getLogger(NetworkErrorHandler.class.getName());
    private NetworkErrorHandler()
    {}

    public static void notifyListenersIfNotConnected(Channel channel, List<NetworkListener> networkListeners)
    {
        if(!channel.isActive())
        {
            LOG.finest("network connection gone, informing listener");
            networkListeners.forEach(listener -> listener.disconnected(new CasualConnectionException("network connection is gone")));
        }
    }
}
