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

public class CasualServiceHandlerExtensionFactory
{
    private static final Map<String, CasualServiceHandlerExtension> casualServiceHandlerExtensionCache = new ConcurrentHashMap<>();
    private static final DefaultCasualServiceHandlerExtension CASUAL_SERVICE_HANDLER_EXTENSION = new DefaultCasualServiceHandlerExtension();

    private CasualServiceHandlerExtensionFactory()
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
     * If there is no registered extension, a default extension is used {@link DefaultCasualServiceHandlerExtension}.
     *
     * @param name to get a handler for.
     * @return the error handler.
     */
    public static CasualServiceHandlerExtension getExtension(String name)
    {
        if( casualServiceHandlerExtensionCache.containsKey( name ) )
        {
            return casualServiceHandlerExtensionCache.get( name );
        }

        List<CasualServiceHandlerExtension> handlers = getHandlers();
        Prioritise.highestToLowest( handlers );

        for( CasualServiceHandlerExtension h: handlers )
        {
            casualServiceHandlerExtensionCache.put(name, h);
            if( h.canHandle( name ) )
            {
                casualServiceHandlerExtensionCache.put( name, h );
                return h;
            }
        }
        casualServiceHandlerExtensionCache.put( name, CASUAL_SERVICE_HANDLER_EXTENSION);
        return CASUAL_SERVICE_HANDLER_EXTENSION;
    }
}
