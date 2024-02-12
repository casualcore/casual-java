package se.laz.casual.event;

import se.laz.casual.spi.Prioritisable;

public interface ServiceCallEventHandler extends Prioritisable
{
    /**
     * Non blocking operation
     * @param event
     */
    void put(ServiceCallEvent event);

    /**
     * Blocking operation
     * @return
     */
    ServiceCallEvent take();
}
