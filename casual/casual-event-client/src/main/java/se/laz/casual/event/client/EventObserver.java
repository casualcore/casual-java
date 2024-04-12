package se.laz.casual.event.client;

import se.laz.casual.event.ServiceCallEvent;

@FunctionalInterface
public interface EventObserver
{
    void notify(ServiceCallEvent event);
}
