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
import se.laz.casual.event.ServiceCallEventHandler;
import se.laz.casual.event.ServiceCallEventHandlerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class EventServer
{
    private static final Logger log = Logger.getLogger(EventServer.class.getName());
    private final Channel channel;

    public EventServer(Channel channel)
    {
        Objects.requireNonNull(channel, "channel can not be null");
        this.channel = channel;
    }

    public static EventServer of(EventServerConnectionInformation connectionInformation)
    {
        Objects.requireNonNull(connectionInformation, "connectionInformation can not be null");
        ChannelGroup connectedClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        Channel ch =  connectionInformation.getServerInitialization().init(connectionInformation, connectedClients);
        final ServiceCallEventHandler serviceCallEventHandler = ServiceCallEventHandlerFactory.getHandler();
        MessageLoop messageLoop = DefaultMessageLoop.of(connectedClients, serviceCallEventHandler::take);
        EventServer eventServer = new EventServer(ch);
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
        channel.eventLoop().shutdownGracefully().syncUninterruptibly();
        log.info(() -> "event server closed");
    }

}
