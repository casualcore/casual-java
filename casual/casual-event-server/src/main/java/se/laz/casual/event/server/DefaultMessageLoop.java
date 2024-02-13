package se.laz.casual.event.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.ServiceCallEventHandler;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class DefaultMessageLoop implements MessageLoop
{
    private static final Logger log = Logger.getLogger(DefaultMessageLoop.class.getName());
    private final ChannelGroup connectedClients;
    private final ServiceCallEventHandler serviceCallEventHandler;
    private Supplier<Boolean> continueLoop;

    private DefaultMessageLoop(ChannelGroup connectedClients, ServiceCallEventHandler serviceCallEventHandler)
    {
        this.connectedClients = connectedClients;
        this.serviceCallEventHandler = serviceCallEventHandler;
    }

    public static MessageLoop of(ChannelGroup connectedClients, ServiceCallEventHandler serviceCallEventHandler)
    {
        Objects.requireNonNull(connectedClients, "connectedClients can not be null");
        Objects.requireNonNull(serviceCallEventHandler, "serviceCallEventHandler can not be null");
        return new DefaultMessageLoop(connectedClients, serviceCallEventHandler);
    }

    public void handleMessages()
    {
        while (continueLoop.get())
        {
            ServiceCallEvent event = serviceCallEventHandler.take();
            log.info(() -> "# of clients: " + connectedClients.size());
            for (Channel client : connectedClients)
            {
                log.info(() -> "writing to client");
                client.writeAndFlush(event);
            }
        }
    }

    @Override
    public void accept(Supplier<Boolean> continueLoop)
    {
        Objects.requireNonNull(continueLoop, "server can not be null");
        this.continueLoop = continueLoop;
    }
}
