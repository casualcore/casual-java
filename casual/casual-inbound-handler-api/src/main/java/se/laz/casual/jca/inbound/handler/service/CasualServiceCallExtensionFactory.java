/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service;

import se.laz.casual.spi.Prioritise;
import se.laz.casual.spi.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class CasualServiceCallExtensionFactory
{
    private static final Map<String, CasualServiceHandlerExtension> casualServiceCallExtensionCache = new ConcurrentHashMap<>();
    private static final DefaultCasualServiceHandlerExtension CASUAL_SERVICE_CALL_EXTENSION = new DefaultCasualServiceHandlerExtension();

    private CasualServiceCallExtensionFactory()
    {}

    /**
     * Get all registered {@link CasualServiceHandlerExtension} instances.
     *
     * @return available handlers.
     */
    private static List<CasualServiceHandlerExtension> getHandlers()
    {
        List<CasualServiceHandlerExtension> handlers = new ArrayList<>();
        for( CasualServiceHandlerExtension h: ServiceLoader.load( CasualServiceHandlerExtension.class ) )
        {
            handlers.add( h );
        }
        return handlers;
    }

    /**
     * Retrieve the most appropriate {@link CasualServiceHandlerExtension} base on it's {@link Priority}.
     *
     * If there is no registered handler a PassThoughHandler is returned, which will ensure no transformation takes place.
     *
     * @param name to get a handler for.
     * @return the error handler.
     */
    public static CasualServiceHandlerExtension getExtension(String name)
    {
        if( casualServiceCallExtensionCache.containsKey( name ) )
        {
            return casualServiceCallExtensionCache.get( name );
        }

        List<CasualServiceHandlerExtension> handlers = getHandlers();
        Prioritise.highestToLowest( handlers );

        for( CasualServiceHandlerExtension h: handlers )
        {
            casualServiceCallExtensionCache.put(name, h);
            if( h.canHandle( name ) )
            {
                casualServiceCallExtensionCache.put( name, h );
                return h;
            }
        }
        casualServiceCallExtensionCache.put( name, CASUAL_SERVICE_CALL_EXTENSION);
        return CASUAL_SERVICE_CALL_EXTENSION;
    }
}
