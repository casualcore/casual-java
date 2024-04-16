/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client;

import se.laz.casual.event.ServiceCallEvent;

@FunctionalInterface
public interface EventObserver
{
    void notify(ServiceCallEvent event);
}
