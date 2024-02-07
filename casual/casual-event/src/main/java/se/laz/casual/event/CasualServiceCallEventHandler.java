package se.laz.casual.event;

import se.laz.casual.api.CasualRuntimeException;

import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

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
            throw new CasualRuntimeException("CasualServiceCallEventHandler::takeFirst interrupted");
        }
    }
}
