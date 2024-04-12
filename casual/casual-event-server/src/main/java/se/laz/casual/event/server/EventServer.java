/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import se.laz.casual.event.ServiceCallEventHandlerFactory;
import se.laz.casual.event.ServiceCallEventStore;

import java.util.Objects;
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

    public EventServer(Channel channel, long quietPeriod, long timeout )
    {
        Objects.requireNonNull(channel, "channel can not be null");
        this.channel = channel;
        this.shutdownQuietPeriod = quietPeriod;
        this.shutdownTimeout = timeout;
    }

    public static EventServer of(EventServerConnectionInformation connectionInformation)
    {
        Objects.requireNonNull(connectionInformation, "connectionInformation can not be null");
        ChannelGroup connectedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        Channel ch =  connectionInformation.getServerInitialization().init(connectionInformation, connectedClients);
        final ServiceCallEventStore serviceCallEventStore = ServiceCallEventHandlerFactory.getHandler();
        MessageLoop messageLoop = DefaultMessageLoop.of(connectedClients, serviceCallEventStore);
        EventServer eventServer = new EventServer(ch, connectionInformation.getShutdownQuietPeriod(), connectionInformation.getShutdownTimeout() );
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
        channel.close().syncUninterruptibly();
        channel.eventLoop().shutdownGracefully( shutdownQuietPeriod, shutdownTimeout, TimeUnit.MILLISECONDS ).syncUninterruptibly();
        log.info(() -> "event server closed");
    }

}
