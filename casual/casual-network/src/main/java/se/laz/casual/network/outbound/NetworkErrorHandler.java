/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.Channel;
import static java.lang.System.Logger.Level.*;

public final class NetworkErrorHandler
{
    private static final System.Logger LOG = System.getLogger(NetworkErrorHandler.class.getName());
    private NetworkErrorHandler()
    {}

    public static void notifyListenersIfNotConnected(Channel channel, ErrorInformer errorInformer)
    {
        if(!channel.isActive())
        {
            LOG.log(DEBUG,"network connection gone, informing listeners");
            errorInformer.inform();
        }
    }
}
