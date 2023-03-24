/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.extension;

import se.laz.casual.spi.Prioritise;
import se.laz.casual.spi.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceHandlerExtensionFactory
{
    private static final Map<String, ServiceHandlerExtension> serviceHandlerExtensionCache = new ConcurrentHashMap<>();

    private ServiceHandlerExtensionFactory()
    {}

    /**
     * Get all registered {@link ServiceHandlerExtension} instances.
     *
     * @return available handlers.
     */
    private static List<ServiceHandlerExtension> getHandlers()
    {
        List<ServiceHandlerExtension> handlers = new ArrayList<>();
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
