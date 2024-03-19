/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.spi.Prioritise;
import se.laz.casual.spi.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceCallEventHandlerFactory
{
    private ServiceCallEventHandlerFactory()
    {}

    /**
     * Retrieve the most appropriate {@link ServiceCallEventStore} based on it's {@link Priority}.
     *
     * If there is no registered handler a {@link CasualRuntimeException} is thrown.
     *
     * @return the appropriate handler.
     */
    public static ServiceCallEventStore getHandler()
    {
        return getHandlers().stream()
                            .findFirst()
                            .orElseThrow(() -> new NoServiceCallEventHandlerFoundException("No ServiceCallEventHandler found"));
    }

    private static List<ServiceCallEventStore> getHandlers()
    {
        List<ServiceCallEventStore> handlers = new ArrayList<>();
        for( ServiceCallEventStore h: ServiceLoader.load( ServiceCallEventStore.class ) )
        {
            handlers.add( h );
        }
        Prioritise.highestToLowest(handlers);
        return handlers;
    }
}
