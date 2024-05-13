/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import se.laz.casual.event.ServiceCallEventStore;
import se.laz.casual.event.ServiceCallEventStoreFactory;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class EventServer
{
    private static final Logger log = Logger.getLogger(EventServer.class.getName());
    private final Channel channel;
    private final long shutdownQuietPeriod;
    private final long shutdownTimeout;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private EventServer(Channel channel, long quietPeriod, long timeout, EventLoopGroup bossGroup, EventLoopGroup workerGroup)
    {
        Objects.requireNonNull(channel, "channel can not be null");
        this.channel = channel;
        this.shutdownQuietPeriod = quietPeriod;
        this.shutdownTimeout = timeout;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
    }

    public static EventServer of(EventServerConnectionInformation connectionInformation, UUID domainId )
    {
        Objects.requireNonNull(connectionInformation, "connectionInformation can not be null");
        ChannelGroup connectedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        EventLoopGroup bossGroup = connectionInformation.createEventLoopGroup();
        EventLoopGroup workerGroup = connectionInformation.createEventLoopGroup();
        Channel ch =  connectionInformation.getServerInitialization().init(connectionInformation, connectedClients, bossGroup, workerGroup);
        final ServiceCallEventStore serviceCallEventStore = ServiceCallEventStoreFactory.getStore(domainId);
        MessageLoop messageLoop = DefaultMessageLoop.of(connectedClients, serviceCallEventStore);
        EventServer eventServer = new EventServer(ch, connectionInformation.getShutdownQuietPeriod(), connectionInformation.getShutdownTimeout(), bossGroup, workerGroup);
        eventServer.setLoopConditionAndDispatch(Executors.newSingleThreadExecutor(), messageLoop);
        return eventServer;
    }



    public void setLoopConditionAndDispatch(ExecutorService executorService, MessageLoop messageLoop)
    {
        Objects.requireNonNull(executorService, "executorService can not be null");
        Objects.requireNonNull(messageLoop, "messageLoop can not be null");
        messageLoop.accept(this::isActive);
        executorService.execute(messageLoop::handleMessages);
    }

    public boolean isActive()
    {
        if (this.channel != null)
        {
            return this.channel.isActive();
        }
        return false;
    }

    public void close()
    {
        log.info(() -> "closing event server");
        bossGroup.shutdownGracefully( shutdownQuietPeriod, shutdownTimeout, TimeUnit.MILLISECONDS ).syncUninterruptibly();
        workerGroup.shutdownGracefully( shutdownQuietPeriod, shutdownTimeout, TimeUnit.MILLISECONDS ).syncUninterruptibly();
        log.info(() -> "event server closed");
    }

}
