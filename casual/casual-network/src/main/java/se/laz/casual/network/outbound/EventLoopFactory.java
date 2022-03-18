/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Outbound;
import se.laz.casual.jca.CasualResourceAdapterException;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Logger;

public final class EventLoopFactory
{
    private static final Logger LOG = Logger.getLogger(NettyNetworkConnection.class.getName());
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
            LOG.info(() -> "outbound not using any ManagedExecutorService, running unmanaged");
            return new NioEventLoopGroup(outbound.getNumberOfThreads());
        }
        return new NioEventLoopGroup(outbound.getNumberOfThreads(), getManagedExecutorService());
    }

    public static ManagedExecutorService getManagedExecutorService()
    {
        Outbound outbound = ConfigurationService.getInstance().getConfiguration().getOutbound();
        String name = outbound.getManagedExecutorServiceName();
        try
        {
            LOG.info(() -> "outbound using ManagedExecutorService: " + name);
            InitialContext ctx = new InitialContext();
            return (ManagedExecutorService) ctx.lookup(name);
        }
        catch (NamingException e)
        {
            throw new CasualResourceAdapterException("failed lookup for: " + name + "\n outbound will not function!", e);
        }
    }

}
