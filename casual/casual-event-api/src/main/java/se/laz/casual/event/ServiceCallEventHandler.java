package se.laz.casual.event;

import se.laz.casual.spi.Prioritisable;

public interface ServiceCallEventHandler extends Prioritisable
{
    void put(ServiceCallEvent event);
    ServiceCallEvent take();
}
