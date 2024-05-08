/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.spi.Prioritise;
import se.laz.casual.spi.Priority;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceCallEventStoreFactory
{
    private static final Map<UUID, ServiceCallEventStore> STORES = new ConcurrentHashMap<>();

    private ServiceCallEventStoreFactory()
    {}

    /**
     * Retrieve the most appropriate {@link ServiceCallEventStore} based on it's {@link Priority}.
     *
     * If there is no registered handler a {@link CasualRuntimeException} is thrown.
     *
     * The store is cached based on the provided domainId.
     *
     * @param domainId unique id of the domain to which the ServiceCallEventStore belongs.
     * @return the appropriate handler.
     */
    public static ServiceCallEventStore getStore(UUID domainId)
    {
        return STORES.computeIfAbsent( domainId, id -> getStores().stream()
                            .findFirst()
                            .orElseThrow(() -> new NoServiceCallEventHandlerFoundException("No ServiceCallEventHandler found")) );
    }

    private static List<ServiceCallEventStore> getStores()
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
