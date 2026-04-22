/*
 * Copyright (c) 2023 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.extension;

import se.laz.casual.spi.Prioritise;
import se.laz.casual.spi.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceHandlerExtensionFactory
{
    private static final Map<String, ServiceHandlerExtension> serviceHandlerExtensionCache = new ConcurrentHashMap<>();
    private static final Set<ServiceHandlerExtension> registeredHandlers = ConcurrentHashMap.newKeySet();
    private ServiceHandlerExtensionFactory()
    {}

    /**
     * Register a handler instance programmatically.
     * Used by the Quarkus extension to make user handlers visible in quarkusDev.
     */
    public static void register(ServiceHandlerExtension handler)
    {
        Objects.requireNonNull(handler, "handler can not be null");
        registeredHandlers.add(handler);
    }

    /**
     * Get all registered {@link ServiceHandlerExtension} instances.
     *
     * @return available handlers.
     */
    private static List<ServiceHandlerExtension> getHandlers()
    {
        List<ServiceHandlerExtension> handlers = new ArrayList<>(registeredHandlers);
        for( ServiceHandlerExtension h: ServiceLoader.load( ServiceHandlerExtension.class ) )
        {
            handlers.add( h );
        }
        return handlers;
    }

    /**
     * Retrieve the most appropriate {@link ServiceHandlerExtension} base on it's {@link Priority}.
     *
     * Default extension {@link DefaultServiceHandlerExtension} is always registered with the lowest priority.
     *
     * @param name provided by the service handler to filter extensions based upon.
     * @return the service handler extension.
     */
    public static ServiceHandlerExtension getExtension( String name )
    {
        if( serviceHandlerExtensionCache.containsKey( name ) )
        {
            return serviceHandlerExtensionCache.get( name );
        }

        List<ServiceHandlerExtension> handlers = getHandlers();
        Prioritise.highestToLowest( handlers );

        ServiceHandlerExtension found = null;

        for( ServiceHandlerExtension h: handlers )
        {
            if( h.canHandle( name ) )
            {
                serviceHandlerExtensionCache.put( name, h );
                found = h;
                break;
            }
        }
        if( null == found )
        {
            throw new ServiceHandlerExtensionMissingException(() -> "No ServiceHandlerExtension found for name: " + name +
                    " This should NEVER happen as DefaultServiceHandlerExtension should always be the fallback!");
        }
        return found;
    }
}
