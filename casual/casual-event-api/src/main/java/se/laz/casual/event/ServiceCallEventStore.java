/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import se.laz.casual.spi.Prioritisable;

public interface ServiceCallEventStore extends Prioritisable
{
    /**
     * Non blocking operation
     * @param event - The service call event
     */
    void put(ServiceCallEvent event);

    /**
     * Blocking operation
     * @return A service call event
     */
    ServiceCallEvent take();
}
