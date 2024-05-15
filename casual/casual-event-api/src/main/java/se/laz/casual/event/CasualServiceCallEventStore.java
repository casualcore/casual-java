/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Default event handler, you can override it by writing your own event handler with the correct priority
 * and making sure that it is available on the class path
 */
public class CasualServiceCallEventStore implements ServiceCallEventStore
{
    private final BlockingDeque<ServiceCallEvent> serviceCallEvents = new LinkedBlockingDeque<>();

    @Override
    public void put(ServiceCallEvent event)
    {
        Objects.requireNonNull(event, "event can not be null");
        serviceCallEvents.add(event);
    }

    @Override
    public ServiceCallEvent take()
    {
        try
        {
            return serviceCallEvents.takeFirst();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new CasualServiceCallEventHandlerInterruptedException("CasualServiceCallEventHandler::takeFirst interrupted");
        }
    }
}
