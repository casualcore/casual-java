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
    private static final Map<String, CasualServiceCallExtension> casualServiceCallExtensionCache = new ConcurrentHashMap<>();
    private static final DefaultCasualServiceCallExtension CASUAL_SERVICE_CALL_EXTENSION = new DefaultCasualServiceCallExtension();

    private CasualServiceCallExtensionFactory()
    {}

    /**
     * Get all registered {@link CasualServiceCallExtension} instances.
     *
     * @return available handlers.
     */
    private static List<CasualServiceCallExtension> getHandlers()
    {
        List<CasualServiceCallExtension> handlers = new ArrayList<>();
        for( CasualServiceCallExtension h: ServiceLoader.load( CasualServiceCallExtension.class ) )
        {
            handlers.add( h );
        }
        return handlers;
    }

    /**
     * Retrieve the most appropriate {@link CasualServiceCallExtension} base on it's {@link Priority}.
     *
     * If there is no registered handler a PassThoughHandler is returned, which will ensure no transformation takes place.
     *
     * @param name to get a handler for.
     * @return the error handler.
     */
    public static CasualServiceCallExtension getExtension(String name)
    {
        if( casualServiceCallExtensionCache.containsKey( name ) )
        {
            return casualServiceCallExtensionCache.get( name );
        }

        List<CasualServiceCallExtension> handlers = getHandlers();
        Prioritise.highestToLowest( handlers );

        for( CasualServiceCallExtension h: handlers )
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
