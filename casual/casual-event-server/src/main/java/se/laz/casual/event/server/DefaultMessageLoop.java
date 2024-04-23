/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server;

import io.netty.channel.group.ChannelGroup;
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.ServiceCallEventStore;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;

public class DefaultMessageLoop implements MessageLoop
{
    private static final Logger log = Logger.getLogger(DefaultMessageLoop.class.getName());
    private final ChannelGroup connectedClients;
    private final ServiceCallEventStore serviceCallEventStore;
    private BooleanSupplier continueLoop = () -> false;

    private DefaultMessageLoop(ChannelGroup connectedClients, ServiceCallEventStore serviceCallEventStore)
    {
        this.connectedClients = connectedClients;
        this.serviceCallEventStore = serviceCallEventStore;
    }

    public static MessageLoop of(ChannelGroup connectedClients, ServiceCallEventStore serviceCallEventStore)
    {
        Objects.requireNonNull(connectedClients, "connectedClients can not be null");
        Objects.requireNonNull(serviceCallEventStore, "serviceCallEventStore can not be null");
        return new DefaultMessageLoop(connectedClients, serviceCallEventStore);
    }

    public void handleMessages()
    {
        while (continueLoop.getAsBoolean())
        {
            ServiceCallEvent event = serviceCallEventStore.take();
            log.finest(() -> "# of clients: " + connectedClients.size());
            log.finest(() -> "writing: " + event + " to all clients");
            connectedClients.writeAndFlush(event);
        }
    }

    @Override
    public void accept(BooleanSupplier continueLoop)
    {
        Objects.requireNonNull(continueLoop, "loop can not be null");
        this.continueLoop = continueLoop;
    }
}
