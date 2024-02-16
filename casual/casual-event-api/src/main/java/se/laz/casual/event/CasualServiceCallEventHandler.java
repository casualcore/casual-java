package se.laz.casual.event;

import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Default event handler, you can override it by writing your own event handler with the correct priority
 * and making sure that it is available on the class path
 */
public class CasualServiceCallEventHandler implements ServiceCallEventHandler
{
    private static final BlockingDeque<ServiceCallEvent> EVENTS = new LinkedBlockingDeque<>();

    @Override
    public void put(ServiceCallEvent event)
    {
        Objects.requireNonNull(event, "event can not be null");
        EVENTS.add(event);
    }

    @Override
    public ServiceCallEvent take()
    {
        try
        {
            return EVENTS.takeFirst();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new CasualServiceCallEventHandlerInterruptedException("CasualServiceCallEventHandler::takeFirst interrupted");
        }
    }
}
