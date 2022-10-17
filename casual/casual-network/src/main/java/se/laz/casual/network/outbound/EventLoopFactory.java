/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import se.laz.casual.api.util.JEEConcurrencyFactory;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Outbound;

import java.util.logging.Logger;

public final class EventLoopFactory
{
    private static final Logger LOG = Logger.getLogger(EventLoopFactory.class.getName());
    private static final EventLoopGroup INSTANCE = createEventLoopGroup();
    private EventLoopFactory()
    {}
    public static synchronized EventLoopGroup getInstance()
    {
        return INSTANCE;
    }

    private static EventLoopGroup createEventLoopGroup()
    {
        Outbound outbound = ConfigurationService.getInstance().getConfiguration().getOutbound();
        if(outbound.getUnmanaged())
        {
            return getUnmanagedEventLoopGroup(outbound.getUseEpoll(), outbound.getNumberOfThreads());
        }
        return getManagedEventLoopGroup(outbound.getUseEpoll(), outbound.getNumberOfThreads());
    }

    private static EventLoopGroup getUnmanagedEventLoopGroup(boolean useEpoll, int numberOfThreads)
    {
        LOG.info(() -> "outbound not using any ManagedExecutorService, running unmanaged");
        if(useEpoll)
        {
            LOG.info(() -> "using EpollEventLoopGroup");
            return new EpollEventLoopGroup(numberOfThreads);
        }
        LOG.info(() -> "using NioEventLoopGroup");
        return new NioEventLoopGroup(numberOfThreads);
    }

    private static EventLoopGroup getManagedEventLoopGroup(boolean useEpoll, int numberOfThreads)
    {
        if (useEpoll)
        {
            LOG.info(() -> "using EpollEventLoopGroup");
            return new EpollEventLoopGroup(numberOfThreads, JEEConcurrencyFactory.getManagedExecutorService());
        }
        LOG.info(() -> "using NioEventLoopGroup");
        return new NioEventLoopGroup(numberOfThreads, JEEConcurrencyFactory.getManagedExecutorService());
    }

}
