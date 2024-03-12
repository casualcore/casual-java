/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import se.laz.casual.event.ServiceCallEvent;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class DefaultMessageLoop implements MessageLoop
{
    private static final Logger log = Logger.getLogger(DefaultMessageLoop.class.getName());
    private final ChannelGroup connectedClients;
    private final Supplier<ServiceCallEvent> serviceCallEventProvider;
    private BooleanSupplier continueLoop = () -> false;

    private DefaultMessageLoop(ChannelGroup connectedClients, Supplier<ServiceCallEvent> serviceCallEventProvider)
    {
        this.connectedClients = connectedClients;
        this.serviceCallEventProvider = serviceCallEventProvider;
    }

    public static MessageLoop of(ChannelGroup connectedClients, Supplier<ServiceCallEvent> serviceCallEventProvider)
    {
        Objects.requireNonNull(connectedClients, "connectedClients can not be null");
        Objects.requireNonNull(serviceCallEventProvider, "serviceCallEventProvider can not be null");
        return new DefaultMessageLoop(connectedClients, serviceCallEventProvider);
    }

    public void handleMessages()
    {
        while (continueLoop.getAsBoolean())
        {
            ServiceCallEvent event = serviceCallEventProvider.get();
            log.info(() -> "# of clients: " + connectedClients.size());
            for (Channel client : connectedClients)
            {
                log.info(() -> "writing: " + event + " to client: " + client);
                client.writeAndFlush(event);
            }
        }
    }

    @Override
    public void accept(BooleanSupplier continueLoop)
    {
        Objects.requireNonNull(continueLoop, "loop can not be null");
        this.continueLoop = continueLoop;
    }
}
