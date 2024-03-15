/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import se.laz.casual.jca.RuntimeInformation;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceCallEventPublisher
{
    private static final Logger log = Logger.getLogger(ServiceCallEventPublisher.class.getName());
    private final ServiceCallEventStore handler;

    private ServiceCallEventPublisher(ServiceCallEventStore handler)
    {
        this.handler = handler;
    }

    public static ServiceCallEventPublisher of(ServiceCallEventStore handler)
    {
        Objects.requireNonNull(handler, "handler can not be null");
        return new ServiceCallEventPublisher(handler);
    }

    /**
     *  Only posts an event in the case that the event server is up and running
     *  If not, it does nothing
     *
     * @param event The event
     */
    public void post(ServiceCallEvent event)
    {
        Objects.requireNonNull(event, "event can not be null");
        if(!RuntimeInformation.isEventServerStarted())
        {
            return;
        }
        try
        {
            handler.put(event);
        }
        catch(Exception ee)
        {
            // catch,almost all, since failure to post should not impact any service call flow
            log.log(Level.WARNING, ee, () -> "Failed to post service call event - metrics will not be available for " + event);
        }
    }

}
