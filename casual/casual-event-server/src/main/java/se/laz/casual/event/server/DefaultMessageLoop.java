package se.laz.casual.event.server;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import se.laz.casual.event.ServiceCallEvent;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class DefaultMessageLoop implements MessageLoop
{
    private static final Logger log = Logger.getLogger(DefaultMessageLoop.class.getName());
    private final ChannelGroup connectedClients;
    private final Supplier<ServiceCallEvent> serviceCallEventProvider;
    private Supplier<Boolean> continueLoop;

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
        while (continueLoop.get())
        {
            ServiceCallEvent event = serviceCallEventProvider.get();
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
